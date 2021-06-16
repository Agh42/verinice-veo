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
package org.veo.core.entity;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Defines the basic properties of nameable elements.
 */
public interface Nameable extends Displayable {

    int DESCRIPTION_MAX_LENGTH = 65535;

    String getName();

    void setName(String aName);

    String getAbbreviation();

    void setAbbreviation(String aAbbreviation);

    String getDescription();

    void setDescription(String aDescription);

    /**
     * A default implementation to render a user friendly display name.
     */
    default String getDisplayName() {
        Stream<String> parts = Stream.of(getName());
        String abbreviation = getAbbreviation();
        if (abbreviation != null) {
            parts = Stream.concat(Stream.of(abbreviation), parts);
        }
        if (this instanceof Designated) {
            parts = Stream.concat(Stream.of(((Designated) this).getDesignator()), parts);
        }
        return parts.collect(Collectors.joining(" "));
    }
}
