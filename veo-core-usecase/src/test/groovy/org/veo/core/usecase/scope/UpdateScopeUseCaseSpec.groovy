/*******************************************************************************
 * Copyright (c) 2021 Jochen Kemnade.
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
package org.veo.core.usecase.scope

import org.veo.core.entity.Key
import org.veo.core.entity.Scope
import org.veo.core.usecase.UseCaseSpec
import org.veo.core.usecase.base.ModifyEntityUseCase
import org.veo.core.usecase.common.ETag
import org.veo.core.usecase.repository.ScopeRepository

import spock.lang.Unroll

class UpdateScopeUseCaseSpec extends UseCaseSpec {

    public static final String USER_NAME = "john"
    ScopeRepository scopeRepository = Mock()

    UpdateScopeUseCase usecase = new UpdateScopeUseCase(scopeRepository)
    @Unroll
    def "update a scope scope"() {
        given:
        def repository = Mock(ScopeRepository)
        def scopeId = Key.newUuid()
        def scope = Mock(Scope)
        scope.getOwner() >> existingUnit
        scope.getId() >> scopeId
        scope.name >> "Updated scope"

        def existingScope = Mock(Scope) {
            it.id >> scopeId
            it.owner >> existingUnit
        }


        when:
        def eTag = ETag.from(scope.getId().uuidValue(), 0)
        def output = usecase.execute(new ModifyEntityUseCase.InputData(scope, existingClient,  eTag, USER_NAME))
        then:

        1 * scopeRepository.findById(scopeId) >> Optional.of(existingScope)
        1 * scope.version(USER_NAME, existingScope)
        1 * scopeRepository.save(_) >> scope
        output.entity != null
        output.entity.name == "Updated scope"
    }
}
