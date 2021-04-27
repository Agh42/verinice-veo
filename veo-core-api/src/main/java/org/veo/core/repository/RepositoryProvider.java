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
package org.veo.core.repository;

import java.util.UUID;

import org.veo.core.entity.EntityLayerSupertype;
import org.veo.core.entity.Key;
import org.veo.core.entity.ModelObject;

/**
 * A service that helps to retrieve the correct {@link Repository} for a given
 * type
 */
public interface RepositoryProvider {

    public <T extends EntityLayerSupertype> EntityLayerSupertypeRepository<T> getEntityLayerSupertypeRepositoryFor(
            Class<T> entityType);

    public <T extends ModelObject> Repository<T, Key<UUID>> getRepositoryFor(Class<T> entityType);
}