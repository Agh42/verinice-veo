/*******************************************************************************
 * Copyright (c) 2020 Jonas Jordan.
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
package org.veo.core.usecase.base;

import java.util.UUID;

import javax.validation.Valid;

import org.veo.core.entity.Client;
import org.veo.core.entity.EntityLayerSupertype;
import org.veo.core.entity.Key;
import org.veo.core.entity.Unit;
import org.veo.core.entity.exception.NotFoundException;
import org.veo.core.usecase.UseCase;
import org.veo.core.usecase.repository.Repository;
import org.veo.core.usecase.repository.UnitRepository;

import lombok.Value;

abstract public class CreateEntityUseCase<TEntity extends EntityLayerSupertype> extends
        UseCase<CreateEntityUseCase.InputData<TEntity>, CreateEntityUseCase.OutputData<TEntity>> {
    private final UnitRepository unitRepository;
    private final Repository<TEntity, Key<UUID>> entityRepo;

    public CreateEntityUseCase(UnitRepository unitRepository,
            Repository<TEntity, Key<UUID>> entityRepo) {
        this.unitRepository = unitRepository;
        this.entityRepo = entityRepo;
    }

    @Override
    public CreateEntityUseCase.OutputData<TEntity> execute(
            CreateEntityUseCase.InputData<TEntity> input) {
        var entity = input.getNewEntity();
        entity.setId(Key.newUuid());
        Unit unit = unitRepository.findById(entity.getOwner()
                                                  .getId())
                                  .orElseThrow(() -> new NotFoundException("Unit %s not found.",
                                          entity.getOwner()
                                                .getId()
                                                .uuidValue()));
        unit.checkSameClient(input.authenticatedClient);
        entity.version(input.username, null);
        return new CreateEntityUseCase.OutputData<>(entityRepo.save(entity));
    }

    @Valid
    @Value
    public static class InputData<TEntity> implements UseCase.InputData {
        TEntity newEntity;
        Client authenticatedClient;
        String username;
    }

    @Valid
    @Value
    public static class OutputData<TEntity> implements UseCase.OutputData {
        @Valid
        TEntity entity;
    }
}