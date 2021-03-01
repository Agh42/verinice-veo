/*******************************************************************************
 * Copyright (c) 2020 Jonas Jordan.
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
package org.veo.persistence.access

import javax.persistence.EntityManager

import org.springframework.beans.factory.annotation.Autowired

import org.veo.core.entity.Asset
import org.veo.core.usecase.repository.EntityLayerSupertypeQuery
import org.veo.persistence.access.jpa.ClientDataRepository
import org.veo.persistence.access.jpa.DomainDataRepository
import org.veo.persistence.access.jpa.ProcessDataRepository
import org.veo.persistence.access.jpa.UnitDataRepository
import org.veo.persistence.entity.jpa.AbstractJpaSpec
import org.veo.persistence.entity.jpa.ClientData
import org.veo.persistence.entity.jpa.UnitData

class EntityLayerSupertypeQueryImplSpec extends AbstractJpaSpec {

    @Autowired
    ProcessDataRepository processDataRepository

    @Autowired
    ClientDataRepository clientDataRepository

    @Autowired
    DomainDataRepository domainRepository

    @Autowired
    UnitDataRepository unitDataRepository

    ClientData client
    UnitData unit
    EntityLayerSupertypeQuery<Asset> query

    def setup() {
        client = clientDataRepository.save(newClient {})
        unit = unitDataRepository.save(newUnit(client))

        query = new EntityLayerSupertypeQueryImpl<>(processDataRepository, client)
    }

    def 'queries by client'() {
        given:
        def otherClient = clientDataRepository.save(newClient {})
        def otherClientUnit = unitDataRepository.save(newUnit(otherClient))
        processDataRepository.saveAll([
            newProcess(unit) {name = "client process"},
            newProcess(otherClientUnit) {
                name = "other client process"
            }
        ])

        when:
        def result = query.execute()
        then:
        result.size() == 1
        result[0].name == "client process"
    }

    def 'queries all processes'() {
        given:
        processDataRepository.saveAll([
            newProcess(unit),
            newProcess(unit),
            newProcess(unit)
        ])
        when:
        def all = query.execute()
        then:
        all.size() == 3
    }

    def 'queries by units'() {
        given:
        def unit2 = unitDataRepository.save(newUnit(client))
        def unit3 = unitDataRepository.save(newUnit(client))
        processDataRepository.saveAll([
            newProcess(unit) { name = "1st process" },
            newProcess(unit2) { name = "2nd process" },
            newProcess(unit3) { name = "3rd process" },
        ])

        when:
        query.whereUnitIn([unit, unit3] as Set)
        def result = query.execute().sort { it.name }
        then:
        result.size() == 2
        result[0].name == "1st process"
        result[1].name == "3rd process"
    }

    def 'queries by sub type'() {
        given:
        def domain = domainRepository.save(newDomain {owner = client})

        processDataRepository.saveAll([
            newProcess(unit) {
                name = "a"
                setSubType(domain, "VT")
            },
            newProcess(unit) {
                name = "b"
                setSubType(domain, "VT")
            },
            newProcess(unit) {
                name = "c"
            }
        ])

        when:
        query.whereSubTypeIn(["VT"] as Set)
        def result = query.execute()
        then:
        result.size() == 2
        with(result.sort{it.name}) {
            it[0].name == "a"
            it[1].name == "b"
        }
    }

    def 'finds processes with no sub type'() {
        given:
        def domain = domainRepository.save(newDomain {owner = client})

        processDataRepository.saveAll([
            newProcess(unit) {
                name = "a"
                setSubType(domain, "VT")
            },
            newProcess(unit) {
                name = "b"
                setSubType(domain, null)
            },
            newProcess(unit) {
                name = "c"
            }
        ])

        when:
        query.whereSubTypeIn([null] as Set)
        def result = query.execute()
        then:
        result.size() == 2
        with(result.sort{it.name}) {
            it[0].name == "b"
            it[1].name == "c"
        }
    }

    def 'queries by unit & client'() {
        given:
        def client2 = clientDataRepository.save(newClient {})
        def unit2 = unitDataRepository.save(newUnit(client))
        def unit3 = unitDataRepository.save(newUnit(client2))
        processDataRepository.saveAll([
            newProcess(unit) {
                name = "process 1"
            },
            newProcess(unit2) {
                name = "process 2"
            },
            newProcess(unit3) {
                name = "process 3"
            }
        ])

        when:
        query.whereUnitIn([unit2, unit3] as Set)
        def result = query.execute()
        then:
        result.size() == 1
        result[0].name == "process 2"
    }
}