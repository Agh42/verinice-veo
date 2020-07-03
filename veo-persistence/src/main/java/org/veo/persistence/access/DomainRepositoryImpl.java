/*******************************************************************************
 * Copyright (c) 2019 Urs Zeidler.
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
package org.veo.persistence.access;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import lombok.AllArgsConstructor;

import org.springframework.stereotype.Repository;

import org.veo.core.entity.Domain;
import org.veo.core.entity.Key;
import org.veo.core.entity.transform.TransformEntityToTargetContext;
import org.veo.core.entity.transform.TransformTargetToEntityContext;
import org.veo.core.usecase.repository.DomainRepository;
import org.veo.persistence.access.jpa.DomainDataRepository;
import org.veo.persistence.entity.jpa.DomainData;
import org.veo.persistence.entity.jpa.ModelObjectValidation;
import org.veo.persistence.entity.jpa.transformer.DataEntityToTargetContext;
import org.veo.persistence.entity.jpa.transformer.DataTargetToEntityContext;

@Repository
@AllArgsConstructor
public class DomainRepositoryImpl implements DomainRepository {

    // public Collection<DomainData> findByNameContainingIgnoreCase(String search);

    private DomainDataRepository dataRepository;

    private ModelObjectValidation validation;

    @Override
    public Domain save(Domain domain, TransformEntityToTargetContext entityToDataContext,
            TransformTargetToEntityContext dataToEntityContext) {
        validation.validateModelObject(domain);
        return dataRepository.save(DomainData.from(domain, Optional.ofNullable(entityToDataContext)
                                                                   .orElseGet(DataEntityToTargetContext::getCompleteTransformationContext)))
                             .toDomain(Optional.ofNullable(dataToEntityContext)
                                               .orElseGet(DataTargetToEntityContext::getCompleteTransformationContext));
    }

    @Override
    public Optional<Domain> findById(Key<UUID> id) {
        return findById(id, null);
    }

    @Override
    public Optional<Domain> findById(Key<UUID> id,
            TransformTargetToEntityContext dataToEntityContext) {
        TransformTargetToEntityContext context = Optional.ofNullable(dataToEntityContext)
                                                         .orElseGet(DataTargetToEntityContext::getCompleteTransformationContext);

        return dataRepository.findById(id.uuidValue())
                             .map(data -> data.toDomain(context));

    }

    @Override
    public List<Domain> findByName(String search) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void delete(Domain entity) {
        dataRepository.delete(DomainData.from(entity));
    }

    @Override
    public void deleteById(Key<UUID> id) {
        dataRepository.deleteById(id.uuidValue());
    }

    @Override
    public boolean exists(Key<UUID> id) {
        return dataRepository.existsById(id.uuidValue());
    }

    @Override
    public Set<Domain> getByIds(Set<Key<UUID>> ids) {
        // TODO Auto-generated method stub
        return null;
    }

}