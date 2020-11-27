/*******************************************************************************
 * Copyright (c) 2020 Alexander Ben Nasrallah.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.veo.rest

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

import org.apache.commons.codec.digest.DigestUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.transaction.support.TransactionTemplate

import org.veo.core.VeoMvcSpec
import org.veo.core.entity.Domain
import org.veo.core.entity.Key
import org.veo.core.entity.Unit
import org.veo.core.usecase.common.ETag
import org.veo.persistence.access.ClientRepositoryImpl
import org.veo.persistence.access.ScenarioRepositoryImpl
import org.veo.persistence.access.UnitRepositoryImpl
import org.veo.rest.configuration.WebMvcSecurityConfiguration

import groovy.json.JsonSlurper

/**
 * Integration test for the scenario controller. Uses mocked spring MVC environment.
 * Uses JPA repositories with in-memory database.
 * Does not start an embedded server.
 * Uses a test Web-MVC configuration with example accounts and clients.
 */
@SpringBootTest(
webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
classes = [WebMvcSecurityConfiguration]
)
@EnableAsync
@ComponentScan("org.veo.rest")
class ScenarioControllerMockMvcITSpec extends VeoMvcSpec {

    @Autowired
    private ClientRepositoryImpl clientRepository
    @Autowired
    private UnitRepositoryImpl unitRepository

    @Autowired
    private ScenarioRepositoryImpl scenarioRepository

    @Autowired
    TransactionTemplate txTemplate

    private Unit unit
    private Domain domain
    private Domain domain1
    private Key clientId = Key.uuidFrom(WebMvcSecurityConfiguration.TESTCLIENT_UUID)
    String salt = "salt-for-etag"

    def setup() {
        txTemplate.execute {
            domain = newDomain {
                abbreviation = "D"
                name = "Domain"
            }

            domain1 = newDomain {
                abbreviation = "D1"
                name = "Domain 1"
            }

            def client= newClient {
                id = clientId
                domains = [domain, domain1] as Set
            }

            unit = newUnit(client) {
                name = "Test unit"
            }

            unit.client = client
            clientRepository.save(client)
            unitRepository.save(unit)
        }
        ETag.setSalt(salt)
    }


    @WithUserDetails("user@domain.example")
    def "create an scenario"() {
        given: "a request body"

        Map request = [
            name: 'New Scenario',
            owner: [
                displayName: 'scenarioDataProtectionObjectivesEugdprEncryption',
                targetUri: '/units/' + unit.id.uuidValue()
            ]
        ]

        when: "a request is made to the server"

        def results = post('/scenarios', request)

        then: "the scenario is created and a status code returned"
        results.andExpect(status().isCreated())

        and: "the location of the new scenario is returned"
        def result = new JsonSlurper().parseText(results.andReturn().response.contentAsString)
        result.success == true
        def resourceId = result.resourceId
        resourceId != null
        resourceId != ''
        result.message == 'Scenario created successfully.'
    }

    @WithUserDetails("user@domain.example")
    def "retrieve an scenario"() {
        given: "a saved scenario"
        def scenario = txTemplate.execute {
            scenarioDataRepository.save(newScenario(unit) {
                name = 'Test scenario-1'
            })
        }


        when: "a request is made to the server"
        def results = get("/scenarios/${scenario.id.uuidValue()}")
        String expectedETag = DigestUtils.sha256Hex(scenario.id.uuidValue() + "_" + salt + "_" + Long.toString(scenario.getVersion()))

        then: "the scenario is found"
        results.andExpect(status().isOk())
        and: "the eTag is set"
        String eTag = results.andReturn().response.getHeader("ETag")
        eTag != null
        getTextBetweenQuotes(eTag).equals(expectedETag)
        and:
        def result = new JsonSlurper().parseText(results.andReturn().response.contentAsString)
        result.name == 'Test scenario-1'
        result.owner.targetUri == "http://localhost/units/"+unit.id.uuidValue()
    }

    @WithUserDetails("user@domain.example")
    def "retrieve all scenarios for a unit"() {
        given: "saved scenarios"
        def scenario = newScenario(unit) {
            name = 'Test scenario-1'
        }
        def scenario2 = newScenario(unit) {
            name = 'Test scenario-2'
        }
        (scenario, scenario2) = txTemplate.execute {
            [scenario, scenario2].collect(scenarioDataRepository.&save)
        }

        when: "a request is made to the server"
        def results = get("/scenarios?unit=${unit.id.uuidValue()}")

        then: "the scenarios are returned"
        results.andExpect(status().isOk())
        when:
        def result = new JsonSlurper().parseText(results.andReturn().response.contentAsString)
        then:
        result.size == 2

        result.sort{it.name}.first().name == 'Test scenario-1'
        result.sort{it.name}.first().owner.targetUri == "http://localhost/units/"+unit.id.uuidValue()
        result.sort{it.name}[1].name == 'Test scenario-2'
        result.sort{it.name}[1].owner.targetUri == "http://localhost/units/"+unit.id.uuidValue()
    }

