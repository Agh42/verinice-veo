/*******************************************************************************
 * Copyright (c) 2018 Daniel Murygin.
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
package org.veo.service.ie;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This type ID mapper maps IDs from verinice archives (VNA) to IDs from veo
 * schemas.
 */
@Service("SchemaTypeIdMapper")
public class SchemaTypeIdMapper implements TypeIdMapper {

    @Autowired
    VnaTypeMappingProperties properties;

    @Override
    public String getVeoElementTypeId(String vnaElementTypeId) {
        return getPropertyValue(vnaElementTypeId);
    }

    @Override
    public String getVeoPropertyTypeId(String vnaPropertyTypeid) {
        return getPropertyValue(vnaPropertyTypeid);
    }

    private String getPropertyValue(String vnaElementTypeId) {
        return properties.getValue(vnaElementTypeId);
    }

}