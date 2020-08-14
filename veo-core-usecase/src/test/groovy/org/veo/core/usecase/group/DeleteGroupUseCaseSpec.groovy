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

import org.veo.core.entity.EntityLayerSupertype
import org.veo.core.entity.GroupType
import org.veo.core.entity.Key
import org.veo.core.usecase.UseCaseSpec
import org.veo.core.usecase.group.DeleteGroupUseCase.InputData

import spock.lang.Unroll

class DeleteGroupUseCaseSpec extends UseCaseSpec {

    DeleteGroupUseCase usecase = new DeleteGroupUseCase(repositoryProvider)

    @Unroll
    def "delete a #type group"() {
        given:
        def repository = Mock(Class.forName("org.veo.core.usecase.repository.${type}Repository"))
        def groupId = Key.newUuid()
        EntityLayerSupertype group = Mock()
        group.getOwner() >> existingUnit
        group.getId() >> groupId

        when:
        def output = usecase.execute(new InputData(groupId, type, existingClient))
        then:
        1 * repositoryProvider.getRepositoryFor(type.entityClass) >> repository
        1 * repository.findById(groupId) >> Optional.of(group)
        1 * repository.deleteById(groupId)
        output.id == groupId

        where:
        type << GroupType.values()
    }
}
