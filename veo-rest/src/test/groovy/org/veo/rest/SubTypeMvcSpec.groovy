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

import org.veo.core.VeoMvcSpec
import org.veo.core.repository.UnitRepository
import org.veo.persistence.access.ClientRepositoryImpl
import org.veo.persistence.access.DomainRepositoryImpl

@WithUserDetails("user@domain.example")
class SubTypeMvcSpec extends VeoMvcSpec {

    @Autowired
    ClientRepositoryImpl clientRepository

    @Autowired
    DomainRepositoryImpl domainRepository

    @Autowired
    UnitRepository unitRepository

    String domainId
    String unitId

    def setup() {
        txTemplate.execute {
            def client = createTestClient()
            def domain = createTestDomain(client, DSGVO_TEST_DOMAIN_TEMPLATE_ID)
            domainId = domain.id.uuidValue()
            unitId = unitRepository.save(newUnit(client)).id.uuidValue()
        }
    }

    def 'write and retrieve an entity with a sub type'() {
        when: "saving a process with the sub type VT"
        def processId = parseJson(post("/processes", [
            name: "Verarbeitungstaetigkeit",
            owner: [
                targetUri: "http://localhost/units/$unitId"
            ],
            domains: [
                (domainId): [
                    "subType": "PRO_DataProcessing",
                    "status": "NEW"
                ]
            ]
        ])).resourceId
        then: "it is saved"
        processId != null

        when: "retrieving the sub type"
        def retrievedProcess = parseJson(get("/processes/$processId"))
        then: "the sub type is present"
        retrievedProcess.domains[domainId].subType == "PRO_DataProcessing"
        retrievedProcess.domains[domainId].status == "NEW"
    }
}
