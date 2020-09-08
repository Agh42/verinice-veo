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
package org.veo.adapter.presenter.api.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.veo.adapter.presenter.api.common.ModelObjectReference;
import org.veo.adapter.presenter.api.response.transformer.DtoToEntityContext;
import org.veo.core.entity.Domain;
import org.veo.core.entity.ModelObject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

/**
 * Base transfer object for Domains. Contains common data for all Domain DTOs.
 */
@Data
@ToString(onlyExplicitlyIncluded = true, callSuper = true)
abstract public class AbstractDomainDto implements NameAbleDto, VersionedDto {

    @NotNull(message = "A name must be present.")
    @Schema(description = "The name for the Domain.", example = "Data protection", required = true)
    @ToString.Include
    private String name;

    @Schema(description = "The abbreviation for the Domain.",
            example = "Data prot.",
            required = false)
    private String abbreviation;

    @Schema(description = "The description for the Domain.",
            example = "Everything around data protection.",
            required = false)
    private String description;

    @Schema(description = "A timestamp acc. to RFC 3339 specifying when this version of the entity was saved.",
            example = "1990-12-31T23:59:60Z")
    @Pattern(regexp = "(\\d{4}-\\d{2}-\\d{2}[Tt]\\d{2}:\\d{2}:\\d{2}(\\.\\d{0,2})?([zZ]|[+-]\\d{2}:\\d{2}))")
    private String validFrom;

    @Schema(description = "The active for the Domain.")
    private Boolean active;

    public Collection<ModelObjectReference<? extends ModelObject>> getReferences() {
        List<ModelObjectReference<? extends ModelObject>> list = new ArrayList<>();
        return list;
    }

    public Boolean isActive() {
        return active;
    }

    public abstract Domain toEntity(DtoToEntityContext tcontext);
}