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

import org.veo.core.entity.Client
import org.veo.core.entity.Domain
import org.veo.core.entity.DomainTemplate
import org.veo.core.entity.Key
import org.veo.core.entity.exception.NotFoundException
import org.veo.core.repository.DomainRepository
import org.veo.core.service.DomainTemplateService
import org.veo.core.usecase.UseCaseSpec
import org.veo.core.usecase.domain.GetDomainsUseCase
import org.veo.core.usecase.domain.GetDomainsUseCase.InputData
import org.veo.core.usecase.repository.ClientRepository


class GetDomainsUseCaseSpec extends UseCaseSpec {

    DomainRepository repository = Mock()
    Key existingDomainId

    GetDomainsUseCase usecase = new GetDomainsUseCase(repository)

    def setup() {
        existingDomainId = Key.newUuid()
        existingDomain.getId() >> existingDomainId
        existingDomain.owner >> existingClient
        existingDomain.active >> true

        repository.findById(existingDomainId) >> Optional.of(existingDomain)
        repository.findById(_) >> Optional.empty()
    }


    def "retrieve all domains for the client"() {
        given:
        def id = Key.newUuid()
        Domain domain = Mock()
        domain.getId() >> id
        domain.owner >> existingClient
        domain.active >> false

        existingClient.getDomains() >> [existingDomain, domain]

        when:
        def output = usecase.execute(new InputData(existingClient))
        then:
        output.objects != null
        output.objects.size() == 1
    }
}
