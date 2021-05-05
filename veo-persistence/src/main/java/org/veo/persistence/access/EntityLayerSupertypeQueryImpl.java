/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2020  Jonas Jordan.
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
package org.veo.persistence.access;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import org.veo.core.entity.Client;
import org.veo.core.entity.EntityLayerSupertype;
import org.veo.core.entity.Unit;
import org.veo.core.repository.PagedResult;
import org.veo.core.repository.PagingConfiguration;
import org.veo.core.usecase.repository.EntityLayerSupertypeQuery;
import org.veo.persistence.access.jpa.EntityLayerSupertypeDataRepository;
import org.veo.persistence.entity.jpa.EntityLayerSupertypeData;
import org.veo.persistence.entity.jpa.UnitData;

/**
 * Implements {@link EntityLayerSupertypeQuery} using {@link Specification} API.
 */
public class EntityLayerSupertypeQueryImpl<TInterface extends EntityLayerSupertype, TDataClass extends EntityLayerSupertypeData>
        implements EntityLayerSupertypeQuery<TInterface> {

    private final EntityLayerSupertypeDataRepository<TDataClass> dataRepository;
    private Specification<TDataClass> mySpec;

    public EntityLayerSupertypeQueryImpl(EntityLayerSupertypeDataRepository<TDataClass> repo,
            Client client) {
        this.dataRepository = repo;
        mySpec = createSpecification(client);
    }

    @Override
    public void whereUnitIn(Set<Unit> units) {
        mySpec = mySpec.and((root, query, criteriaBuilder) -> in(root.get("owner"), units,
                                                                 criteriaBuilder));
    }

    @Override
    public void whereSubTypeIn(Set<String> values) {
        mySpec = mySpec.and((root, query,
                criteriaBuilder) -> in(root.join("subTypeAspects", JoinType.LEFT)
                                           .get("subType"),
                                       values, criteriaBuilder));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<TInterface> execute(PagingConfiguration pagingConfiguration) {
        Page<TDataClass> items = dataRepository.findAll(mySpec, toPageable(pagingConfiguration));
        List<String> ids = items.stream()
                                .map(EntityLayerSupertype::getDbId)
                                .collect(Collectors.toList());
        List<TDataClass> fullyLoadedItems = dataRepository.findAllById(ids);
        fullyLoadedItems.sort(Comparator.comparingInt(item -> ids.indexOf(item.getDbId())));

        return new PagedResult<>(pagingConfiguration, (List<TInterface>) fullyLoadedItems,
                items.getTotalElements(), items.getTotalPages());
    }

    private Specification<TDataClass> createSpecification(Client client) {
        return (root, query, criteriaBuilder) -> {
            Path<UnitData> unit = criteriaBuilder.treat(root.join("owner"), UnitData.class);
            return criteriaBuilder.equal(unit.get("client"), client);
        };
    }

    private Predicate in(Path<Object> column, Set<?> values, CriteriaBuilder criteriaBuilder) {
        if (values.stream()
                  .anyMatch(Objects::isNull)) {
            return criteriaBuilder.or(column.in(values), column.isNull());
        } else {
            return criteriaBuilder.isTrue(column.in(values));
        }
    }

    protected static Pageable toPageable(PagingConfiguration pagingConfiguration) {
        return PageRequest.of(pagingConfiguration.getPageNumber(),
                              pagingConfiguration.getPageSize(),
                              pagingConfiguration.getSortOrder() == PagingConfiguration.SortOrder.ASCENDING
                                      ? Direction.ASC
                                      : Direction.DESC,
                              pagingConfiguration.getSortColumn());
    }
}
