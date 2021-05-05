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

/**
 * A link connects two model objects. This link serve only documentation
 * purpose.
 */
public interface CustomLink extends CustomProperties {

    String getName();

    void setName(String name);

    EntityLayerSupertype getTarget();

    void setTarget(EntityLayerSupertype aTarget);

    EntityLayerSupertype getSource();

    void setSource(EntityLayerSupertype aSource);

}
