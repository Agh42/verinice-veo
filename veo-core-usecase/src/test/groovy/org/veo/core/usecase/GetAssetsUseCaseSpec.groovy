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
package org.veo.core.usecase

import org.veo.core.entity.Asset
import org.veo.core.entity.Key
import org.veo.core.entity.Person
import org.veo.core.repository.PagingConfiguration
import org.veo.core.usecase.asset.GetAssetsUseCase
import org.veo.core.usecase.base.GetEntitiesUseCase.InputData
import org.veo.core.usecase.base.QueryCondition
import org.veo.core.usecase.repository.AssetRepository
import org.veo.core.usecase.repository.EntityLayerSupertypeQuery

class GetAssetsUseCaseSpec extends UseCaseSpec {

    AssetRepository assetRepository = Mock()
    EntityLayerSupertypeQuery<Person> query = Mock()
    PagingConfiguration pagingConfiguration = Mock()

    GetAssetsUseCase usecase = new GetAssetsUseCase(clientRepository, assetRepository, unitHierarchyProvider)

    def setup() {
        assetRepository.query(existingClient) >> query
    }

    def "retrieve all assets for a client"() {
        given:
        def id = Key.newUuid()
        Asset asset = Mock() {
            getOwner() >> existingUnit
            getId() >> id
        }
        when:
        def output = usecase.execute(new InputData(existingClient, null, null, null,pagingConfiguration))
        then:
        1 * clientRepository.findById(existingClient.id) >> Optional.of(existingClient)
        1 * query.execute(pagingConfiguration) >> singleResult(asset, pagingConfiguration)
        output.entities.resultPage*.id == [id]
    }


    def "apply query conditions"() {
        given:
        def id = Key.newUuid()
        Asset asset = Mock() {
            getOwner() >> existingUnit
            getId() >> id
        }
        when:
        def output = usecase.execute(new InputData(existingClient,
                Mock(QueryCondition) {
                    getValues() >> [existingUnit.id]
                },
                null,
                Mock(QueryCondition) {
                    getValues() >> ["subType 1", "subType 2"]
                },pagingConfiguration))
        then:
        1 * clientRepository.findById(existingClient.id) >> Optional.of(existingClient)
        1 * unitHierarchyProvider.findAllInRoot(existingUnit.id) >> existingUnitHierarchyMembers
        1 * query.whereUnitIn(existingUnitHierarchyMembers)
        1 * query.whereSubTypeIn(["subType 1", "subType 2"].toSet())
        1 * query.execute(pagingConfiguration) >> singleResult(asset, pagingConfiguration)
        output.entities.resultPage*.id == [id]
    }
}