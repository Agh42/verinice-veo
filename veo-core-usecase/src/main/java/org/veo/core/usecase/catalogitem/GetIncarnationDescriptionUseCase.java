/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2021  Urs Zeidler.
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
package org.veo.core.usecase.catalogitem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.veo.core.entity.CatalogItem;
import org.veo.core.entity.Client;
import org.veo.core.entity.Domain;
import org.veo.core.entity.Element;
import org.veo.core.entity.Key;
import org.veo.core.entity.LinkTailoringReference;
import org.veo.core.entity.TailoringReferenceTyped;
import org.veo.core.entity.Unit;
import org.veo.core.entity.exception.NotFoundException;
import org.veo.core.entity.exception.RuntimeModelException;
import org.veo.core.entity.util.TailoringReferenceComparators;
import org.veo.core.repository.CatalogItemRepository;
import org.veo.core.repository.ElementQuery;
import org.veo.core.repository.ElementRepository;
import org.veo.core.repository.PagedResult;
import org.veo.core.repository.PagingConfiguration;
import org.veo.core.repository.UnitRepository;
import org.veo.core.usecase.TransactionalUseCase;
import org.veo.core.usecase.UseCase;
import org.veo.core.usecase.UseCaseTools;
import org.veo.core.usecase.parameter.IncarnateCatalogItemDescription;
import org.veo.core.usecase.parameter.TailoringReferenceParameter;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class GetIncarnationDescriptionUseCase implements
        TransactionalUseCase<GetIncarnationDescriptionUseCase.InputData, GetIncarnationDescriptionUseCase.OutputData> {
    private final UnitRepository unitRepository;
    private final CatalogItemRepository catalogItemRepository;
    private final org.veo.core.repository.RepositoryProvider repositoryProvider;

    @Override
    public OutputData execute(InputData input) {
        log.info("GetIncarnationDescriptionUseCase: {}", input);
        Unit unit = unitRepository.findById(input.getContainerId())
                                  .orElseThrow(() -> new NotFoundException("Unit %s not found.",
                                          input.getContainerId()));
        unit.checkSameClient(input.authenticatedClient);

        validateInput(input);
        List<CatalogItem> itemsToCreate = input.getCatalogItemIds()
                                               .stream()
                                               .map(id -> catalogItemRepository.findById(id)
                                                                               .orElseThrow(() -> new NotFoundException(
                                                                                       "CatalogItem not found %s",
                                                                                       id)))
                                               .flatMap(ci -> ci.getAllElementsToCreate()
                                                                .stream())
                                               .collect(Collectors.toList());

        List<IncarnateCatalogItemDescription> incarnationDescriptions = itemsToCreate.stream()
                                                                                     .map(catalogItem -> {
                                                                                         Domain domain = (Domain) catalogItem.getCatalog()
                                                                                                                             .getDomainTemplate();
                                                                                         UseCaseTools.checkDomainBelongsToClient(input.getAuthenticatedClient(),
                                                                                                                                 domain);
                                                                                         List<TailoringReferenceParameter> parameters = toTailorreferenceParameters(unit,
                                                                                                                                                                    catalogItem);
                                                                                         return new IncarnateCatalogItemDescription(
                                                                                                 catalogItem,
                                                                                                 parameters);

                                                                                     })
                                                                                     .collect(Collectors.toList());
        log.info("GetIncarnationDescriptionUseCase IncarnationDescription: {}",
                 incarnationDescriptions);
        return new OutputData(incarnationDescriptions, unit);
    }

    private void validateInput(InputData input) {
        if (input.catalogItemIds.stream()
                                .collect(Collectors.toSet())
                                .size() != input.catalogItemIds.size()) {
            throw new IllegalArgumentException("Provided catalogitems are not unique.");
        }
    }

    private List<TailoringReferenceParameter> toTailorreferenceParameters(Unit unit,
            CatalogItem catalogItem) {
        return catalogItem.getTailoringReferences()
                          .stream()
                          .filter(TailoringReferenceTyped.IS_ALL_LINK_PREDICATE)
                          .sorted(TailoringReferenceComparators.BY_EXECUTION)
                          .map(LinkTailoringReference.class::cast)
                          .map(lr -> toParameter(unit, lr))
                          .collect(Collectors.toList());
    }

    /**
     * Create the parameter object for this {@link LinkTailoringReference} it also
     * adds the suggestion found by
     * {@link #findReferencedAppliedItem(Unit, CatalogItem)} in the reference.
     */
    private TailoringReferenceParameter toParameter(Unit unit,
            LinkTailoringReference linkReference) {
        if (linkReference.getLinkType() == null) {
            throw new RuntimeModelException(
                    "LinkType should not be null affected TailoringReferences: "
                            + linkReference.getId());
        }
        TailoringReferenceParameter tailoringReferenceParameter = new TailoringReferenceParameter(
                linkReference.getReferenceType(), linkReference.getLinkType());
        findReferencedAppliedItem(unit,
                                  linkReference.getCatalogItem()).ifPresent(tailoringReferenceParameter::setReferencedElement);

        return tailoringReferenceParameter;
    }

    private Optional<Element> findReferencedAppliedItem(Unit unit, CatalogItem catalogItem) {
        List<Element> list = findReferencedAppliedItems(unit, catalogItem);
        return list.size() == 0 ? Optional.empty() : Optional.of(list.get(0));
    }

    /**
     * Searches for {@link Element} in the unit which have the given catalogItem
     * applied.
     */
    private List<Element> findReferencedAppliedItems(Unit unit, CatalogItem catalogItem) {
        return findReferencedAppliedItems(unit, catalogItem,
                                          PagingConfiguration.UNPAGED).getResultPage();
    }

    /**
     * Searches for {@link Element} in the unit which have the given catalogItem
     * applied.
     */
    private PagedResult<Element> findReferencedAppliedItems(Unit unit, CatalogItem catalogItem,
            PagingConfiguration pagingConfiguration) {
        ElementRepository<Element> repository = repositoryProvider.getElementRepositoryFor((Class<Element>) catalogItem.getElement()
                                                                                                                       .getModelInterface());
        ElementQuery<Element> query = repository.query(unit.getClient());
        query.whereOwnerIs(unit);
        query.whereAppliedItemsContains(catalogItem);
        return query.execute(pagingConfiguration);
    }

    @Valid
    @Value
    public static class InputData implements UseCase.InputData {
        Client authenticatedClient;
        Key<UUID> containerId;
        List<Key<UUID>> catalogItemIds;
    }

    @Valid
    @Value
    public static class OutputData implements UseCase.OutputData {
        @Valid
        List<IncarnateCatalogItemDescription> references;
        Unit container;
    }

}
