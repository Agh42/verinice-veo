/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2020  Jonas Jordan.
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
package org.veo.persistence.access;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import org.veo.core.entity.Key;
import org.veo.core.entity.ModelObject;
import org.veo.core.repository.Repository;
import org.veo.persistence.entity.jpa.BaseModelObjectData;
import org.veo.persistence.entity.jpa.ModelObjectValidation;

@Transactional(readOnly = true)
abstract class AbstractModelObjectRepository<T extends ModelObject, S extends BaseModelObjectData>
        implements Repository<T, Key<UUID>> {
    protected final CrudRepository<S, String> dataRepository;
    protected final ModelObjectValidation validation;

    protected AbstractModelObjectRepository(CrudRepository<S, String> dataRepository,
            ModelObjectValidation validation) {
        this.dataRepository = dataRepository;
        this.validation = validation;
    }

    @Override
    @Transactional
    public T save(T entity) {
        validation.validateModelObject(entity);
        return (T) dataRepository.save((S) entity);
    }

    @Override
    @Transactional
    public List<T> saveAll(Set<T> entities) {
        entities.forEach(validation::validateModelObject);
        return (List<T>) dataRepository.saveAll((Set<S>) entities);
    }

    @Override
    public Optional<T> findById(Key<UUID> id) {
        return (Optional<T>) dataRepository.findById(id.uuidValue());
    }

    @Override
    @Transactional
    public void delete(T entity) {
        deleteById(entity.getId());
    }

    @Override
    @Transactional
    public void deleteById(Key<UUID> id) {
        dataRepository.deleteById(id.uuidValue());
    }

    @Override
    public boolean exists(Key<UUID> id) {
        return dataRepository.existsById(id.uuidValue());
    }

    @Override
    public Set<T> getByIds(Set<Key<UUID>> ids) {
        var idStrings = ids.stream()
                           .map(Key::uuidValue)
                           .collect(Collectors.toList());
        return StreamSupport.stream(dataRepository.findAllById(idStrings)
                                                  .spliterator(),
                                    false)
                            .map(e -> (T) e)
                            .collect(Collectors.toSet());
    }
}
