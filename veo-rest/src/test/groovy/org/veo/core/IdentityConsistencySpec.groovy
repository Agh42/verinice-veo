/*******************************************************************************
 * Copyright (c) 2020 Alexander Koderman.
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
package org.veo.core

import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

import org.hibernate.Session
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

import org.veo.core.entity.Asset
import org.veo.core.entity.ModelObject
import org.veo.core.entity.transform.EntityFactory
import org.veo.persistence.access.AssetRepositoryImpl
import org.veo.persistence.access.ClientRepositoryImpl
import org.veo.persistence.access.UnitRepositoryImpl
import org.veo.persistence.entity.jpa.AssetData
import org.veo.persistence.entity.jpa.BaseModelObjectData
import org.veo.persistence.entity.jpa.ClientData
import org.veo.persistence.entity.jpa.ControlData
import org.veo.persistence.entity.jpa.DocumentData
import org.veo.persistence.entity.jpa.PersonData
import org.veo.persistence.entity.jpa.ProcessData
import org.veo.persistence.entity.jpa.UnitData
import org.veo.persistence.entity.jpa.groups.AssetGroupData
import org.veo.persistence.entity.jpa.groups.ControlGroupData
import org.veo.persistence.entity.jpa.groups.DocumentGroupData
import org.veo.persistence.entity.jpa.groups.PersonGroupData
import org.veo.persistence.entity.jpa.groups.ProcessGroupData

@SpringBootTest(classes = IdentityConsistencySpec.class)
@ComponentScan("org.veo")
@ActiveProfiles("test")
class IdentityConsistencySpec<T extends ModelObject> extends VeoSpringSpec {


    @PersistenceContext
    EntityManager entityManager

    @Autowired
    ClientRepositoryImpl clientRepository

    @Autowired
    UnitRepositoryImpl unitRepository

    @Autowired
    private EntityFactory factory
    private client
    private unit

    @Transactional
    def setup() {
        client = newClient()
        unit = newUnit(this.client)
        clientRepository.save(this.client)
        unitRepository.save(this.unit)
        entityManager.flush()
    }


    def void testIdentityConsistency(Class<T> clazz, T entity) {
        // HashSet requires correct implementations
        // of hashCode() (to find the bucket) and
        // of equals() (to identify the object):
        def entities = new HashSet<T>()

        //when: "the entity is added to the set"
        entities.add(entity)

        //then: "the entity is present in the set"
        assert entities.contains(entity)

        //when: "the entity is persisted"
        entityManager.persist(entity)
        entityManager.flush()

        //then: "the entity is present in the set"
        assert entities.contains(entity)

        //when: "a reference is loaded in a different persistence context"
        def reference = entityManager.getReference(clazz, entity.getDbId())

        //then: "the reference is equal with the entity"
        assert reference.equals(entity)

        //and: "the entity is equal with the reference"
        assert entity.equals(reference)

        //and: "the entity is present in the set"
        assert entities.contains(reference)

        //when: "the entity is updated"
        entityManager.merge(entity)

        //then: "the entity is present in the set"
        assert entities.contains(entity)

        //when: "the entity is reattached"
        entityManager.unwrap(Session.class).update(entity)

        //then: "the entity is present in the set"
        assert entities.contains(entity)

        //when: "the entity is loaded in a different persistence context"
        def entityInstance2 = entityManager.find(clazz, entity.getDbId())

        //then: "the entity is present in the set"
        assert entities.contains(entityInstance2)

        //when: "the entity is deleted"
        def deletedEntity = entityManager.getReference(
                clazz,
                entity.getDbId()
                )
        entityManager.remove(deletedEntity)

        //then: "the entity is found in the set"
        assert entities.contains(deletedEntity)
    }

    @Transactional
    def "The identity of the entity 'asset' is consistent over state transitions"() {
        // TODO VEO-343 the integrity of the created entity is maintained by this factory method (newAsset).
        // This factory method should not be part of the test component.
        // It is currently possible to create inconsistent
        // entities if the 'Create*UseCase' classes and DTO-transformation are not being used.
        // This can lead to subtle errors because of
        // incorrectly initialized entities being used in other places in the code.
        // Entity creation should be encapsulated in constructors and/or factory methods and all
        // relevant initialization methods be made package private. It should be impossible or at least harder to create entities that do not
        // have a valid ID or that do not have their collections properly initialized.

        when:
        testIdentityConsistency(AssetData.class, newAsset(unit))

        then:
        notThrown(Exception)
    }

    @Transactional
    def "The identity of the entity 'control' is consistent over state transitions"() {
        when:
        testIdentityConsistency(ControlData.class, newControl(unit))

        then:
        notThrown(Exception)
    }

    @Transactional
    def "The identity of the entity 'person' is consistent over state transitions"() {
        when:
        testIdentityConsistency(PersonData.class, newPerson(unit))

        then:
        notThrown(Exception)
    }

    @Transactional
    def "The identity of the entity 'process' is consistent over state transitions"() {
        when:
        testIdentityConsistency(ProcessData.class, newProcess(unit))

        then:
        notThrown(Exception)
    }

    @Transactional
    def "The identity of the entity 'document' is consistent over state transitions"() {
        when:
        testIdentityConsistency(DocumentData.class, newDocument(unit))

        then:
        notThrown(Exception)
    }

    @Transactional
    def "The identity of the entity 'controlgroup' is consistent over state transitions"() {
        when:
        testIdentityConsistency(ControlGroupData.class, newControlGroup(unit))

        then:
        notThrown(Exception)
    }

    @Transactional
    def "The identity of the entity 'assetgroup' is consistent over state transitions"() {
        when:
        testIdentityConsistency(AssetGroupData.class, newAssetGroup(unit))

        then:
        notThrown(Exception)
    }

    @Transactional
    def "The identity of the entity 'persongroup' is consistent over state transitions"() {
        when:
        testIdentityConsistency(PersonGroupData.class, newPersonGroup(unit))

        then:
        notThrown(Exception)
    }

    @Transactional
    def "The identity of the entity 'documentgroup' is consistent over state transitions"() {
        when:
        testIdentityConsistency(DocumentGroupData.class, newDocumentGroup(unit))

        then:
        notThrown(Exception)
    }

    @Transactional
    def "The identity of the entity 'processgroup' is consistent over state transitions"() {
        when:
        testIdentityConsistency(ProcessGroupData.class, newProcessGroup(unit))

        then:
        notThrown(Exception)
    }

    @Transactional
    def "The identity of the entity 'unit' is consistent over state transitions"() {
        when:
        testIdentityConsistency(UnitData.class, newUnit(client))

        then:
        notThrown(Exception)
    }

    @Transactional
    def "The identity of the entity 'client' is consistent over state transitions"() {
        when:
        testIdentityConsistency(ClientData.class, newClient())

        then:
        notThrown(Exception)
    }
}