    @WithUserDetails("user@domain.example")
    def "retrieving all scenarios for a unit does not return groups"() {
        given: "a saved scenario and a saved scenario group"

        def scenario = newScenario(unit) {
            name = 'Test scenario-1'
        }

        def scenarioGroup = newScenarioGroup(unit) {
            name = 'Group 1'
        }

        txTemplate.execute {
            [scenario, scenarioGroup].collect(scenarioDataRepository.&save)
        }

        when: "a request is made to the server"
        def results = get("/scenarios?unit=${unit.id.uuidValue()}")

        then: "the scenarios are returned"
        results.andExpect(status().isOk())
        when:
        def result = new JsonSlurper().parseText(results.andReturn().response.contentAsString)
        then:
        result.size == 1
        result.first().name == 'Test scenario-1'
    }

    @WithUserDetails("user@domain.example")
    def "put an scenario"() {
        given: "a saved scenario"
        def scenario = txTemplate.execute {
            scenarioDataRepository.save(newScenario(unit) {
                domains = [domain1] as Set
            })
        }

        Map request = [
            name: 'New scenario-2',
            abbreviation: 'u-2',
            description: 'desc',
            owner:
            [
                targetUri: '/units/'+unit.id.uuidValue(),
                displayName: 'test unit'
            ],  domains: [
                [
                    targetUri: '/domains/'+domain.id.uuidValue(),
                    displayName: 'test ddd'
                ]
            ]
        ]


        when: "a request is made to the server"
        Map headers = [
            'If-Match': ETag.from(scenario.id.uuidValue(), 1)
        ]
        def results = put("/scenarios/${scenario.id.uuidValue()}", request, headers)

        then: "the scenario is found"
        results.andExpect(status().isOk())
        def result = new JsonSlurper().parseText(results.andReturn().response.contentAsString)
        result.name == 'New scenario-2'
        result.abbreviation == 'u-2'
        result.domains.first().displayName == domain.abbreviation+" "+domain.name
        result.owner.targetUri == "http://localhost/units/"+unit.id.uuidValue()
    }

    @WithUserDetails("user@domain.example")
    def "delete an scenario"() {

        given: "an existing scenario"
        def scenario = txTemplate.execute {
            scenarioDataRepository.save(newScenario(unit))
        }


        when: "a delete request is sent to the server"

        def results = delete("/scenarios/${scenario.id.uuidValue()}")

        then: "the scenario is deleted"
        results.andExpect(status().isOk())
        scenarioRepository.findById(scenario.id).empty
    }

    @WithUserDetails("user@domain.example")
    def "can't put an scenario with another scenario's ID"() {
        given: "two scenarios"
        def scenario1 = txTemplate.execute({
            scenarioDataRepository.save(newScenario(unit, {
                name = "old name 1"
            }))
        })
        def scenario2 = txTemplate.execute({
            scenarioDataRepository.save(newScenario(unit, {
                name = "old name 2"
            }))
        })
        when: "a put request tries to update scenario 1 using the ID of scenario 2"
        Map headers = [
            'If-Match': ETag.from(scenario1.id.uuidValue(), 1)
        ]
        put("/scenarios/${scenario2.id.uuidValue()}", [
            id: scenario1.id.uuidValue(),
            name: "new name 1",
            owner: [targetUri: '/units/' + unit.id.uuidValue()]
        ], headers, false)
        then: "an exception is thrown"
        thrown(DeviatingIdException)
    }

    @WithUserDetails("user@domain.example")
    def "can put back scenario"() {
        given: "a new scenario"
        def id = parseJson(post("/scenarios/", [
            name: "new name",
            owner: [targetUri: "/units/"+unit.id.uuidValue()]
        ])).resourceId
        def getResult = get("/scenarios/$id")

        expect: "putting the retrieved scenario back to be successful"
        put("/scenarios/$id", parseJson(getResult), [
            "If-Match": getTextBetweenQuotes(getResult.andReturn().response.getHeader("ETag"))
        ])
    }
}