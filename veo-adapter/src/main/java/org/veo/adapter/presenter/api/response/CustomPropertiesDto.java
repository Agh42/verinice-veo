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
package org.veo.adapter.presenter.api.response;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import org.veo.adapter.presenter.api.common.ModelObjectReference;
import org.veo.adapter.presenter.api.openapi.ModelObjectReferenceCustomPropertiesDomains;
import org.veo.core.entity.Domain;
import org.veo.core.entity.ModelObject;

/**
 * Transfer object for complete CustomPropertiess.
 *
 * Contains all information of the CustomProperties.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(onlyExplicitlyIncluded = true, callSuper = true)
public class CustomPropertiesDto extends BaseModelObjectDto {

    // TODO Add an example for the API documentation for field type.
    @Schema(description = "The type for the CustomProperties.",
            example = "<add example here>",
            required = false)
    @ToString.Include
    private String type;

    // TODO Add an example for the API documentation for field applicableTo.
    @Schema(description = "The applicableTo for the CustomProperties.",
            example = "<add example here>",
            required = false)
    private Set<String> applicableTo = Collections.emptySet();

    @ArraySchema(schema = @Schema(implementation = ModelObjectReferenceCustomPropertiesDomains.class))

    private Set<ModelObjectReference<Domain>> domains = Collections.emptySet();

    public Collection<ModelObjectReference<? extends ModelObject>> getReferences() {
        List<ModelObjectReference<? extends ModelObject>> list = new ArrayList<>();
        list.addAll(getDomains());
        return list;
    }

    // maybe this is the best way to handle this.
    // we can use the map here and the transform function creates the properties in
    // the entity
    // so no need for dtoProperties

    @Schema(description = "The properties of the element described by the schema of the type attribute.",
            example = " name: 'value'",
            required = false)
    private Map<String, ?> attributes = new HashMap<>();

    public Map<String, ?> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, ?> attributes) {
        this.attributes = attributes;
    }

}