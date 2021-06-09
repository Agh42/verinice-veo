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
package org.veo.persistence.entity.jpa

import org.springframework.beans.factory.annotation.Autowired

import org.veo.core.entity.Unit
import org.veo.persistence.access.jpa.AssetDataRepository
import org.veo.persistence.access.jpa.ClientDataRepository
import org.veo.persistence.access.jpa.UnitDataRepository

class CustomPropertyJpaSpec extends AbstractJpaSpec {
    @Autowired
    AssetDataRepository assetRepository

    @Autowired
    UnitDataRepository unitRepository

    @Autowired
    ClientDataRepository clientDataRepository

    Unit unit

    def setup() {
        def client = clientDataRepository.save(newClient())
        unit = newUnit(client)
        unit = unitRepository.save(unit)
    }

    def 'custom props are inserted'() {
        given:
        def asset = newAsset(unit) {
            customAspects = [
                new CustomPropertiesData(
                dbId: UUID.randomUUID().toString(),
                attributes: [
                    "k1": "uno",
                    "k2": 2
                ]
                )
            ]
        }
        when:
        assetRepository.save(asset)
        def retrievedAsset = assetRepository.findById(asset.dbId)
        then:
        retrievedAsset.present
        retrievedAsset.get().customAspects[0].attributes == [
            "k1": "uno",
            "k2": 2
        ]
    }

    def 'property type can be changed'() {
        given: 'a saved asset with a string prop'
        def asset = newAsset(unit) {
            customAspects = [
                new CustomPropertiesData(
                dbId: UUID.randomUUID().toString(),
                attributes: [
                    "k1": "uno"
                ]
                )
            ]
        }
        assetRepository.save(asset)
        when: 'replacing the string prop with an int prop'
        asset.customAspects[0].attributes = [
            "k1": 1
        ]
        assetRepository.save(asset)
        def retrievedAsset = assetRepository.findById(asset.dbId)
        then: 'the change has been applied'
        retrievedAsset.get().customAspects[0].attributes == [
            "k1": 1
        ]
    }

    def 'property can be removed'() {
        given: 'a saved asset with two props'
        def asset = newAsset(unit) {
            customAspects = [
                new CustomPropertiesData(
                dbId: UUID.randomUUID().toString(),
                attributes: [
                    "k1": "uno",
                    "k2": "due",
                ]
                )
            ]
        }
        assetRepository.save(asset)
        when: 'removing the first prop'
        asset.customAspects[0].attributes = ["k2": "due"]
        assetRepository.save(asset)
        def retrievedAsset = assetRepository.findById(asset.dbId)
        then: 'only the second prop remains'
        retrievedAsset.get().customAspects[0].attributes == ["k2": "due"]
    }

    def 'long custom property value is accepted'() {
        given:
        def stringLength = 18000
        def longString = "X" * stringLength
        def asset = newAsset(unit) {
            customAspects = [
                new CustomPropertiesData(
                dbId: UUID.randomUUID().toString(),
                attributes: ["p": longString]
                )
            ]
        }
        when:
        assetRepository.save(asset)
        def retrievedAsset = assetRepository.findById(asset.getId().uuidValue())
        then:
        retrievedAsset.present
        when:
        def savedValue = retrievedAsset.get().customAspects.first().attributes["p"]
        then:
        savedValue.length() == stringLength
        savedValue == longString
    }
}
