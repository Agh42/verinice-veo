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
package org.veo.core

import org.springframework.beans.factory.annotation.Autowired

import org.veo.core.entity.Domain
import org.veo.core.entity.TailoringReferenceType
import org.veo.core.entity.definitions.CustomAspectDefinition
import org.veo.core.entity.definitions.LinkDefinition
import org.veo.core.repository.CatalogRepository
import org.veo.core.repository.DomainRepository
import org.veo.service.CatalogMigrationService

class CatalogMigrationServiceITSpec extends VeoSpringSpec{
    @Autowired
    CatalogRepository catalogRepository

    @Autowired
    DomainRepository domainRepository

    @Autowired
    CatalogMigrationService catalogItemMigrationService

    Domain domain


    def setup() {
        def client = createTestClient()
        domain = domainRepository.save(newDomain(client) {domain ->
            elementTypeDefinitions = [
                newElementTypeDefinition("document", domain) {
                    customAspects = [
                        file: new CustomAspectDefinition().tap{
                            attributeSchemas = [
                                extension: [
                                    enum: ["pdf", "md", "txt"]
                                ]
                            ]
                        }
                    ]
                    links = [
                        author: new LinkDefinition().tap{
                            targetType = "person"
                        }
                    ]
                }
            ]
        })
    }

    def "invalid custom aspect attribute is removed"() {
        given:
        def catalogId = catalogRepository.save(newCatalog(domain) {
            catalogItems = [
                newCatalogItem(it, {
                    newDocument(it) {
                        customAspects = [
                            newCustomAspect("file") {
                                attributes = [
                                    extension: "pdf",
                                    size: 5000,
                                ]
                            }
                        ]
                    }
                }) {
                    it.namespace = "routerManual"
                }
            ]
        }).id

        when:
        executeInTransaction {
            catalogItemMigrationService.migrate(domain.getElementTypeDefinition("document"), domain)
        }

        and:
        def catalog = executeInTransaction {
            catalogRepository.findById(catalogId).get().tap{
                it.catalogItems*.element*.customAspects*.attributes
            }
        }

        then:
        with(catalog.catalogItems.find{it.namespace == "routerManual"}) {
            def attributes = element.customAspects.find{it.type == "file"}.attributes
            attributes.extension == "pdf"
            attributes.size == null
        }
    }

    def "link reference with invalid target is removed"() {
        given:
        def catalogId = catalogRepository.save(newCatalog(domain) {
            def manualAuthor = newCatalogItem(it, {newPerson(it)}) {
                it.namespace = "manualAuthor"
            }
            def randomAsset = newCatalogItem(it, {newAsset(it)}) {
                it.namespace = "randomAsset"
            }
            def routerManual = newCatalogItem(it, {newDocument(it)}) {
                it.namespace = "routerManual"
                it.tailoringReferences = [
                    newLinkTailoringReference(it, TailoringReferenceType.LINK) {
                        it.linkType = "author"
                        it.catalogItem = manualAuthor
                    },
                    newLinkTailoringReference(it, TailoringReferenceType.LINK) {
                        it.linkType = "author"
                        it.catalogItem = randomAsset
                    },
                ]
            }
            catalogItems = [
                manualAuthor,
                randomAsset,
                routerManual
            ]
        }).id

        when:
        executeInTransaction {
            catalogItemMigrationService.migrate(domain.getElementTypeDefinition("document"), domain)
        }

        and:
        def catalog = executeInTransaction {
            catalogRepository.findById(catalogId).get().tap{
                it.catalogItems*.tailoringReferences*.catalogItem*.element
            }
        }

        then:
        with(catalog.catalogItems.find{it.namespace == "routerManual"}) {
            it.tailoringReferences*.catalogItem*.namespace == ["manualAuthor"]
        }
    }
}