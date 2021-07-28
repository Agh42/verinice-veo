/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2021  Urs Zeidler
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
package org.veo.adapter.service.domaintemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.veo.adapter.ModelObjectReferenceResolver;
import org.veo.adapter.presenter.api.common.ModelObjectReference;
import org.veo.adapter.presenter.api.dto.CustomLinkDto;
import org.veo.adapter.presenter.api.dto.EntityLayerSupertypeDto;
import org.veo.adapter.presenter.api.response.IdentifiableDto;
import org.veo.adapter.presenter.api.response.transformer.DtoToEntityTransformer;
import org.veo.core.entity.Catalogable;
import org.veo.core.entity.ModelObject;
import org.veo.core.entity.exception.NotFoundException;

class PlaceholderResolver extends ModelObjectReferenceResolver {
    Map<String, ModelObject> cache = new HashMap<>();
    Map<String, IdentifiableDto> dtoCache = new HashMap<>();

    private final DtoToEntityTransformer entityTransformer;

    PlaceholderResolver(DtoToEntityTransformer entityTransformer) {
        super(null, null);
        this.entityTransformer = entityTransformer;
    }

    @Override
    public <TEntity extends ModelObject> TEntity resolve(
            ModelObjectReference<TEntity> objectReference) throws NotFoundException {
        if (objectReference == null) {
            return null;
        }
        String id = objectReference.getId();
        ModelObject modelObject = cache.computeIfAbsent(id,
                                                        a -> createElement(id,
                                                                           objectReference.getType()));
        return (TEntity) modelObject;
    }

    @Override
    public <TEntity extends ModelObject> Set<TEntity> resolve(
            Set<ModelObjectReference<TEntity>> objectReferences) {

        return objectReferences.stream()
                               .map(o -> resolve(o))
                               .collect(Collectors.toSet());
    }

    /**
     * Creates the missing element from the dto in the cache.
     */
    private ModelObject createElement(String id, Class<? extends ModelObject> type) {
        IdentifiableDto catalogableDto = dtoCache.get(id);
        if (catalogableDto != null) {
            EntityLayerSupertypeDto es = (EntityLayerSupertypeDto) catalogableDto;
            HashMap<String, List<CustomLinkDto>> hashMap = new HashMap<>(es.getLinks());
            es.getLinks()
              .clear();
            Catalogable catalogable = entityTransformer.transformDto2Catalogable(es, this);
            es.getLinks()
              .putAll(hashMap);
            return catalogable;
        }
        throw new IllegalArgumentException("Unknown type (not dtoCached):" + type + "  id:" + id);
    }

}