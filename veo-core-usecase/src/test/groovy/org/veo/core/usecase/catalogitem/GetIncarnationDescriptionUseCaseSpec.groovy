/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2019  Urs Zeidler.
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
package org.veo.core.usecase.catalogitem

import org.veo.core.entity.Catalog
import org.veo.core.entity.CatalogItem
import org.veo.core.entity.Catalogable
import org.veo.core.entity.Client
import org.veo.core.entity.Control
import org.veo.core.entity.CustomLink
import org.veo.core.entity.DomainTemplate
import org.veo.core.entity.Key
import org.veo.core.entity.TailoringReference
import org.veo.core.entity.TailoringReferenceType
import org.veo.core.entity.Unit
import org.veo.core.entity.exception.NotFoundException
import org.veo.core.entity.specification.ClientBoundaryViolationException
import org.veo.core.repository.ElementQuery
import org.veo.core.repository.ElementRepository
import org.veo.core.repository.PagedResult
import org.veo.core.repository.PagingConfiguration
import org.veo.core.repository.RepositoryProvider
import org.veo.core.repository.UnitRepository
import org.veo.core.service.CatalogItemService
import org.veo.core.service.DomainTemplateService
import org.veo.core.usecase.DesignatorService
import org.veo.core.usecase.UseCaseSpec
import org.veo.core.usecase.catalogitem.GetIncarnationDescriptionUseCase
import org.veo.core.usecase.catalogitem.GetIncarnationDescriptionUseCase.InputData
import org.veo.core.usecase.parameter.TailoringReferenceParameter

class GetIncarnationDescriptionUseCaseSpec extends ApplyIncarnationDescriptionSpec {

    GetIncarnationDescriptionUseCase usecase = new GetIncarnationDescriptionUseCase(unitRepo, catalogItemRepository, entityRepo)

    def setup() {
        entityRepo.getElementRepositoryFor(_) >> repo
        repo.query(existingClient) >> emptyQuery

        catalogItemRepository.findById(item1.id) >> Optional.of(item1)
        catalogItemRepository.findById(item2.id) >> Optional.of(item2)
        catalogItemRepository.findById(_) >> Optional.empty()
    }

    def "get the apply information for a catalog-item no tailorref"() {
        given:

        item1.tailoringReferences >> []

        existingDomain.catalogs >> [catalog]
        catalog.domainTemplate >> existingDomain
        catalog.catalogItems >> [item1]

        when:
        def output = usecase.execute(new InputData(existingClient, existingUnit.id, [item1.id]))
        then:
        output.references.size() == 1
        output.references.first().item == item1
        output.references.first().references == []
    }

    def "get the apply information for a catalog-item with one copy ref"() {
        given:

        Control control2 = Mock()

        def id2 = Key.newUuid()
        item2.id >> id2
        item2.catalog >> catalog
        item2.element>>control2
        item2.tailoringReferences >> []

        TailoringReference tr = Mock()
        tr.referenceType >> TailoringReferenceType.COPY
        tr.owner >> item1
        tr.catalogItem >> item2

        item1.tailoringReferences >> [tr]

        existingDomain.catalogs >> [catalog]
        catalog.domainTemplate >> existingDomain
        catalog.catalogItems >> [item1]

        when:
        def output = usecase.execute(new InputData(existingClient, existingUnit.id, [item1.id]))
        then:
        output.references.size() == 2
        output.references.first().item == item1
        output.references.first().references == []
    }

    def "get the apply information for a catalog-item with link"() {
        given:

        Control control2 = Mock()

        def id2 = Key.newUuid()
        item2.id >> id2
        item2.catalog >> catalog
        item2.element>>control2

        TailoringReference tr = Mock()
        tr.referenceType >> TailoringReferenceType.LINK
        tr.owner >> item1
        tr.catalogItem >> item2

        item1.tailoringReferences >> [tr]
        item1.element >> control

        CustomLink link = Mock()
        link.type >> "link.type"
        link.target>>control2
        control.links >> [link]

        existingDomain.catalogs >> [catalog]
        catalog.domainTemplate >> existingDomain
        catalog.catalogItems >> [item1, item2]

        when:
        def output = usecase.execute(new InputData(existingClient, existingUnit.id, [item1.id]))
        then:
        output.references.size()== 1
        output.references.first().item == item1
        output.references.first().references.size() == 1
        output.references.first().references.first().referenceKey == "link.type"
    }

    def "get the apply information for a catalog-item with link to an unknown feature "() {
        given:

        Catalogable someThing = Mock()
        item1.element >> someThing

        TailoringReference tr = Mock()
        tr.referenceType >> TailoringReferenceType.LINK
        tr.owner >> item1
        tr.catalogItem >> item1
        item1.tailoringReferences >> [tr]

        Control control2 = Mock()

        CustomLink link = Mock()
        link.type >> "link.type"
        link.target>> control2
        control.links >> [link]

        existingDomain.catalogs >> [catalog]
        catalog.domainTemplate >> existingDomain
        catalog.catalogItems >> [item1, item2]

        when:
        def output = usecase.execute(new InputData(existingClient, existingUnit.id, [item1.id]))
        then:
        output.references.size()== 1
        output.references.first().item == item1
        output.references.first().references.size() == 1
        output.references.first().references.first().referenceKey == "unknown"
    }

    def "wrong unit"() {
        given:

        item1.tailoringReferences >> []

        existingDomain.catalogs >> [catalog]
        catalog.domainTemplate >> existingDomain
        catalog.catalogItems >> [item1]

        def unitId = Key.newUuid()
        Unit anotherUnit = Mock()
        anotherUnit.id >> unitId

        when:
        def output = usecase.execute(new InputData(existingClient, anotherUnit.id, [item1.id]))
        then:
        thrown(NotFoundException)
    }

    def "wrong item id"() {
        given:

        item1.tailoringReferences >> []

        existingDomain.catalogs >> [catalog]
        catalog.domainTemplate >> existingDomain
        catalog.catalogItems >> [item1]

        when:
        def output = usecase.execute(new InputData(existingClient, existingUnit.id, [Key.newUuid()]))
        then:
        thrown(NotFoundException)
    }

    def "not unique items"() {
        given:

        item1.tailoringReferences >> []

        existingDomain.catalogs >> [catalog]
        catalog.domainTemplate >> existingDomain
        catalog.catalogItems >> [item1]

        when:
        def output = usecase.execute(new InputData(existingClient, existingUnit.id, [item1.id, item1.id]))
        then:
        thrown(IllegalArgumentException)
    }
}
