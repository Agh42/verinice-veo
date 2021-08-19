/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2021  Jochen Kemnade.
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
package org.veo.core

import javax.transaction.Transactional
import javax.validation.ConstraintViolationException

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

import org.veo.core.entity.CatalogItem
import org.veo.core.entity.Client
import org.veo.persistence.access.CatalogItemRepositoryImpl
import org.veo.persistence.access.ClientRepositoryImpl
import org.veo.persistence.access.UnitRepositoryImpl
import org.veo.persistence.entity.jpa.CatalogItemData

@SpringBootTest(classes = CatalogItemRepositoryITSpec.class)
@Transactional()
class CatalogItemRepositoryITSpec extends VeoSpringSpec {


    @Autowired
    private ClientRepositoryImpl clientRepository
    @Autowired
    private UnitRepositoryImpl unitRepository

    @Autowired
    private CatalogItemRepositoryImpl catalogItemRepository

    def "load a catalog item"() {
        given: "a client with a catalog containing an item"
        Client client = newClient()
        def domain = newDomain(client)
        def catalog = newCatalog(domain)
        newCatalogItem(catalog) {
            newControl(it) {
                name = 'Control 1'
            }
        }
        client = clientRepository.save(client)
        def itemId = client.domains.first().catalogs.first().catalogItems.first().id

        when:
        def item = catalogItemRepository.findById(itemId)

        then:
        item.present
        item.get().element.name == 'Control 1'
    }

    def "a catalog item cannot be saved without an associated element"() {
        given: "a client with a catalog containing an item"
        def client = clientRepository.save(newClient())
        newDomain(client)
        client = clientRepository.save(client)
        def catalog = catalogDataRepository.save(newCatalog(client.domains.first()))

        when:
        CatalogItem catalogItem = new CatalogItemData()
        catalogItem.setCatalog(catalog)
        catalogItemRepository.save(catalogItem)

        then:
        ConstraintViolationException ex = thrown(ConstraintViolationException)
        ex.getConstraintViolations().first().propertyPath ==~ /element/
        ex.getConstraintViolations().first().getMessageTemplate() ==~ /.*NotNull.message.*/
    }

    def "a client cannot be saved with an invalid catalog item"() {
        given: "a client with a catalog containing an item"
        Client client = newClient()
        def domain = newDomain(client)
        def catalog = newCatalog(domain)
        def catalogItem = newCatalogItem(catalog) {
            newControl(it) {
                name = 'Control 1'
            }
        }
        catalogItem.element = null

        when: "the client is saved"
        clientRepository.save(client)

        then: "the validation cascades down to the invalid item"
        ConstraintViolationException ex = thrown(ConstraintViolationException)
        ex.getConstraintViolations().first().propertyPath ==~ /domains\[].catalogs\[].catalogItems\[].element/
        ex.getConstraintViolations().first().getMessageTemplate() ==~ /.*NotNull.message.*/
    }
}
