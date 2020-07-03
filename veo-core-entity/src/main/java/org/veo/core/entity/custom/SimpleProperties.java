/*******************************************************************************
 * Copyright (c) 2020 Urs Zeidler.
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
package org.veo.core.entity.custom;

import java.util.UUID;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.veo.core.entity.Key;
import org.veo.core.entity.ModelPackage;
import org.veo.core.entity.impl.CustomPropertiesImpl;

/**
 * @author urszeidler
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@ToString(onlyExplicitlyIncluded = true, callSuper = true)
public class SimpleProperties extends CustomPropertiesImpl {
    public SimpleProperties() {
        this(Key.newUuid());
    }

    public SimpleProperties(Key<UUID> id) {
        super(id);
    }

    @Override
    public String getModelType() {
        return ModelPackage.ELEMENT_CUSTOMPROPERTIES;
    }
}