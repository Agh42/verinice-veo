/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2020  Jonas Jordan.
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
import org.springframework.test.web.servlet.ResultActions

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.JanLoebel.jsonschemavalidation.JsonSchemaValidationException
import com.networknt.schema.JsonSchema
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion

import org.veo.core.VeoMvcSpec
import org.veo.core.entity.Client
import org.veo.core.repository.UnitRepository
import org.veo.core.service.EntitySchemaService

/**
 * Tests if resources returned by the API conform to the entity schema.
 */
@WithUserDetails("user@domain.example")
class EntitySchemaConformityMvcSpec extends VeoMvcSpec {

    @Autowired
    EntitySchemaService entitySchemaService

    @Autowired
    UnitRepository unitRepository

    Client client
    ObjectMapper om = new ObjectMapper()
    String domainId
    String unitId

    def setup() {
        executeInTransaction {
            client = createTestClient()
            domainId = createTestDomain(client, DSGVO_DOMAINTEMPLATE_UUID).id.uuidValue()
            unitId = unitRepository.save(newUnit(client)).dbId
        }
    }

    def "created asset with custom aspect conforms to schema"() {
        given: "the asset schema and a newly created asset"
        def schema = getSchema("asset")
        def assetId = (String)parseJson(post("/assets", [
            customAspects: [
                asset_details: [
                    attributes: [
                        asset_details_operatingStage: "asset_details_operatingStage_operation"
                    ]
                ]
            ],
            domains: [
                (domainId): [
                    subType: "AST_Application",
                    status: "NEW",
                ]
            ],
            name: "asset",
            owner: [
                targetUri: "http://localhost/units/"+unitId,
            ]])).resourceId
        def createdAssetJson = parseNode(get("/assets/$assetId"))

        when: "validating the asset JSON"
        def validationMessages = schema.validate(createdAssetJson)

        then:
        validationMessages.empty
    }

    def "can't create an asset when custom aspect not conforms to schema"() {
        given: "asset with custom aspect that does not conform to schema"
        def asset = [
            customAspects: [
                asset_foo: [
                    attributes: [
                        asset_details_operatingStage: "asset_details_operatingStage_operation"
                    ]
                ]
            ],
            name: "asset",
            owner: [
                targetUri: "http://localhost/units/"+unitId,
            ]]
        when: "posting the asset"
        (post("/assets", asset))
        then: "an exception is thrown"
        JsonSchemaValidationException ex = thrown()
        and: "the reason is given"
        ex.message ==~ /.*customAspects.asset_foo: is not defined.*/
    }

    def "can't create a scope when link not conforms to schema"() {
        given: "scope with link that does not conform to schema"
        def scope = [
            name: "scope",
            owner: [
                targetUri: "http://localhost/units/"+unitId,
            ],
            domains: [
                (domainId): [
                    subType: "SCP_Controller",
                    status: "NEW"
                ]
            ],
            links: [
                scope_bar: [
                    [
                        target: [
                            targetUri: "http://localhost/persons/906cf5e9-91c9-4035-b25d-ea1b3ba4ca05"
                        ]
                    ]
                ]
            ]
        ]

        when: "posting the scope"
        (post("/scopes", scope))
        then: "an exception is thrown"
        JsonSchemaValidationException ex = thrown()
        and: "the reason is given"
        ex.message ==~ /.*links.scope_bar: is not defined.*/
    }

    def "can't create a control when subType not conforms to schema"() {
        given: "control with subType that does not conform to schema"
        def control = [
            name: "control",
            owner: [
                targetUri: "http://localhost/units/"+unitId,
            ],
            domains: [
                (domainId): [
                    subType: "CTL_Foo",
                    status: "NEW"
                ]
            ]
        ]

        when: "posting the scope"
        (post("/controls", control))
        then: "an exception is thrown"
        JsonSchemaValidationException ex = thrown()
        and: "the reason is given"
        ex.message ==~ /.*subType: does not have a value in the enumeration.*/
    }

