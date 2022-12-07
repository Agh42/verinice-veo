/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2020  Daniel Murygin
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
package org.veo.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithUserDetails

import com.github.JanLoebel.jsonschemavalidation.JsonSchemaValidationException

import org.veo.core.VeoMvcSpec
import org.veo.core.entity.exception.ReferenceTargetNotFoundException
import org.veo.persistence.access.ClientRepositoryImpl
import org.veo.persistence.access.UnitRepositoryImpl

/**
 * Tests the functionality of scenarios required for risk determination and risk analysis.
 */
@WithUserDetails("user@domain.example")
class ScenarioRiskMockMvcITSpec extends VeoMvcSpec {

    @Autowired
    private ClientRepositoryImpl clientRepository
    @Autowired
    private UnitRepositoryImpl unitRepository

    private String unitId
    private String domainId

    def setup() {
        executeInTransaction {
            def client = createTestClient()
            def domain = newDomain(client) {
                riskDefinitions = [
                    "myFirstRiskDefinition": createRiskDefinition("myFirstRiskDefinition"),
                    "mySecondRiskDefinition": createRiskDefinition("mySecondRiskDefinition"),
                    "myThirdRiskDefinition": createRiskDefinition("myThirdRiskDefinition")
                ]
                applyElementTypeDefinition(newElementTypeDefinition("scenario", it) {
                    subTypes = [
                        RiskyScenario: newSubTypeDefinition()
                    ]
                })
            }
            domainId = domain.idAsString
            unitId = unitRepository.save(newUnit(client)).idAsString
            clientRepository.save(client)
        }
    }

    def "can create a scenario without potential probability"() {
        when: "creating a scenario without probability"
        def scenarioId = parseJson(post("/scenarios", [
            name: "Flood",
            owner: [targetUri: "http://localhost/units/$unitId"],
            domains: [
                (domainId): [
                    subType: "RiskyScenario",
                    status: "NEW",
                ]
            ]
        ])).resourceId

        and: "retrieving it"
        def retrievedScenario = parseJson(get("/scenarios/$scenarioId"))

        then: "the retrieved scenario has no probability"
        retrievedScenario.domains[domainId].riskValues.isEmpty()
    }

    def "can create and update scenario potential probability"() {
        when: "creating a scenario with probabilities for different risk definitions"
        def scenarioId = parseJson(post("/scenarios", [
            name: "Flood",
            owner: [targetUri: "http://localhost/units/$unitId"],
            domains: [
                (domainId): [
                    subType: "RiskyScenario",
                    status: "NEW",
                    riskValues: [
                        myFirstRiskDefinition : [
                            potentialProbability: 0
                        ],
                        mySecondRiskDefinition : [
                            potentialProbability: 1
                        ]
                    ]
                ]
            ]
        ])).resourceId

        and: "retrieving it"
        def getScenarioResponse = get("/scenarios/$scenarioId")
        def scenarioETag = getETag(getScenarioResponse)
        def retrievedScenario = parseJson(getScenarioResponse)

        then: "the retrieved probabilities are complete"
        retrievedScenario.domains[domainId].riskValues.myFirstRiskDefinition.potentialProbability == 0
        retrievedScenario.domains[domainId].riskValues.mySecondRiskDefinition.potentialProbability == 1

        when: "updating the probabilities on the scenario"
        put("/scenarios/$scenarioId", [
            name: "Flood",
            owner: [targetUri: "http://localhost/units/$unitId"],
            domains: [
                (domainId): [
                    subType: "RiskyScenario",
                    status: "NEW",
                    riskValues: [
                        myFirstRiskDefinition : [
                            potentialProbability: 2
                        ],
                        myThirdRiskDefinition : [
                            potentialProbability: 3
                        ]
                    ]
                ]
            ]
        ], ['If-Match': scenarioETag])

        and: "retrieving it again"
        def updatedScenario = parseJson(get("/scenarios/$scenarioId"))

        then: "the changes have been applied"
        updatedScenario.domains[domainId].riskValues.myFirstRiskDefinition.potentialProbability == 2
        updatedScenario.domains[domainId].riskValues.mySecondRiskDefinition == null
        updatedScenario.domains[domainId].riskValues.myThirdRiskDefinition.potentialProbability == 3
    }

    def "can not create a scenario with an undefined value"() {
        when: "creating a scenario with an undefined probability"
        post("/scenarios", [
            name: "Flood",
            owner: [targetUri: "http://localhost/units/$unitId"],
            domains: [
                (domainId): [
                    riskValues: [
                        myFirstRiskDefinition : [
                            potentialProbability: 12345
                        ]
                    ]
                ]
            ]
        ], 400)

        then: "an exception is thrown"
        def ex = thrown(JsonSchemaValidationException)
        ex.message.contains("potentialProbability: does not have a value in the enumeration")

        when: "creating a scenario with an undefined risk definition"
        def undefinedName = "undefinedRiskDefinition"
        post("/scenarios", [
            name: "Flood",
            owner: [targetUri: "http://localhost/units/$unitId"],
            domains: [
                (domainId): [
                    subType: "RiskyScenario",
                    status: "NEW",
                    riskValues: [
                        (undefinedName) : [
                            potentialProbability: 1
                        ]
                    ]
                ]
            ]
        ], 422)

        then: "an exception is thrown"
        ex = thrown(ReferenceTargetNotFoundException)
        ex.message.contains("Risk definition '$undefinedName' was not found for domain")

        when: "creating a scenario with an undefined risk value"
        post("/scenarios", [
            name: "Flood",
            owner: [targetUri: "http://localhost/units/$unitId"],
            domains: [
                (domainId): [
                    riskValues: [
                        myFirstRiskDefinition : [
                            potentialProbability: 1,
                            implementationStatus: 1
                        ]
                    ]
                ]
            ]
        ], 400)

        then: "an exception is thrown"
        ex = thrown(JsonSchemaValidationException)
        ex.message.contains("implementationStatus: is not defined in the schema")
    }
}
