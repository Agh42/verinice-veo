/*******************************************************************************
 * Copyright (c) 2021 Jochen Kemnade.
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
package org.veo.adapter.presenter.api.openapi;

import org.veo.adapter.presenter.api.common.IModelObjectReference;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Swagger documentation for a rerefence to a composite entity's part:
 */
@Schema(name = "PartReference", description = "A reference to an entity's part")
public interface ModelObjectReferencePart extends IModelObjectReference {

    @Schema(description = "A friendly human readable title of the referenced entity.",
            example = "My Entity")
    @Override
    String getDisplayName();

    @Schema(required = true,
            description = "The resource URL of the referenced entity.",
            example = "http://<api.example.org>/api/v1/asset/<00000000-0000-0000-0000-000000000000>")
    @Override
    String getTargetUri();

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    String getSearchesUri();

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    String getResourcesUri();
}