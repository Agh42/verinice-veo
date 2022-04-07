/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2022  Jonas Jordan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.veo.rest.test

class DecisionRestTest extends VeoRestTest {
    def PIA = "process_privacyImpactAssessment"
    String unitUri
    String unitId
    String domainId
    def decisions

    def setup() {
        unitId = post("/units", [name: "decision rest test unit"]).body.resourceId
        unitUri = "http://localhost/units/" + unitId

        def domain = get("/domains").body.find { it.name == "DS-GVO" }
        domainId = domain.id
        decisions = domain.decisions
    }

    def "piaMandatory decision is persisted"() {
        given:
        def decision = decisions.piaMandatory

        when: "creating a blank data processing"
        def processId = post("/processes", [
            name: "blank process",
            domains: [
                (domainId): [
                    subType: "PRO_DataProcessing",
                    status: "NEW"
                ],
            ],
            owner: [targetUri: unitUri]
        ]).body.resourceId

        then:
        with(get("/processes/$processId").body.domains[domainId].decisionResults.piaMandatory) {
            value == null
            decision.rules[decisiveRule].description.en == "Risk analysis not carried out"
            matchingRules == [decisiveRule]
            agreeingRules == [decisiveRule]
        }

        when: "adding a risk"
        addRiskValue(processId, 1)

        // TODO VEO-1282 assert that the decision has already changed based on the added risk

        and: "adding PIA related attributes"
        put("/processes/$processId", [
            name: "blank process",
            customAspects: [
                (PIA): [
                    attributes: [
                        ("${PIA}_listed"): "${PIA}_listed_negative",
                        ("${PIA}_processingCriteria"): [
                            "${PIA}_processingCriteria_automated",
                            "${PIA}_processingCriteria_specialCategories",
                        ],
                    ]
                ]
            ],
            domains: [
                (domainId): [
                    subType: "PRO_DataProcessing",
                    status: "NEW"
                ],
            ],
            owner: [targetUri: unitUri]
        ], get("/processes/$processId").parseETag())

        then: "added risk and attributes are taken into consideration"
        with(get("/processes/$processId").body.domains[domainId].decisionResults.piaMandatory) {
            value == false
            decision.rules[decisiveRule].description.en == "Processing on list of the kinds of processing operations not subject to a DPIA"
            matchingRules.collect { decision.rules[it].description.en } ==~ [
                "Processing on list of the kinds of processing operations not subject to a DPIA",
                "Two or more criteria apply"
            ]
            agreeingRules.collect { decision.rules[it].description.en } ==~ [
                "Processing on list of the kinds of processing operations not subject to a DPIA"
            ]
        }
    }

    def "piaMandatory decision can be evaluated for transient process"() {
        given: "a transient process"
        def decision = decisions.piaMandatory
        def process = [
            name: "transient process",
            customAspects: [
                (PIA): [
                    attributes:[
                        ("${PIA}_listed"): "${PIA}_listed_positive"
                    ]
                ]
            ],
            domains: [
                (domainId): [
                    subType: "PRO_DataProcessing",
                    status: "NEW"
                ],
            ],
            owner: [targetUri: unitUri]
        ]

        expect: "non-persistent evaluation to consider custom aspect attribute and missing risk values"
        with(post("/processes/decision-evaluation?decision=piaMandatory&domain=$domainId", process, 200).body) {
            value == null
            decision.rules[decisiveRule].description.en == "Risk analysis not carried out"
            matchingRules.collect{decision.rules[it].description.en} == [
                "Risk analysis not carried out",
                "Processing on list of the kinds of processing operations subject to a DPIA",
            ]
            agreeingRules.collect{decision.rules[it].description.en} == [
                "Risk analysis not carried out"
            ]
        }

        and: "no process has been persisted"
        get("/processes?unit=$unitId").body.totalItemCount == 0

        when: "persisting the process and adding a risk"
        def processId = post("/processes", process).body.resourceId
        process = get("/processes/$processId").body
        addRiskValue(processId, 1)

        then: "non-persistent evaluation returns different results due to added risk"
        with(post("/processes/decision-evaluation?decision=piaMandatory&domain=$domainId", process, 200).body) {
            value == true
            decision.rules[decisiveRule].description.en == "Processing on list of the kinds of processing operations subject to a DPIA"
            matchingRules.collect{decision.rules[it].description.en} == [
                "Processing on list of the kinds of processing operations subject to a DPIA",
            ]
            agreeingRules.collect{decision.rules[it].description.en} == [
                "Processing on list of the kinds of processing operations subject to a DPIA"
            ]
        }

        when: "adding an attribute to the transient process"
        def processUpdateTimeBeforeEvaluation = get("/processes/$processId").body.updatedAt
        process.customAspects[PIA].attributes["${PIA}_otherExclusions"] = true

        then: "non-persistent evaluation returns different results due to added attribute"
        with(post("/processes/decision-evaluation?decision=piaMandatory&domain=$domainId", process, 200).body) {
            value == false
            decision.rules[decisiveRule].description.en == "Other exclusions"
            matchingRules.collect{decision.rules[it].description.en} == [
                "Other exclusions",
                "Processing on list of the kinds of processing operations subject to a DPIA"
            ]
            agreeingRules.collect{decision.rules[it].description.en} == [
                "Other exclusions"
            ]
        }

        and: "changes to the process have not been persisted"
        with(get("/processes/$processId").body) {
            customAspects[owner.PIA].attributes.size() == 1
            updatedAt == processUpdateTimeBeforeEvaluation
        }

        and: "risk is still attached to the process"
        get("/processes/$processId/risks").body.size() == 1
    }

    private void addRiskValue(String processId, Integer residualRisk) {
        post("/scopes", [
            name: "risky scope",
            domains: [
                (domainId): [
                    riskDefinition: "DSRA"
                ]
            ],
            owner: [targetUri: unitUri],
            members: [
                [targetUri: "http://localhost/processes/$processId"]
            ],
        ])
        def scenarioId = post("/scenarios", [
            name: "danger",
            owner: [targetUri: unitUri],
        ]).body.resourceId
        post("/processes/$processId/risks", [
            domains: [
                (domainId): [
                    reference: [targetUri: "http://localhost/domains/$domainId"],
                    riskDefinitions: [
                        DSRA: [
                            impactValues: [
                                [
                                    category: "A",
                                    specificImpact: 1
                                ]
                            ],
                            riskValues: [
                                [
                                    category: "A",
                                    residualRisk: residualRisk
                                ]
                            ]
                        ]
                    ]
                ]
            ],
            scenario: [targetUri: "http://localhost/scenarios/$scenarioId"]
        ])
    }


}
