/*******************************************************************************
 * Copyright (c) 2019 Alexander Koderman.
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

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.Value;
import lombok.With;

import org.modelmapper.ModelMapper;

import org.veo.core.entity.asset.Asset;

/**
 * Transfer object for assets when used as a collection of assets that is
 * included inside other objects.
 *
 * Contains just the display name and the ID of the asset. With this the asset
 * can be listed by name and queried if more information about it is needed.
 *
 * @author akoderman
 *
 */
@Value
public class AssetItemDto {

    @Pattern(regexp = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}",
             flags = Pattern.Flag.CASE_INSENSITIVE,
             message = "ID must either be null (for new processes) or a valid UUID string following RFC 4122. ")

    @NotNull
    private String id;

    @NotNull(message = "A name must be present.")
    @NotBlank
    @Size(min = 1, max = 255, message = "The name must be between 1 and 255 characters long.")
    @With
    private String name;

    public static AssetItemDto from(Asset asset) {
        ModelMapper mapper = new ModelMapper();
        return mapper.map(asset, AssetItemDto.class);
    }
}