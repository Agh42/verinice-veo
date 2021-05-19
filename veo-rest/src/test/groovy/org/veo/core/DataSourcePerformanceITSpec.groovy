/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2020  Jochen Kemnade.
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

import static com.vladmihalcea.sql.SQLStatementCountValidator.assertDeleteCount
import static com.vladmihalcea.sql.SQLStatementCountValidator.assertInsertCount
import static com.vladmihalcea.sql.SQLStatementCountValidator.assertSelectCount
import static com.vladmihalcea.sql.SQLStatementCountValidator.assertUpdateCount
import static com.vladmihalcea.sql.SQLStatementCountValidator.reset

import javax.transaction.Transactional

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

import org.veo.core.entity.Asset
import org.veo.core.entity.Client
import org.veo.core.entity.CompositeEntity
import org.veo.core.entity.CustomProperties
import org.veo.core.entity.EntityLayerSupertype
import org.veo.core.entity.Person
import org.veo.core.entity.Process
import org.veo.core.entity.Scope
import org.veo.core.entity.Unit
import org.veo.core.entity.Versioned.Lifecycle
import org.veo.core.repository.PagingConfiguration
import org.veo.persistence.access.AssetRepositoryImpl
import org.veo.persistence.access.ClientRepositoryImpl
import org.veo.persistence.access.DomainRepositoryImpl
import org.veo.persistence.access.PersonRepositoryImpl
import org.veo.persistence.access.ProcessRepositoryImpl
import org.veo.persistence.access.ScopeRepositoryImpl
import org.veo.persistence.access.UnitRepositoryImpl

@SpringBootTest(classes = DataSourcePerformanceITSpec.class)
@ActiveProfiles(["test", "stats"])
class DataSourcePerformanceITSpec extends VeoSpringSpec {

    public static final String PROP_KEY = "propKey"
    @Autowired
    private ClientRepositoryImpl clientRepository

    @Autowired
    private UnitRepositoryImpl unitRepository

    @Autowired
    private PersonRepositoryImpl personRepository

    @Autowired
    private AssetRepositoryImpl assetRepository

    @Autowired
    private ProcessRepositoryImpl processRepository

    @Autowired
    private ScopeRepositoryImpl scopeRepository

    @Autowired
    private DomainRepositoryImpl domainRepository

    private Client client
    private Unit unit

    def "SQL performance for saving a new domain, client and unit"() {
        when:
        reset()
        createClient()

        then:
        assertDeleteCount(0)
        assertInsertCount(6)
        assertUpdateCount(0)
        assertSelectCount(5)
    }

    def "SQL performance for saving 1 process"() {
        given:
        createClient()

        when:
        reset()
        saveProcess("process1")

        then:
        assertDeleteCount(0)
        assertInsertCount(2)
        assertUpdateCount(0)
        assertSelectCount(0)
    }

    def "SQL performance for saving 1 process with 2 links to 1 asset and 1 composite person"() {
        given:
        createClient()

        when:
        reset()
        saveProcessWithLinks()

        then:
        assertDeleteCount(0)
        assertInsertCount(7)
        assertUpdateCount(0)
        assertSelectCount(0)
    }


    def     "SQL performance for saving 1 process with 10 customAspects"() {
        given:
        createClient()

        when:
        reset()
        saveProcessWithCustomAspects(10)

        then:
        assertDeleteCount(0)
        assertInsertCount(3)
        assertUpdateCount(0)
        assertSelectCount(0)
    }

    def "SQL performance for saving 1 process with 1 customAspect with 10 array properties"() {
        given:
        createClient()

        when:
        reset()
        def process = saveProcessWithArrayCustomAspect(10)

        then:
        assertDeleteCount(0)
        assertInsertCount(5)
        assertUpdateCount(0)
        assertSelectCount(0)
    }

    def "SQL performance for putting 1 string value in 1 customAspect with 10 existing values"() {
        given:
        createClient()
        def process = saveProcessWithArrayCustomAspect(10)

        when:
        reset()
        updateProcessWithArrayCustomAspect(process)

        then:
        assertDeleteCount(0)
        assertInsertCount(1)
        assertUpdateCount(0)
        assertSelectCount(10)
    }

    def "SQL performance for saving 1 composite person with 2 parts"() {
        given:
        createClient()

        when:
        reset()
        savePerson([
            newPerson(unit),
            newPerson(unit)
        ])

        then:
        assertDeleteCount(0)
        assertInsertCount(3)
        assertUpdateCount(0)
        assertSelectCount(0)
    }

    def "SQL performance for saving 1 scope with 2 persons"() {
        given:
        createClient()

        when:
        reset()
        saveScope("Scope 1", [
            newPerson(unit),
            newPerson(unit)
        ])

        then:
        assertDeleteCount(0)
        assertInsertCount(4)
        assertUpdateCount(0)
        assertSelectCount(0)
    }


