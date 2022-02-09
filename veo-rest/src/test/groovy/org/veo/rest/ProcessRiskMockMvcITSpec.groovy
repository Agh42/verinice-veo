/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2022  Alexander Koderman
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

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.support.TransactionTemplate

import com.github.JanLoebel.jsonschemavalidation.JsonSchemaValidationException

import org.veo.core.VeoMvcSpec
import org.veo.core.entity.Key
import org.veo.core.entity.exception.NotFoundException
import org.veo.persistence.access.ClientRepositoryImpl
import org.veo.persistence.access.UnitRepositoryImpl

/**
 * Test risk related functionality on scopes.
 */
@WithUserDetails("user@domain.example")
class ProcessRiskMockMvcITSpec extends VeoMvcSpec {

    @Autowired
    private ClientRepositoryImpl clientRepository
    @Autowired
    private UnitRepositoryImpl unitRepository

    @Autowired
    TransactionTemplate txTemplate

    private String unitId
    private String domainId

    def setup() {
        txTemplate.execute {
            def client = createTestClient()
            domainId = newDomain(client) {
                riskDefinitions = [
                    "default-risk-definition": createRiskDefinition("default-risk-definition"),
                    "risk-definition-for-projects": createRiskDefinition("risk-definition-for-projects"),
                ]
            }.idAsString
            unitId = unitRepository.save(newUnit(client)).idAsString
            clientRepository.save(client)
        }
    }

    def "Get on nonexistent risk returns error code 404"() {

        given: "a process and scenario are created but no risk"
        def processId = parseJson(post("/processes", [
            domains: [
                (domainId): [:]
            ],
            name: "risk test process",
            owner: [targetUri: "http://localhost/units/$unitId"]
        ])).resourceId

        def scenarioId = parseJson(post("/scenarios", [
            name: "process risk test scenario",
            owner: [targetUri: "http://localhost/units/$unitId"],
            domains: [
                (domainId): [:]
            ]
        ])).resourceId

        when: "trying to get nonexistent risk returns 404"
        get("/processes/$processId/risks/$scenarioId", 404)

        then:
        thrown(NotFoundException)
    }

    def "Getting risk on nonexistent process returns 404"() {

        given: "a scenario is created but no process"
        def scenarioId = parseJson(post("/scenarios", [
            name: "process risk test scenario",
            owner: [targetUri: "http://localhost/units/$unitId"],
            domains: [
                (domainId): [:]
            ]
        ])).resourceId
        def randomUuid = UUID.randomUUID()

        when: "trying to get risk from nonexistent entity returns 404"
        get("/processes/$randomUuid/risks/$scenarioId", 404)

        then:
        thrown(NotFoundException)
    }

    def "Getting risk on nonexistent scenario returns 404"() {

        given: "a process is created"
        def processId = parseJson(post("/processes", [
            domains: [
                (domainId): [:]
            ],
            name: "risk test process",
            owner: [targetUri: "http://localhost/units/$unitId"]
        ])).resourceId
        def randomUuid = UUID.randomUUID()

        when: "trying to get nonexistent risk returns 404"
        get("/processes/$processId/risks/$randomUuid", 404)

        then:
        thrown(NotFoundException)
    }
}

xxx


/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2022  Urs Zeidler.
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
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.util.NestedServletException

import org.veo.core.VeoMvcSpec
import org.veo.core.entity.riskdefinition.ImplementationStateDefinition
import org.veo.persistence.access.ClientRepositoryImpl
import org.veo.persistence.access.UnitRepositoryImpl

/**
 * Test risk related functionality on controls.
 */
@WithUserDetails("user@domain.example")
class ProcessRiskMockMvcITSpec extends VeoMvcSpec {

    @Autowired
    private ClientRepositoryImpl clientRepository
    @Autowired
    private UnitRepositoryImpl unitRepository

    @Autowired
    TransactionTemplate txTemplate

    private String unitId
    private String domainId

    def setup() {
        txTemplate.execute {
            def client = createTestClient()
            def domain = newDomain(client) {
                riskDefinitions = [
                    "myFirstRiskDefinition": createRiskDefinition("myFirstRiskDefinition"),
                    "mySecondRiskDefinition": createRiskDefinition("mySecondRiskDefinition"),
                    "myThirdRiskDefinition": createRiskDefinition("myThirdRiskDefinition")
                ]
            }
            domainId = domain.idAsString
            unitId = unitRepository.save(newUnit(client)).idAsString
            clientRepository.save(client)
        }
    }

