/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2021  Jonas Jordan
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

import org.veo.core.VeoMvcSpec
import org.veo.core.repository.ClientRepository
import org.veo.core.repository.DocumentRepository
import org.veo.core.repository.UnitRepository
import org.veo.core.usecase.unit.CreateDemoUnitUseCase

@WithUserDetails("admin")
class AdminControllerMvcITSpec extends VeoMvcSpec {

    @Autowired
    private ClientRepository clientRepo
    @Autowired
    private UnitRepository unitRepo
    @Autowired
    private DocumentRepository documentRepo
    @Autowired
    private CreateDemoUnitUseCase createDemoUnitUseCase

    def "deletes client"() {
        given: "a client with some units and a document"
        def client = clientRepo.save(newClient {})
        def unit1 = unitDataRepository.save(newUnit(client))
        def unit2 = unitDataRepository.save(newUnit(client))
        def document = documentRepo.save(newDocument(unit1))

        when: "deleting the client"
        delete("/admin/client/${client.id.uuidValue()}")

        then:
        !clientRepo.exists(client.id)
        !unitRepo.exists(unit1.id)
        !unitRepo.exists(unit2.id)
        !documentRepo.exists(document.id)
    }

    def "generates unit dump"() {
        given: "a unit with a bunch of elements and risks"
        def client = createTestClient()
        createTestDomain(client, TEST_DOMAIN_TEMPLATE_ID)
        createTestDomain(client, DSGVO_TEST_DOMAIN_TEMPLATE_ID)
        def unitId = parseJson(post("/units", [name: "you knit"])).resourceId
        def domainId = parseJson(get("/domains")).first().id
        def owner = [targetUri: "http://localhost/units/$unitId"]

        def assetId = parseJson(post("/assets", [
            domains: [
                (domainId): [:]
            ],
            name: "asset",
            owner: owner
        ])).resourceId
        post("/controls", [
            name: "control",
            owner: owner
        ])
        post("/documents", [
            name: "document",
            owner: owner
        ])
        post("/incidents", [
            name: "incident",
            owner: owner
        ])
        post("/persons", [
            name: "person",
            owner: owner
        ])
        def processId = parseJson(post("/processes", [
            domains: [
                (domainId): [:]
            ],
            name: "process",
            owner: owner
        ])).resourceId
        def scenarioId = parseJson(post("/scenarios", [
            name: "scenario",
            owner: owner
        ])).resourceId
        post("/scopes", [
            name: "scope",
            owner: owner
        ])

        post("/assets/$assetId/risks", [
            domains: [
                [targetUri: "http://localhost/domains/$domainId"]
            ],
            scenario: [targetUri: "http://localhost/scenarios/$scenarioId"]
        ])
        post("/processes/$processId/risks", [
            domains: [
                [targetUri: "http://localhost/domains/$domainId"]
            ],
            scenario: [targetUri: "http://localhost/scenarios/$scenarioId"]
        ])

        when: "requesting a unit dump"
        def dump = parseJson(get("/admin/unit-dump/$unitId"))

        then: "it contains the unit and all its elements"
        with(dump) {
            unit.name == "you knit"
            domains.size == 2
            elements*.type.sort() == [
                "asset",
                "control",
                "document",
                "incident",
                "person",
                "process",
                "scenario",
                "scope"
            ]
            risks*._self.sort() == [
                "http://localhost/assets/$assetId/risks/$scenarioId",
                "http://localhost/processes/$processId/risks/$scenarioId"
            ]
        }
    }

    def "update client domains"() {
        given: "a client with some units and a document"
        def client = createTestClient()
        createTestDomain(client, DSGVO_DOMAINTEMPLATE_UUID)
        createTestDomain(client, DSGVO_TEST_DOMAIN_TEMPLATE_ID)
        def demoUnit = createDemoUnitUseCase.execute(new CreateDemoUnitUseCase.InputData(client.id)).unit
        when: 'updating all clients'
        post("/admin/domaintemplates/${DSGVO_TEST_DOMAIN_TEMPLATE_ID}/allclientsupdate", [:], 204)
        then: 'the demo unit is transferred to the new domain'
        with(parseJson(get("/admin/unit-dump/${demoUnit.idAsString}"))) {
            domains.size() == 1
            domains.first().name == 'DSGVO-test'
            elements.each {
                it.domains.keySet() == ['DSGVO-test']
                it.customAspects.each { type, ca ->
                    ca.domains*.displayName == ['DSGVO-test']
                }
                it.links.each { type, linksOfType->
                    linksOfType.each {
                        it.domains*.displayName == ['DSGVO-test']
                    }
                }
            }
        }
    }
}