    def "status is validated by schema"() {
        when: "posting a control with a sub type but null status"
        post("/controls", [
            name: "control",
            owner: [
                targetUri: "http://localhost/units/"+unitId,
            ],
            domains: [
                (domainId): [
                    subType: "CTL_TOM",
                    status: null
                ]
            ]
        ])
        then: "an exception is thrown"
        def ex = thrown(JsonSchemaValidationException)
        ex.message ==~ /.*status: null found, string expected.*/

        when: "posting a control with an invalid status"
        post("/controls", [
            name: "control",
            owner: [
                targetUri: "http://localhost/units/"+unitId,
            ],
            domains: [
                (domainId): [
                    subType: "CTL_TOM",
                    status: "CRAZY"
                ]
            ]
        ])
        then: "an exception is thrown"
        ex = thrown(JsonSchemaValidationException)
        ex.message ==~ /.*status: does not have a value in the enumeration.*/

        when: "posting a control with a valid status"
        post("/controls", [
            name: "control",
            owner: [
                targetUri: "http://localhost/units/"+unitId,
            ],
            domains: [
                (domainId): [
                    subType: "CTL_TOM",
                    status: "NEW"
                ]
            ]
        ])
        then: "no exception is thrown"
        notThrown(Exception)
    }

    def "control with custom aspect and risk value conforms to schema"() {
        given: "the control schema and a newly created control in a scope"
        def schema = getSchema("control")
        def controlId = (String)parseJson(post("/controls", [
            domains: [
                (domainId): [:]
            ],
            name: "control",
            owner: [
                targetUri: "http://localhost/units/"+unitId,
            ]])).resourceId

        post("/scopes", [
            name: "schema test scope",
            domains: [
                (domainId): [
                    riskDefinition: "DSRA"
                ]
            ],
            members: [
                [targetUri: "http://localhost/controls/$controlId"]
            ],
            owner: [
                targetUri: "http://localhost/units/"+unitId,
            ]
        ])
        def controlETag = getETag(get("/controls/$controlId"))
        put("/controls/$controlId", [
            customAspects: [
                control_generalInformation: [
                    attributes: [
                        control_generalInformation_document: "https://example.org/controls/this_one"
                    ]
                ]
            ],
            domains: [
                (domainId): [
                    riskValues: [
                        DSRA: [
                            implementationStatus: 1
                        ]
                    ]
                ]

            ],
            name: "control",
            owner: [
                targetUri: "http://localhost/units/"+unitId,
            ]
        ], ['If-Match': controlETag])
        def controlJson = parseNode(get("/controls/$controlId"))

        when: "validating the control JSON"
        def validationMessages = schema.validate(controlJson)

        then:
        validationMessages.empty
    }

    def "created document with custom aspect conforms to schema"() {
        given: "the document schema and a newly created document"
        def documentSchema = getSchema("document")
        def documentId = (String)parseJson(post("/documents", [
            name: "doc",
            owner: [
                targetUri: "http://localhost/units/"+unitId
            ],
            domains: [
                (domainId): [
                    subType: "DOC_Document",
                    status: "NEW",
                ]
            ],
            customAspects: [
                document_details: [
                    attributes: [
                        document_details_approvalDate: "2020-01-01"
                    ]
                ]
            ]
        ])).resourceId
        def createdDocumentJson = parseNode(get("/documents/$documentId"))

        when: "validating the document JSON"
        def validationMessages = documentSchema.validate(createdDocumentJson)

        then:
        validationMessages.empty
    }

    def "created incident conforms to schema"() {
        given: "the incident schema and a newly created incident"
        def incidentSchema = getSchema("incident")
        // TODO VEO-320 add custom aspect & link.
        def incidentId = (String)parseJson(post("/incidents", [
            name: "incident",
            owner: [
                targetUri: "http://localhost/units/"+unitId
            ],
            domains: [
                (domainId): [
                    subType: "INC_Incident",
                    status: "NEW",
                ]
            ],
        ])).resourceId
        def createdIncidentJson = parseNode(get("/incidents/$incidentId"))

        when: "validating the incident JSON"
        def validationMessages = incidentSchema.validate(createdIncidentJson)

        then:
        validationMessages.empty
    }

    def "created person with custom aspect conforms to schema"() {
        given: "the person schema and a newly created person"
        def personSchema = getSchema("person")
        def personId = (String)parseJson(post("/persons", [
            name: "person",
            owner: [
                targetUri: "http://localhost/units/"+unitId
            ],
            domains: [
                (domainId): [
                    subType: "PER_Person",
                    status: "NEW",
                ]
            ],
            customAspects: [
                person_address: [
                    attributes: [
                        person_address_city: "Goettingen"
                    ]
                ]
            ]
        ])).resourceId
        def createdPersonJson = parseNode(get("/persons/$personId"))

        when: "validating the process JSON"
        def validationMessages = personSchema.validate(createdPersonJson)

        then:
        validationMessages.empty
    }

