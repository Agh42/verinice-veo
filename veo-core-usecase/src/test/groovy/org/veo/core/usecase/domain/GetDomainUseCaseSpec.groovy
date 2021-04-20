/*******************************************************************************
 * Copyright (c) 2019 Urs Zeidler.
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
package org.veo.core.usecase.domain;

import org.veo.core.entity.Domain
import org.veo.core.entity.Key
import org.veo.core.entity.exception.ModelConsistencyException
import org.veo.core.entity.exception.NotFoundException
import org.veo.core.entity.specification.ClientBoundaryViolationException
import org.veo.core.repository.DomainRepository
import org.veo.core.usecase.UseCaseSpec
import org.veo.core.usecase.domain.GetDomainUseCase
import org.veo.core.usecase.domain.GetDomainUseCase.InputData
import org.veo.core.usecase.repository.ClientRepository

class GetDomainUseCaseSpec extends UseCaseSpec {

    DomainRepository repository = Mock()
    ClientRepository clientRepository = Mock()
    Key existingDomainId

    GetDomainUseCase usecase = new GetDomainUseCase(repository)

    def setup() {
        existingDomainId = Key.newUuid()
        existingDomain.getId() >> existingDomainId
        existingDomain.owner >> existingClient

        repository.findById(existingDomainId) >> Optional.of(existingDomain)
        repository.findById(_) >> Optional.empty()
    }

    def "retrieve a domain"() {
        when :
        existingDomain.isActive() >> true
        def output = usecase.execute(new InputData(existingDomainId,  existingClient))
        then:
        output.domain != null
        output.domain.id == existingDomainId
    }

    def "retrieve an inactive domain"() {
        when:
        existingDomain.isActive() >> false
        def output = usecase.execute(new InputData(existingDomainId,  existingClient))
        then:
        thrown(NotFoundException)
    }

    def "retrieve a domain unknown client"() {
        when:
        def output = usecase.execute(new InputData(existingDomainId,  anotherClient))
        then:
        thrown(ClientBoundaryViolationException)
    }

    def "retrieve an unknown domain"() {
        when:
        def output = usecase.execute(new InputData(Key.newUuid(),  existingClient))
        then:
        thrown(NotFoundException)
    }
}
