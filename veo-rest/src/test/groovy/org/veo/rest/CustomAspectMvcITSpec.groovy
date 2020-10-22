/*******************************************************************************
 * Copyright (c) 2020 Jochen Kemnade.
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

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.transaction.support.TransactionTemplate

import org.veo.core.VeoMvcSpec
import org.veo.core.entity.Key
import org.veo.core.usecase.repository.UnitRepository
import org.veo.persistence.access.ClientRepositoryImpl
import org.veo.rest.configuration.WebMvcSecurityConfiguration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [WebMvcSecurityConfiguration])
@WithUserDetails("user@domain.example")
class CustomAspectMvcITSpec extends VeoMvcSpec {

    @Autowired
    ClientRepositoryImpl clientRepository

    @Autowired
    UnitRepository unitRepository

    @Autowired
    TransactionTemplate txTemplate

    String unitId

    def setup() {
        txTemplate.execute {
            def client = clientRepository.save(newClient {
                id = Key.uuidFrom(WebMvcSecurityConfiguration.TESTCLIENT_UUID)
            })
            unitId = unitRepository.save(newUnit(client)).id.uuidValue()
        }
    }

    def "save multiple links"() {
        when: "creating a person with different aspects"
        def personId = parseJson(post("/persons", [
            customAspects: [
                PersonCommons: [
                    applicableTo: [
                        "person"
                    ],
                    attributes: [
                        personIsoAnrede: "personIsoAnredeHerr",
                        personIsoSurname: "Mick"
                    ],
                    domains: [],
                ],
                PersonInfosDsb: [
                    applicableTo: [
                        "person"
                    ],
                    attributes: [
                        fachkunde: true
                    ],
                    domains: [],
                ]
            ],
            name : "person",
            owner: [targetUri: "/units/$unitId"]
        ])).resourceId
        def retrievedPerson = parseJson(get("/persons/$personId"))

        then:
        with(retrievedPerson.customAspects) {
            PersonCommons.attributes.personIsoAnrede == "personIsoAnredeHerr"
            PersonCommons.attributes.personIsoSurname == "Mick"
            PersonInfosDsb.attributes.fachkunde == true
        }
    }
}
