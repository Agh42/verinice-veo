/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2019  Urs Zeidler.
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
package org.veo.adapter.presenter.api.io.mapper;

import java.util.Optional;

import org.veo.adapter.presenter.api.common.ApiResponseBody;
import org.veo.core.entity.Key;
import org.veo.core.entity.ModelObject;

/**
 * Returns just the id of the newly created ModelObject as output.
 */
public final class CreateOutputMapper {

    public static ApiResponseBody map(ModelObject modelObject) {
        Optional<String> id = Optional.ofNullable(modelObject.getId())
                                      .map(Key::uuidValue);
        return new ApiResponseBody(true, id,
                String.format("%s created successfully.", upperFirst(modelObject.getModelType())));
    }

    private static String upperFirst(String in) {
        if (in.isEmpty()) {
            return "";
        }
        return in.substring(0, 1)
                 .toUpperCase()
                + in.substring(1);
    }
}
