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
package org.veo.adapter.presenter.api.request;

import java.util.*;
import java.util.Collections;

import javax.validation.*;
import javax.validation.constraints.*;

import lombok.Data;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.enums.*;
import io.swagger.v3.oas.annotations.media.*;

import org.veo.adapter.presenter.api.common.*;
import org.veo.adapter.presenter.api.openapi.*;
import org.veo.adapter.presenter.api.response.*;
import org.veo.core.entity.*;

@Data
public final class CreateProcessDto {

    @Pattern(regexp = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}",
             flags = Pattern.Flag.CASE_INSENSITIVE,
             message = "ID for new objects must either be null or a valid UUID string following RFC 4122.")
    @Schema(description = "ID must be a valid UUID string following RFC 4122.",
            example = "adf037f1-0089-48ad-9177-92269918758b")
    private String id;

    @Schema(description = "A timestamp acc. to RFC 3339 specifying when this version of the entity was saved.",
            example = "1990-12-31T23:59:60Z")
    @Pattern(regexp = "(\\d{4}-\\d{2}-\\d{2}[Tt]\\d{2}:\\d{2}:\\d{2}(\\.\\d{0,2})?([zZ]|[+-]\\d{2}:\\d{2}))")
    private String validFrom;

    @Schema(description = "A timestamp acc. to RFC 3339 specifying the point in time when this version of the entity was superseded "
            + "by a newer version or deleted. Empty if this is the current version.",
            example = "1990-12-31T23:59:60Z")
    @Pattern(regexp = "(\\d{4}-\\d{2}-\\d{2}[Tt]\\d{2}:\\d{2}:\\d{2}(\\.\\d{0,2})?([zZ]|[+-]\\d{2}:\\d{2}))")
    private String validUntil;

    @NotNull(message = "A name must be present.")
    @Schema(description = "The name for the Process.",
            example = "Two-factor authentication",
            required = true)
    private String name;

    @Schema(description = "The abbreviation for the Process.", example = "2FA")
    private String abbreviation;

    @Schema(description = "The description for the Process.",
            example = "Implement 2FA where possible.",
            required = false)
    private String description;

    @ArraySchema(schema = @Schema(implementation = ModelObjectReferenceProcessDomains.class))

    private Set<ModelObjectReference<Domain>> domains = Collections.emptySet();

    @Schema(description = "The links for the Process.")
    private Set<CustomLinkDto> links = Collections.emptySet();

    @Schema(description = "The customAspects for the Process.")
    private Set<CustomPropertiesDto> customAspects = Collections.emptySet();

    @NotNull(message = "A owner must be present.")
    @Schema(required = true, implementation = ModelObjectReferenceProcessOwner.class)
    private ModelObjectReference<Unit> owner;

}
