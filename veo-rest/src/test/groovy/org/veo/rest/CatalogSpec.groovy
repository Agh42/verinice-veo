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
package org.veo.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.support.TransactionTemplate

import org.veo.core.VeoMvcSpec
import org.veo.core.entity.Catalog
import org.veo.core.entity.CatalogItem
import org.veo.core.entity.Client
import org.veo.core.entity.Control
import org.veo.core.entity.Domain
import org.veo.core.entity.Key
import org.veo.core.entity.TailoringReferenceType
import org.veo.core.entity.Unit
import org.veo.core.usecase.common.ETag
import org.veo.persistence.access.CatalogRepositoryImpl
import org.veo.persistence.access.ClientRepositoryImpl
import org.veo.persistence.access.ControlRepositoryImpl
import org.veo.persistence.access.DomainRepositoryImpl
import org.veo.persistence.access.UnitRepositoryImpl
import org.veo.persistence.entity.jpa.transformer.EntityDataFactory
import org.veo.rest.configuration.WebMvcSecurityConfiguration

/**
 * This provides a complete client domain catalog example.
 */
class CatalogSpec extends VeoMvcSpec {
    @Autowired
    ClientRepositoryImpl clientRepository
    @Autowired
    UnitRepositoryImpl unitRepository
    @Autowired
    ControlRepositoryImpl controlRepository
    @Autowired
    DomainRepositoryImpl domainRepository
    @Autowired
    CatalogRepositoryImpl catalogRepository
    @Autowired
    TransactionTemplate txTemplate
    @Autowired
    EntityDataFactory entityFactory

    Domain domain
    Domain domain1
    Catalog catalog
    CatalogItem item1
    CatalogItem item2
    CatalogItem item3
    CatalogItem item4
    CatalogItem item5
    CatalogItem item6
    CatalogItem otherItem
    Key clientId = Key.uuidFrom(WebMvcSecurityConfiguration.TESTCLIENT_UUID)
    Client client
    Client secondClient
    Domain domain3
    Catalog catalog1
    Unit unit
    Unit unitSecondClient
    Control controlSecondClient
    String salt = "salt-for-etag"

    def setup() {
        ETag.setSalt(salt)
        txTemplate.execute {
            client = newClient {
                id = clientId
            }

            domain = newDomain {
                description = "ISO/IEC"
                abbreviation = "ISO"
                name = "ISO"
                authority = 'ta'
                revision = '1'
                templateVersion = '1.0'
                domainTemplate = domainTemplate
            }
            catalog = newCatalog(domain) {
                name= 'a'
            }

            item1 = newCatalogItem(catalog, {
                newControl(it) {
                    name = 'c1'
                    domains = [domain]
                }
            })
            item2 = newCatalogItem(catalog, {
                newControl(it) {
                    name = 'c2'
                }
            })
            item3 = newCatalogItem(catalog, {
                newControl(it) {
                    name = 'c3'
                }
            })
            newTailoringReference(item3) {
                catalogItem = item2
                referenceType = TailoringReferenceType.COPY
            }
            newTailoringReference(item3) {
                catalogItem = item1
                referenceType = TailoringReferenceType.COPY_ALWAYS
            }

            item4 = newCatalogItem(catalog, {
                newProcess(it) {
                    name = 'p1'
                    description = "a process example entry"
                }
            })

            newTailoringReference(item4) {
                catalogItem = item1
                referenceType = TailoringReferenceType.LINK
            }
            newTailoringReference(item4) {
                catalogItem = item2
                referenceType = TailoringReferenceType.LINK
            }

            item4.element.links = [
                newCustomLink(item1.element) {
                    type = "link_to_item_1"
                },
                newCustomLink(item2.element) {
                    type = "link_to_item_2"
                }
            ] as Set


            item5 = newCatalogItem(catalog, {
                newProcess(it) {
                    name = 'p2'
                    description = "a process with subtype"
                    setSubType(domain, "MY_SUBTYPE")
                }
            })

            item6 = newCatalogItem(catalog, {
                newProcess(it) {
                    abbreviation = "caf"
                    name = 'p3-all-features'
                    description = "a process with subtype"
                    setSubType(domain, "MY_SUBTYPE")
                    customAspects = [
                        newCustomProperties("process_resilience") {
                            attributes = [
                                "process_resilience_impact":"process_resilience_impact_low"
                            ]
                        },
                        newCustomProperties("process_processingDetails") {
                            attributes = [
                                "process_processingDetails_comment":"my comment",
                                "process_processingDetails_operatingStage":"process_processingDetails_operatingStage_operation"
                            ]
                        }
                    ]
                    links = [
                        newCustomLink(item1.element) {
                            type = "link_to_item_1"
                        }
                    ]
                }
            })

            newTailoringReference(item6) {
                catalogItem = item1
                referenceType = TailoringReferenceType.LINK
            }
            domain1 = newDomain {
                description = "ISO/IEC2"
                abbreviation = "ISO"
                name = "ISO"
                authority = 'ta'
                revision = '1'
                templateVersion = '1.0'
            }

            client.domains = [domain, domain1] as Set
            client = clientRepository.save(client)

            unit = newUnit(client) {
                it.name = "Test unit"
            }
            unit = unitDataRepository.save(unit)

            domain = client.domains.toList().get(0)
            domain1 = client.domains.toList().get(1)
            catalog = domain.catalogs.first()

            (item1, item2, item3, item4, item5, item6) = catalog.catalogItems.sort{it.element.name}

            domain3 = newDomain {
                abbreviation = "D1"
                name = "Domain 1"
                authority = 'ta'
                revision = '1'
                templateVersion = '1.0'
            }
            catalog1 = newCatalog(domain3) {
                name = 'b'
                newCatalogItem(it, {
                    newControl(it) {
                        name = 'c15'
                    }
                })
            }

            secondClient = newClient() {
                it.name = "the other"
            }
            secondClient.addToDomains(domain3)
            secondClient = clientRepository.save(secondClient)
            domain3 = secondClient.domains[0]

            unitSecondClient = newUnit(secondClient) {
                it.name = "Other client unit"
            }

            unitSecondClient = unitDataRepository.save(unitSecondClient)
            controlSecondClient = controlRepository.save(newControl(unitSecondClient))

            catalog1 = domain3.catalogs.first()
            otherItem = catalog1.catalogItems.first()
        }
    }
}