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
import org.veo.core.entity.transform.TransformTargetToEntityContext
import org.veo.core.usecase.UseCaseSpec
import org.veo.core.usecase.group.UpdateGroupUseCase.InputData
import spock.lang.Unroll

class UpdateGroupUseCaseSpec extends UseCaseSpec {

    PutGroupUseCase usecase = new PutGroupUseCase(repositoryProvider)
    @Unroll
    def "update a #type group"() {
        given:
        TransformTargetToEntityContext targetToEntityContext = Mock()
        def repository = Mock(Class.forName("org.veo.core.usecase.repository.${type}Repository"))
        def groupId = Key.newUuid()
        def group = Mock(type.groupClass)
        group.getOwner() >> existingUnit
        group.getId() >> groupId
        group.name >> "Updated $type group"

        repositoryProvider.getRepositoryFor(_) >> repository

        when:
        def output = usecase.execute(new InputData(group, existingClient))
        then:

        //        1 * targetToEntityContext.partialDomain() >> targetToEntityContext
        1 * repository.findById(groupId) >> Optional.of(group)

        1 * repository.save(_) >> group
        output.group != null
        output.group.name == "Updated $type group"
        //        output.group.getClass() == type.groupClass // check removed as it is a mock
        where:
        type << GroupType.values()
    }
}