    def "created process with custom aspect & links conforms to schema"() {
        given: "the process schema and a newly created process"
        def processSchema = getSchema("process")
        def scopeId = (String)parseJson(post("/scopes", [
            domains: [
                (domainId): [
                    subType: "SCP_Controller",
                    status: "NEW"
                ]
            ],
            name: "scope",
            owner: [
                targetUri: "http://localhost/units/"+unitId
            ]
        ])).resourceId
        def processId = (String)parseJson(post("/processes", [
            name: "process",
            owner: [
                targetUri: "http://localhost/units/"+unitId
            ],
            domains: [
                (domainId): [
                    subType: "PRO_DataProcessing",
                    status: "NEW",
                ]
            ],
            links: [
                process_controller: [
                    [
                        attributes: [
                            process_controller_document: "http://example.org/doc"
                        ],
                        target: [
                            targetUri: "http://localhost/scopes/$scopeId"
                        ]
                    ]
                ]
            ],
            customAspects: [
                process_accessAuthorization: [
                    attributes: [
                        process_accessAuthorization_concept: true
                    ]
                ]
            ]
        ])).resourceId
        def createdProcessJson = parseNode(get("/processes/$processId"))

        when: "validating the process JSON"
        def validationMessages = processSchema.validate(createdProcessJson)

        then:
        validationMessages.empty
    }

    def "created scenario with custom aspect conforms to schema"() {
        given: "the scenario schema and a newly created scenario"
        def scenarioSchema = getSchema("scenario")
        def scenarioId = (String)parseJson(post("/scenarios", [
            name: "scenario",
            owner: [
                targetUri: "http://localhost/units/"+unitId
            ],
            domains: [
                (domainId): [
                    subType: "SCN_Scenario",
                    status: "NEW",
                ]
            ],
            customAspects: [
                scenario_threat: [
                    attributes: [
                        scenario_threat_type: 'scenario_threat_type_malware'
                    ]
                ]
            ]
        ])).resourceId
        def createdScenarioJson = parseNode(get("/scenarios/$scenarioId"))

        when: "validating the scenario JSON"
        def validationMessages = scenarioSchema.validate(createdScenarioJson)

        then:
        validationMessages.empty
    }

    def "created scope with custom aspect, link & member conforms to schema"() {
        given: "the scope schema and a scope with one member"
        def schema = getSchema("scope")
        def memberAssetId = parseJson(post("/assets", [
            name: "member",
            owner: [
                targetUri: "http://localhost/units/"+unitId,
            ]])).resourceId
        def targetPersonId = parseJson(post("/persons", [
            name: "target",
            owner: [
                targetUri: "http://localhost/units/"+unitId,
            ],
            domains: [
                (domainId): [
                    subType: "PER_DataProtectionOfficer",
                    status: "NEW"
                ]
            ]])).resourceId
        def scopeId = parseJson(post("/scopes", [
            name: "scope",
            owner: [
                targetUri: "http://localhost/units/"+unitId,
            ],
            domains: [
                (domainId): [
                    subType: "SCP_Controller",
                    status: "NEW"
                ]
            ],
            customAspects: [
                scope_address: [
                    attributes: [
                        scope_address_city: "Goettingen"
                    ]
                ]
            ],
            links: [
                scope_dataProtectionOfficer: [
                    [
                        target: [
                            targetUri: "http://localhost/persons/$targetPersonId"
                        ]
                    ]
                ]
            ],
            members: [
                [
                    targetUri: "http://localhost/assets/$memberAssetId"
                ]
            ]
        ])).resourceId
        def scope = parseNode(get("/scopes/$scopeId"))

        when: "validating the scope JSON"
        def validationMessages = schema.validate(scope)

        then:
        validationMessages.empty
    }

    private JsonSchema getSchema(String type) {
        def schemaString = entitySchemaService.findSchema(type, client.domains)
        return JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909).getSchema(schemaString)
    }

    JsonNode parseNode(ResultActions resultActions) {
        om.readTree(resultActions.andReturn().response.contentAsString)
    }
}