    def     "SQL performance for saving 1 scope with 100 persons"() {
        given:
        createClient()

        when:
        reset()
        saveScope("Scope 1",(1..100).collect{
            newPerson(unit)
        })

        then:
        assertDeleteCount(0)
        assertInsertCount(13)
        assertUpdateCount(0)
        assertSelectCount(0)
    }

    def "SQL performance for saving 1 scope with 100 persons with 2 parts each"() {
        given:
        createClient()

        when:
        reset()
        saveScope("Scope 1",(1..100).collect{
            newPerson(unit) {
                parts = [
                    newPerson(unit),
                    newPerson(unit)
                ]
            }
        })

        then:
        assertDeleteCount(0)
        assertInsertCount(33)
        assertUpdateCount(0)
        assertSelectCount(0)
    }

    def "SQL performance for saving 1 scope with 10 persons with 10 parts each"() {
        given:
        createClient()

        when:
        reset()
        saveScope("Scope 1",(1..10).collect{
            newPerson(unit) {
                parts =(1..10).collect{
                    newPerson(unit)
                }
            }
        })

        then:
        assertDeleteCount(0)
        assertInsertCount(14)
        assertUpdateCount(0)
        assertSelectCount(0)
    }

    def "SQL performance for adding 100 persons with 2 parts each to an existing scope"() {
        given:
        createClient()
        def scope = saveScope("scope")

        when:
        reset()
        saveWithAddedCompositePersons(100, "person", scope )

        then:
        assertDeleteCount(1)
        assertInsertCount(35)
        assertUpdateCount(5)
        assertSelectCount(7)
    }

    def "SQL performance for selecting units of a client"() {
        given:
        createClient()
        createClientUnits(100)

        when:
        reset()
        def units = selectUnits()

        then:
        units.size() == 101
        assertDeleteCount(0)
        assertInsertCount(0)
        assertUpdateCount(0)
        assertSelectCount(1)
    }

    def "SQL performance for selecting subunits of a unit"() {
        given:
        createClient()
        createSubUnits(100)

        when:
        reset()
        def units = selectSubUnits(unit)

        then:
        units.size() == 100
        assertDeleteCount(0)
        assertInsertCount(0)
        assertUpdateCount(0)
        assertSelectCount(2)
    }

    def "SQL performance for deleting 1 unit with 100 persons of 2 parts each"() {
        given:
        createClient()
        100.times {
            savePerson([
                newPerson(unit),
                newPerson(unit)
            ])
        }

        when:
        reset()
        personRepository.deleteByUnit(unit)
        unitRepository.delete(unit)

        then:
        assertDeleteCount(16)
        assertInsertCount(11)
        assertUpdateCount(0)
        assertSelectCount(11)
    }

    def "SQL performance for deleting a unit with 1 asset, 1 process and 1 composite person linked to each other"() {
        given:
        createClient()
        createLinkedEntities()

        when:
        reset()
        deleteUnit()

        then: "all entities are removed"
        with(personRepository.query(client)) {
            whereUnitIn([unit] as Set)
            execute(PagingConfiguration.UNPAGED).totalResults == 0
        }
        with(assetRepository.query(client)) {
            whereUnitIn([unit] as Set)
            execute(PagingConfiguration.UNPAGED).totalResults == 0
        }
        with(processRepository.query(client)) {
            whereUnitIn([unit] as Set)
            execute(PagingConfiguration.UNPAGED).totalResults == 0
        }
        unitRepository.findByClient(client).size() == 0

        and:
        assertDeleteCount(12)
        assertInsertCount(4)
        assertUpdateCount(0)
        assertSelectCount(36)
    }

    def "SQL performance for deleting 2 units with 1 commonly referenced domain"() {
        given:
        createClient()
        def units = createClientUnits(2)

        def domain2 = newDomain(client) {
            authority = 'ta'
            revision = '1'
            templateVersion = '1.0'
        }
        domainRepository.save(domain2)

        client.addToDomains(domain2)
        client = clientRepository.save(client)

        when:
        reset()
        units.each {
            it.addToDomains(domain2)
        }
        unitRepository.saveAll(units)

        then:
        assertDeleteCount(0)
        assertInsertCount(2)
        assertUpdateCount(1)
        assertSelectCount(6)
    }

    @Transactional
    void createClient() {
        client = clientRepository.save(newClient())
        def domain = domainRepository.save(newDomain{
            owner = this.client
            name = "domain1"
        })

        client.addToDomains(domain)
        client = clientRepository.save(client)

        unit = newUnit(client)
        unit.setClient(client)
        unit.addToDomains(domain)

        unit = unitRepository.save(unit)
        unit.client = this.client
    }

    @Transactional
    Asset saveAsset(String assetName) {
        return assetRepository.save(newAsset(unit).with {
            name = assetName
            it
        })
    }