    def "can create and update process impact"() {
        when: "creating a process with risk values for different risk definitions"
        def processId = parseJson(post("/processes", [
            name: "Super PRO",
            owner: [targetUri: "http://localhost/units/$unitId"],
            domains: [
                (domainId): [
                    riskValues: [
                        myFirstRiskDefinition : [
                            riskValues: [ "C": "0",
                                "I": "1"
                            ]
                        ],
                        mySecondRiskDefinition : [
                            riskValues: [
                                "C": "1"
                            ]
                        ]
                    ]
                ]
            ]
        ])).resourceId

        and: "retrieving it"
        def getProcessResponse = get("/processes/$processId")
        def processETag = getETag(getProcessResponse)
        def retrievedProcess = parseJson(getProcessResponse)

        then: "the retrieved risk values are complete"
        retrievedProcess.domains[domainId].riskValues.myFirstRiskDefinition.riskValues.size() == 2
        retrievedProcess.domains[domainId].riskValues.myFirstRiskDefinition.riskValues.C == "0"
        retrievedProcess.domains[domainId].riskValues.myFirstRiskDefinition.riskValues.I == "1"
        retrievedProcess.domains[domainId].riskValues.mySecondRiskDefinition.riskValues.C == "1"

        when: "updating the risk values on the process"
        put("/processes/$processId", [
            name: "Super PRO1",
            owner: [targetUri: "http://localhost/units/$unitId"],
            domains: [
                (domainId): [
                    riskValues: [
                        myFirstRiskDefinition : [
                            riskValues: [ "C": "1",
                                "I": "2"
                            ]
                        ],
                        myThirdRiskDefinition : [
                            riskValues: [ "C": "1",
                                "I": "2"
                            ]
                        ]
                    ]
                ]
            ]
        ], ['If-Match': processETag])

        and: "retrieving it again"
        def updatedProcess = parseJson(get("/processes/$processId"))

        then: "the changes have been applied"
        updatedProcess.domains[domainId].riskValues.myFirstRiskDefinition.riskValues.C == "1"
        updatedProcess.domains[domainId].riskValues.mySecondRiskDefinition == null
        updatedProcess.domains[domainId].riskValues.myThirdRiskDefinition.riskValues.C == "1"
        updatedProcess.domains[domainId].riskValues.myThirdRiskDefinition.riskValues.I == "2"
    }

    def "can't create process with wrong riskdefinition id"() {
        when: "creating a process with risk values for different risk definitions"
        def processId = parseJson(post("/processes", [
            name: "Super PRO wrong",
            owner: [targetUri: "http://localhost/units/$unitId"],
            domains: [
                (domainId): [
                    riskValues: [
                        myFirstWrongDefinition : [
                            riskValues: [ "E": "0",
                                "GGG": "1"
                            ]
                        ],
                        mySecondRiskDefinition : [
                            riskValues: [
                                "C": "1"
                            ]
                        ]
                    ]
                ]
            ]
        ],false)).resourceId

        then: "an exception is thrown"
        IllegalArgumentException ex = thrown()
        ex.message == 'Undefined Risk definitions: myFirstWrongDefinition'
    }

    def "can't create process with wrong impact"() {
        when: "creating a process with risk values for different risk definitions"
        def processId = parseJson(post("/processes", [
            name: "Super PRO wrong",
            owner: [targetUri: "http://localhost/units/$unitId"],
            domains: [
                (domainId): [
                    riskValues: [
                        myFirstRiskDefinition : [
                            riskValues: [ "E": "0",
                                "GGG": "1"
                            ]
                        ],
                        mySecondRiskDefinition : [
                            riskValues: [
                                "C": "1"
                            ]
                        ]
                    ]
                ]
            ]
        ],false)).resourceId

        then: "an exception is thrown"
        IllegalArgumentException ex = thrown()
        ex.message == "Category: 'E' not defined in myFirstRiskDefinition"
    }

    def "can't create process with wrong impact value"() {
        when: "creating a process with risk values for different risk definitions"
        def processId = parseJson(post("/processes", [
            name: "Super PRO wrong",
            owner: [targetUri: "http://localhost/units/$unitId"],
            domains: [
                (domainId): [
                    riskValues: [
                        mySecondRiskDefinition : [
                            riskValues: [
                                "C": "10"
                            ]
                        ]
                    ]
                ]
            ]
        ],false)).resourceId

        then: "an exception is thrown"
        IllegalArgumentException ex = thrown()
        ex.message == "Impact: '10' not defined in mySecondRiskDefinition"
    }
}
