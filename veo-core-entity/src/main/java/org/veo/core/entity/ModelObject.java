/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2020  Urs Zeidler.
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

import java.util.UUID;

/**
 * Base type for entities that act as aggregate roots and have a UUID
 */
public interface ModelObject extends Versioned {
    Key<UUID> getId();

    /**
     * @return Lowercase singular name for the specific model object type
     */
    String getModelType();

    void setId(Key<UUID> id);

    /**
     * @return The specific interface for this type of model object
     */
    Class<? extends ModelObject> getModelInterface();
}