    @Transactional
    Process saveProcess(String processName) {
        return processRepository.save(newProcess(unit).tap {
            name = processName
        })
    }

    @Transactional
    Scope saveScope(String name, List<CompositeEntity<EntityLayerSupertype>> members = []) {
        return scopeRepository.save(newScope(unit).tap {
            it.name = name
            it.members = members
        })
    }

    @Transactional
    Process saveProcessWithLinks() {
        def asset = saveAsset("asset1")
        def process = newProcess(unit).with {
            name = "process1"
            it
        }
        def person = savePerson()

        def link1 = newCustomLink(process, asset) {
            type = "aLink"
        }

        def link2 = newCustomLink(process, person) {
            type = "anotherLink"
        }

        process.setLinks([link1, link2] as Set)
        processRepository.save(process)
    }

    @Transactional
    Person savePerson(List<Person> parts = []) {
        def person = newPerson(unit) {
            it.parts = parts
            it.state = Lifecycle.CREATING
        }
        return personRepository.save(person)
    }

    @Transactional
    def saveWithAddedCompositePersons(int count, String baseName, Scope scope ) {
        def compositePersons = (0..<count).collect {
            def dolly = newPerson(unit)
            def minime = newPerson(unit)
            def person = newPerson(unit) {
                name = baseName+count
            }
            person.tap {
                parts = [dolly, minime]
                state = Lifecycle.CREATING
            }
        }

        scope.members.addAll(compositePersons)
        scopeRepository.save(scope)
    }

    @Transactional
    def saveProcessWithCustomAspects(int count) {
        def process = newProcess(unit)
        for (i in 0..<count) {
            CustomProperties cp = newCustomProperties("aType")
            process.addToCustomAspects(cp)
        }
        processRepository.save(process)
    }

    @Transactional
    HashSet<Unit> createClientUnits(int count) {
        def units = new HashSet<Unit>()
        for (i in 0..<count) {
            def unit = newUnit(client).tap {
                name = "unit" + count
            }
            units.add(unitRepository.save(unit))
        }
        units
    }

    @Transactional
    selectUnits() {
        unitRepository.findByClient(client)
    }

    @Transactional
    void createSubUnits(int count) {
        for (i in 0..<count) {
            def unit = newUnit(client).tap {
                name = "unit" + count
                parent = unit
            }
            unitRepository.save(unit)
        }
    }

    @Transactional
    List<Unit> selectSubUnits(Unit owner) {
        unitRepository.findByParent(owner)
    }

    @Transactional
    def Process saveProcessWithArrayCustomAspect(int count) {
        def process = newProcess(unit)
        def values = []
        for (i in 0..<count) {
            values.add("value_" + i)
        }
        CustomProperties cp = newCustomProperties("aType")
        cp.setProperty(PROP_KEY, values as List)
        process.addToCustomAspects(cp)
        return processRepository.save(process)
    }

    @Transactional
    def void updateProcessWithArrayCustomAspect(Process detachedProcess) {
        def process = processRepository.findById(detachedProcess.getId()).get()
        // adding 1 value to the end of the list:
        process.customAspects
                .first().stringListProperties.entrySet().first().value.add("value_new")
        processRepository.save(process)
    }

    @Transactional
    def createLinkedEntities() {
        def compositePerson = savePerson([
            newPerson(unit),
            newPerson(unit)
        ]) // creates person with 2 parts

        def asset = newAsset(unit)
        asset = assetRepository.save(asset)

        def asset2 = newAsset(unit)
        asset2 = assetRepository.save(asset2)

        def process = newProcess(unit)
        process = processRepository.save(process)

        def link_person_asset = newCustomLink(compositePerson, asset) {
            type = "type1"
        }
        compositePerson.addToLinks(link_person_asset)
        compositePerson = personRepository.save(compositePerson)

        def link_asset_person = newCustomLink(asset, compositePerson) {
            type = "type2"
        }
        def link_asset_process = newCustomLink(asset, process) {
            type = "type3"
        }
        asset.addToLinks(link_asset_process)
        asset.addToLinks(link_asset_person)
        asset = assetRepository.save(asset)

        def link_process_person = newCustomLink(process, compositePerson) {
            type = "type4"
        }
        process.addToLinks(link_process_person)
        process = processRepository.save(process)

        def link_asset_asset = newCustomLink(asset, asset2) {
            type = "type5"
        }
        asset.addToLinks(link_asset_asset)
        asset = assetRepository.save(asset)
    }

    @Transactional
    def deleteUnit() {
        assetRepository.deleteByUnit(unit)
        personRepository.deleteByUnit(unit)
        processRepository.deleteByUnit(unit)
        scopeRepository.deleteByUnit(unit)
        unitRepository.delete(unit)
    }
}
