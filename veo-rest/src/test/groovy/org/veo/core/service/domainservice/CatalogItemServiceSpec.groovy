/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2021  Urs Zeidler.
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
package org.veo.core.service.domainservice

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithUserDetails

import org.veo.adapter.service.domaintemplate.DomainTemplateServiceImpl
import org.veo.core.VeoSpringSpec
import org.veo.core.entity.Client
import org.veo.core.entity.Domain
import org.veo.core.entity.Unit
import org.veo.core.entity.profile.ProfileRef
import org.veo.core.repository.PagingConfiguration
import org.veo.core.service.CatalogItemService
import org.veo.persistence.access.ClientRepositoryImpl
import org.veo.persistence.access.ControlRepositoryImpl
import org.veo.persistence.access.UnitRepositoryImpl

@WithUserDetails("user@domain.example")
class CatalogItemServiceSpec extends VeoSpringSpec {
    @Autowired
    private ClientRepositoryImpl repository

    @Autowired
    private UnitRepositoryImpl unitRepository

    @Autowired
    DomainTemplateServiceImpl domainTemplateService

    @Autowired
    CatalogItemService catalogItemService

    @Autowired
    private ControlRepositoryImpl repo

    private Client client
    private Unit unit
    private Unit unit1
    private Domain testDomain
    private Domain dsgvoDomain

    def item
    def element

    public setup () {
        txTemplate.execute {
            client = repository.save(newClient())
            testDomain = createTestDomain(client, TEST_DOMAIN_TEMPLATE_ID)
            dsgvoDomain = createTestDomain(client, DSGVO_DOMAINTEMPLATE_UUID)
            client = repository.save(client)
            unit = unitRepository.save(newUnit(client) {
                name = "Test unit"
            })
        }
        item = testDomain.catalogs.first().catalogItems.sort({it.namespace}).first()
        element = catalogItemService.createInstance(item, testDomain)
    }

    def "retrieve example elements for default client"() {
        when: "retrieving the example elements for a client"
        def elements = domainTemplateService.getProfileElements(dsgvoDomain, new ProfileRef("exampleOrganization"))

        then: "the elements for all the client's units are returned"
        elements.size() == 9
    }

    def "create an element from a catalog item"() {
        given: "a client and a domain"

        expect: "the element is created and initialized"
        element.name == "Control-1"
        element.abbreviation == item.element.abbreviation
        element.description == item.element.description
        element.appliedCatalogItems.size() == 1
        element.appliedCatalogItems.contains(item)
        element.domains.size() == 1
        element.domains.contains(testDomain)

        when: "we take another item"
        item = testDomain.catalogs.first().catalogItems.sort({it.namespace})[2]
        element = catalogItemService.createInstance(item, testDomain)

        then: "the element is created and initialized"
        element.name == "Control-3"
        element.abbreviation == item.element.abbreviation
        element.description == item.element.description
        element.appliedCatalogItems.size() == 1
        element.appliedCatalogItems.contains(item)
        element.domains.size() == 1
        element.domains.contains(testDomain)
    }

    def "find an applied item"() {
        given: "a client"
        txTemplate.execute {
            unit1 = unitRepository.save(newUnit(client) {
                name = "Test unit1"
            })
        }

        expect: "the element is created and initalized"
        element.name == "Control-1"
        element.abbreviation == item.element.abbreviation
        element.description == item.element.description
        element.appliedCatalogItems.size() == 1
        element.appliedCatalogItems.contains(item)
        element.domains.size() == 1
        element.domains.contains(testDomain)

        when: "the element is saved and searched for by applieditems"
        element.setOwner(unit)
        element.setDesignator("CTL-1")
        repo.save(element)
        def result = executeInTransaction {
            repo.query(client).with {
                whereOwnerIs(unit)
                whereAppliedItemsContain([item])
                fetchAppliedCatalogItems()
                execute(PagingConfiguration.UNPAGED)
            }
        }

        then:"the element is found"
        result.resultPage.size() == 1
        result.resultPage.first().appliedCatalogItems.contains(item)

        when: "searched in another unit"
        result = repo.query(client).with {
            whereOwnerIs(unit1)
            whereAppliedItemsContain([item])
            execute(PagingConfiguration.UNPAGED)
        }

        then:"the result is empty"
        result.resultPage.size() == 0
    }
}
