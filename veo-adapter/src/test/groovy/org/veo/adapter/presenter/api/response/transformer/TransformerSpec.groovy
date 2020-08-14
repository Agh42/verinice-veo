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
package org.veo.adapter.presenter.api.response.transformer


import org.veo.adapter.presenter.api.dto.AbstractClientDto
import org.veo.adapter.presenter.api.dto.AbstractDomainDto
import org.veo.adapter.presenter.api.dto.AbstractUnitDto
import org.veo.adapter.presenter.api.dto.full.FullClientDto
import org.veo.adapter.presenter.api.dto.full.FullDomainDto
import org.veo.adapter.presenter.api.dto.full.FullUnitDto
import org.veo.core.entity.Client
import org.veo.core.entity.Domain
import org.veo.core.entity.Key
import org.veo.core.entity.Unit
import org.veo.core.entity.transform.EntityFactory

import spock.lang.Specification

//@CompileStatic
class TransformerSpec extends Specification {

    def unitName = "Test unit"
    def unitId = "2e63d3f8-b326-4304-84e6-c12efbbcaaa4"
    def subUnitName = "Test subunit"
    def subUnitId = "fb329c3e-b87b-44d2-a680-e2d12539f3f7"
    def clientName = "New Client"
    def clientId = "c6960c88-1c71-4e0f-b429-0746d362f12b"
    def domainName = "New Domain"
    def domainId = "202ef4bc-102b-4feb-bbec-1366bcbdac0f"
    def domainDescription = "This is a domain."
    def mUnitId = null

    def createUnit() {
        Unit subUnit = Mock()

        subUnit.getClient() >> null
        subUnit.getDomains() >> []
        subUnit.getName() >> subUnitName
        subUnit.getId() >> Key.uuidFrom(subUnitId)
        subUnit.getUnits() >> []
        subUnit.getModelInterface() >> Unit.getClass()


        Unit unit = Mock()
        unit.getClient() >> null
        unit.getDomains() >> []
        unit.getParent() >> null
        unit.getName() >> unitName
        unit.getId() >> Key.uuidFrom(unitId)
        unit.getUnits() >> [subUnit]
        unit.getModelInterface() >> Unit.getClass()

        subUnit.getParent() >> unit
        return unit
    }

    def createCient(Unit unit) {
        Domain domain = Mock()
        domain.getName()>>domainName
        domain.getId()>> Key.uuidFrom(domainId)
        domain.getDescription()>> domainDescription
        domain.getModelInterface() >> Domain.getClass()

        Client client = Mock()
        client.getid()>>Key.uuidFrom(clientId)
        client.getDomains >> [domain]
        client.getUnits>>[unit]
        client.getName()>> clientName
        client.getModelInterface() >> Client.getClass()



    }

    def AbstractUnitDto createUnitDto() {
        def subUnitDto = new FullUnitDto()
        subUnitDto.setId(subUnitId)
        subUnitDto.setName(subUnitName)

        def unitDto = new FullUnitDto()
        unitDto.setId(unitId)
        unitDto.setName(unitName)
        unitDto.setUnits([subUnitDto] as Set)

        return unitDto
    }

    def "Transform Unit to UnitDto"() {
        given: "A unit with a sub-unit"
        def unit = createUnit()

        when: "the parent unit is transformed into a DTO"
        def unitDto = FullUnitDto.from(unit, EntityToDtoContext.getCompleteTransformationContext())

        then: "The DTO contains all required data"
        unitDto.name == unitName
        unitDto.id == unitId

    }

    def "Transform UnitDto to Unit"() {
        given: " A unit DTO with a subunit DTO"
        def unitDto = createUnitDto()

        Unit u=  Mock(Unit)
        u.id >> Key.uuidFrom(unitId)
        u.name >> unitName


        def factory = Mock(EntityFactory)
        factory.createUnit(_,_,_) >> u

        when: "The parent unit DTO is transformed into a unit"
        def unit = unitDto.toEntity(new DtoToEntityContext(factory))

        then: "The unit contains all data"
        unit.id.uuidValue() == unitId
        unit.name == unitName

    }

    def "Transform Client to ClientDto"() {
        given: "A Client with a unit"
        def unit = createUnit()

        Domain domain = Mock()
        domain.getName()>>domainName
        domain.getId()>> Key.uuidFrom(domainId)
        domain.getDescription()>> domainDescription


        Client client = Mock()
        client.getId()>>Key.uuidFrom(clientId)
        client.getDomains() >> [domain]
        client.getUnits()>>[unit]
        client.getName()>> clientName

        when: "the client is transformed into a DTO"
        def clientDto = FullClientDto.from(client, EntityToDtoContext.getCompleteTransformationContext())

        then: "The DTO contains all required data"
        unit.id.uuidValue() == unitId
        unit.name == unitName
    }

    def "Transform ClientDto to Client"() {
        given: "A client DTO with a unit and a domain"
        def unitDto = createUnitDto()

        def domainDto = new FullDomainDto()
        domainDto.setId(domainId)
        domainDto.setName(domainName)

        def clientDto = new FullClientDto()
        clientDto.setId(clientId)
        clientDto.setName(clientName)
        clientDto.setUnits([unitDto] as Set)
        clientDto.setDomains([domainDto] as Set)

        Domain d = Mock(Domain)
        d.id >> Key.uuidFrom(domainId)
        d.name >> domainName

        Client c=  Mock(Client)
        c.id >> Key.uuidFrom(clientId)
        c.name >> clientName
        c.domains >> [d]

        def factory = Mock(EntityFactory)
        factory.createClient(c.id,clientName) >> c
        factory.createDomain(d.id,domainName) >> d

        when: "the DTO is transformed into a Client"
        def client = clientDto.toEntity(new DtoToEntityContext(factory))

        then: "the client contains all relevant fields"
        client.id.uuidValue() == clientId
        client.name == clientName

        and: "the domain DTO was also converted"
        client.domains.first().id.uuidValue() == domainId
        client.domains.first().name == domainName
    }



}
