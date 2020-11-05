/*******************************************************************************
 * Copyright (c) 2020 Alexander Koderman.
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
package org.veo.core.usecase.unit;

import java.util.List;
import java.util.UUID;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.veo.core.entity.Asset;
import org.veo.core.entity.Client;
import org.veo.core.entity.Control;
import org.veo.core.entity.Document;
import org.veo.core.entity.Key;
import org.veo.core.entity.Person;
import org.veo.core.entity.Process;
import org.veo.core.entity.Unit;
import org.veo.core.entity.exception.NotFoundException;
import org.veo.core.usecase.UseCase;
import org.veo.core.usecase.UseCase.EmptyOutput;
import org.veo.core.usecase.repository.ClientRepository;
import org.veo.core.usecase.repository.RepositoryProvider;
import org.veo.core.usecase.repository.UnitRepository;

import lombok.Value;

public class DeleteUnitUseCase<R> extends UseCase<DeleteUnitUseCase.InputData, EmptyOutput> {

    private final ClientRepository clientRepository;
    private final RepositoryProvider repositoryProvider;
    private final UnitRepository unitRepository;

    public DeleteUnitUseCase(ClientRepository clientRepository, UnitRepository unitRepository,
            RepositoryProvider repositoryProvider) {
        super();
        this.clientRepository = clientRepository;
        this.repositoryProvider = repositoryProvider;
        this.unitRepository = unitRepository;
    }

    @Override
    @Transactional
    public EmptyOutput execute(InputData input) {
        Client client = clientRepository.findById(input.getAuthenticatedClient()
                                                       .getId())
                                        .orElseThrow(() -> new NotFoundException(
                                                "Invalid client ID"));

        Unit unit = unitRepository.findById(input.unitId)
                                  .orElseThrow(() -> new NotFoundException("Invalid unit ID"));
        unit.checkSameClient(client);

        removeObjectsInUnit(unit);
        unitRepository.delete(unit);
        return EmptyOutput.INSTANCE;
    }

    @Transactional
    void removeObjectsInUnit(Unit unit) {
        List.of(Asset.class, Control.class, Document.class, Person.class, Process.class)
            .forEach(clazz -> {
                repositoryProvider.getEntityLayerSupertypeRepositoryFor(clazz)
                                  .deleteByUnit(unit);
            });

    }

    @Valid
    @Value
    public static class InputData implements UseCase.InputData {
        Key<UUID> unitId;
        Client authenticatedClient;
    }

}
