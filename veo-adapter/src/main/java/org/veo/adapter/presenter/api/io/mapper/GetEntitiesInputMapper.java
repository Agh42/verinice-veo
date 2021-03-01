/*******************************************************************************
 * Copyright (c) 2021 Jonas Jordan.
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
package org.veo.adapter.presenter.api.io.mapper;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.veo.adapter.presenter.api.dto.QueryConditionDto;
import org.veo.adapter.presenter.api.dto.SearchQueryDto;
import org.veo.adapter.presenter.api.dto.UuidQueryConditionDto;
import org.veo.core.entity.Client;
import org.veo.core.entity.Key;
import org.veo.core.usecase.base.GetEntitiesUseCase;
import org.veo.core.usecase.base.QueryCondition;

public class GetEntitiesInputMapper {

    public static GetEntitiesUseCase.InputData map(Client client, String unitUuid,
            String displayName, String subType) {
        return new GetEntitiesUseCase.InputData(client, createUuidCondition(unitUuid),
                createStringFilter(displayName), createNonEmptyCondition(subType));
    }

    public static GetEntitiesUseCase.InputData map(Client client, SearchQueryDto searchQuery) {
        return new GetEntitiesUseCase.InputData(client, transformCondition(searchQuery.getUnitId()),
                transformCondition(searchQuery.getDisplayName()),
                transformCondition(searchQuery.getSubType()));
    }

    private static QueryCondition<Key<UUID>> transformCondition(UuidQueryConditionDto filterDto) {
        if (filterDto != null) {
            return new QueryCondition<>(filterDto.values.stream()
                                                        .map(Key::uuidFrom)
                                                        .collect(Collectors.toSet()));
        }
        return null;
    }

    private static QueryCondition<String> transformCondition(QueryConditionDto<String> filterDto) {
        if (filterDto != null) {
            return new QueryCondition<>(filterDto.values);
        }
        return null;
    }

    private static QueryCondition<String> createNonEmptyCondition(String value) {
        if (value == null) {
            return null;
        }
        // Empty string -> match against null.
        if (value.equals("")) {
            return new QueryCondition<>(Collections.singleton(null));
        }
        return new QueryCondition<>(Set.of(value));
    }

    private static QueryCondition<String> createStringFilter(String value) {
        if (value != null) {
            return new QueryCondition<>(Set.of(value));
        }
        return null;
    }

    private static QueryCondition<Key<UUID>> createUuidCondition(String value) {
        if (value != null) {
            return new QueryCondition<>(Set.of(Key.uuidFrom(value)));
        }
        return null;
    }
}