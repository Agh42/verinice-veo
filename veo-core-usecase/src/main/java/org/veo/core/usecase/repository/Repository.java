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
package org.veo.core.usecase.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.veo.core.entity.transform.TransformEntityToTargetContext;
import org.veo.core.entity.transform.TransformTargetToEntityContext;

/*
* The repository provides access to business entities through a well-known interface.
*
* It provides methods to add, remove or reconstitute objects and encapsulates
* the actual interaction with the data store.
*
* The repository guarantees all invariants of the entities entity to maintain
* the integrity of the entities and all references. More than a simple data gateway,
* the repository does not just offer CRUD operations but
* uses factories and builders to build entities to specification.
* */
public interface Repository<T, K> {

    public default T save(T entity) {
        return save(entity, null, null);
    }

    public T save(T entity, TransformEntityToTargetContext dataEntityToTargetContext,
            TransformTargetToEntityContext dataTargetToEntityContext);

    public Optional<T> findById(K id);

    public Optional<T> findById(K id, TransformTargetToEntityContext dataTargetToEntityContext);

    public List<T> findByName(String search);

    public Set<T> getByIds(Set<K> ids);

    public void delete(T entity);

    public void deleteById(K id);

    public boolean exists(K id);

}