/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2022  Jonas Jordan
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
package org.veo.core.entity.decision;

import java.util.Collection;

import org.veo.core.entity.Domain;
import org.veo.core.entity.Element;

import lombok.RequiredArgsConstructor;

/**
 * Provides the size/length of a collection value for a certain custom aspect
 * attribute on an element.
 */
@RequiredArgsConstructor
public class CustomAspectAttributeSizeProvider implements InputProvider {
    private final String customAspectType;
    private final String attributeType;

    @Override
    public Object getValue(Element element, Domain domain) {
        var value = new CustomAspectAttributeValueProvider(customAspectType,
                attributeType).getValue(element, domain);

        if (value == null) {
            return 0;
        }
        if (value instanceof Collection<?>) {
            return ((Collection) value).size();
        }
        throw new IllegalArgumentException(
                String.format("Cannot determine size for custom aspect %s attribute %s because the value is not a collection",
                              customAspectType, attributeType));
    }
}
