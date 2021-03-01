/*******************************************************************************
 * Copyright (c) 2020 Jochen Kemnade.
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
package org.veo.core.entity;

import java.util.Set;

/**
 * A group of objects that form a logical unit
 */
public interface Scope extends EntityLayerSupertype {

    @Override
    default String getModelType() {
        return EntityTypeNames.SCOPE;
    }

    @Override
    default Class<? extends ModelObject> getModelInterface() {
        return Scope.class;
    }

    Set<EntityLayerSupertype> getMembers();

    default boolean addMember(EntityLayerSupertype member) {
        return getMembers().add(member);
    }

    default boolean addMembers(Set<EntityLayerSupertype> members) {
        return getMembers().addAll(members);
    }

    default boolean removeMember(EntityLayerSupertype member) {
        return getMembers().remove(member);
    }

    default boolean removeMembers(Set<EntityLayerSupertype> members) {
        return getMembers().removeAll(members);
    }

    default void setMembers(Set<EntityLayerSupertype> members) {
        getMembers().clear();
        getMembers().addAll(members);
    }

}