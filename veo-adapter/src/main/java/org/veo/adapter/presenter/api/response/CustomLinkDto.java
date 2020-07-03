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
import java.util.List;

import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import io.swagger.v3.oas.annotations.media.Schema;

import org.veo.adapter.presenter.api.common.ModelObjectReference;
import org.veo.adapter.presenter.api.openapi.ModelObjectReferenceCustomLinkSource;
import org.veo.adapter.presenter.api.openapi.ModelObjectReferenceCustomLinkTarget;
import org.veo.core.entity.EntityLayerSupertype;
import org.veo.core.entity.ModelObject;

/**
 * Transfer object for complete CustomLinks.
 *
 * Contains all information of the CustomLink.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(onlyExplicitlyIncluded = true, callSuper = true)
public class CustomLinkDto extends CustomPropertiesDto implements NameAbleDto {

    @NotNull(message = "A name must be present.")
    @Schema(description = "The name for the CustomLink.",
            example = "My custom relation",
            required = true)
    @ToString.Include
    private String name;

    @Schema(description = "The abbreviation for the CustomLink.",
            example = "Relation",
            required = false)
    private String abbreviation;

    @Schema(description = "The description for the CustomLink.",
            example = "This is a custom link.",
            required = false)
    private String description;

    @NotNull(message = "A target must be present.")
    @ToString.Include
    @Schema(required = true, implementation = ModelObjectReferenceCustomLinkTarget.class)
    private ModelObjectReference<EntityLayerSupertype> target;
    @ToString.Include
    @NotNull(message = "A source must be present.")
    @Schema(required = true,
            implementation = ModelObjectReferenceCustomLinkSource.class,
            example = "The source entity")
    private ModelObjectReference<EntityLayerSupertype> source;

    public Collection<ModelObjectReference<? extends ModelObject>> getReferences() {
        List<ModelObjectReference<? extends ModelObject>> list = new ArrayList<>();
        list.addAll(getDomains());
        list.add(getTarget());
        list.add(getSource());
        return list;
    }

}