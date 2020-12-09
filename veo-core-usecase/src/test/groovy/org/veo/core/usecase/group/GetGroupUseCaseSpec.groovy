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
package org.veo.core.usecase.group

import org.veo.core.entity.GroupType
import org.veo.core.entity.Key
import org.veo.core.entity.ModelGroup
import org.veo.core.entity.transform.TransformTargetToEntityContext
import org.veo.core.usecase.UseCaseSpec
import org.veo.core.usecase.group.GetGroupUseCase.InputData
import org.veo.core.usecase.repository.EntityGroupRepository

import spock.lang.Unroll

class GetGroupUseCaseSpec extends UseCaseSpec {

    EntityGroupRepository entityGroupRepository = Mock()

    GetGroupUseCase usecase = new GetGroupUseCase(entityGroupRepository)
    @Unroll
    def "retrieve a #type group"() {
        given:
        TransformTargetToEntityContext targetToEntityContext = Mock()
        def repository = Mock(EntityGroupRepository)
        def groupId = Key.newUuid()
        def group = Mock(ModelGroup)
        group.getOwner() >> existingUnit
        group.getId() >> groupId

        when:
        def output = usecase.execute(new InputData(groupId, existingClient))
        then:
        1 * entityGroupRepository.findById(groupId) >> Optional.of(group)
        output.group != null
        output.group.id == groupId
        where:
        type << GroupType.values()
    }
}
