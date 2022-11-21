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
package org.veo.message

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithUserDetails
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper

import org.veo.core.VeoSpringSpec
import org.veo.core.entity.Key
import org.veo.core.repository.ClientRepository
import org.veo.core.repository.PagingConfiguration
import org.veo.core.repository.PersonRepository
import org.veo.core.repository.ProcessRepository
import org.veo.core.repository.ScopeRepository
import org.veo.core.usecase.common.NameableInputData
import org.veo.core.usecase.unit.CreateUnitUseCase
import org.veo.persistence.access.jpa.StoredEventDataRepository
import org.veo.rest.configuration.WebMvcSecurityConfiguration

class VersioningMessageITSpec extends VeoSpringSpec {
    private clientId = Key.uuidFrom(WebMvcSecurityConfiguration.TESTCLIENT_UUID)

    def setup() {
        createTestDomainTemplate(DSGVO_DOMAINTEMPLATE_UUID)
    }

    @Autowired
    CreateUnitUseCase createUnitUseCase

    @Autowired
    StoredEventDataRepository storedEventRepository

    @Autowired
    private ClientRepository clientRepository

    @Autowired
    PersonRepository personRepository

    @Autowired
    ProcessRepository processRepository

    @Autowired
    ScopeRepository scopeRepository

    @WithUserDetails("user@domain.example")
    def "creation messages produced for client creation with demo unit"() {
        when: "creating a client with a demo unit"
        executeInTransaction {
            createUnitUseCase.execute(new CreateUnitUseCase.InputData(
                    new NameableInputData(Optional.empty(), "non-demo-unit", "ndu", "whatever"),
                    clientId, Optional.empty()
                    ))
        }

        and: "fetching all messages"
        def messages = storedEventRepository.findAll()
                .findAll { it.routingKey.contains("versioning_event") }
                .collect { new ObjectMapper().readValue(it.content, Map.class) }

        then: "no message with uri null"
        messages.every {it.uri != null}

        and: "there is one creation message for each person"
        def persons = personRepository.query(clientRepository.findById(clientId).get()).execute(PagingConfiguration.UNPAGED).resultPage
        persons.size() > 0
        persons.forEach({ person ->
            def elementMessages = messages.findAll { it.uri?.endsWith("/persons/${person.idAsString}") }
            assert elementMessages.size() == 1
            elementMessages.first().with{
                assert type == "CREATION"
                assert changeNumber == 0
                assert content.designator.contains("DMO-")
            }
        })

        and: "there is one creation message for each process"
        def processes = processRepository.query(clientRepository.findById(clientId).get()).execute(PagingConfiguration.UNPAGED).resultPage
        processes.size() > 0
        processes.forEach({ process ->
            def elementMessages = messages.findAll { it.uri?.endsWith("/processes/${process.idAsString}") }
            assert elementMessages.size() == 2

            def creation = elementMessages.find{it.type == "CREATION"}
            with(creation) {
                changeNumber == 0
                content.designator.contains("DMO-")
                time != null
            }

            def modification = elementMessages.find{it.type == "MODIFICATION"}
            with(modification) {
                changeNumber == 1
                content.designator.contains("DMO-")
                time > creation.time
            }
        })

        and: "there is one creation message for each scope"
        def scopes = scopeRepository.query(clientRepository.findById(clientId).get()).execute(PagingConfiguration.UNPAGED).resultPage
        scopes.size() > 0
        scopes.forEach({ scope ->
            def elementMessages = messages.findAll { it.uri?.endsWith("/scopes/${scope.idAsString}") }
            assert elementMessages.size() == 1
            elementMessages.first().with{
                assert type == "CREATION"
                assert changeNumber == 0
                assert content.designator.contains("DMO-")
            }
        })

        and: "there is one creation message for each catalog item"
        var catalogItems = txTemplate.execute {
            domainDataRepository.findAllActiveByClient(clientId.uuidValue())
                    .collectMany { it.catalogs }
                    .collectMany { it.catalogItems }
        }
        catalogItems.size() > 0
        catalogItems.forEach({ item ->
            def itemMessages = messages.findAll { it.uri?.contains("/items/$item.idAsString") }
            assert itemMessages.size() == 1

            with(itemMessages.first()) {
                type == "CREATION"
                changeNumber == 0
                time != null
            }
        })
    }
}
