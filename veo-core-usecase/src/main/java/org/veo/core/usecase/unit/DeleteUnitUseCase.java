/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2020  Alexander Koderman.
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
package org.veo.core.usecase.unit;

import static java.util.function.Predicate.not;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import javax.validation.Valid;

import org.veo.core.entity.Asset;
import org.veo.core.entity.Client;
import org.veo.core.entity.EntityType;
import org.veo.core.entity.Key;
import org.veo.core.entity.Process;
import org.veo.core.entity.Scenario;
import org.veo.core.entity.Scope;
import org.veo.core.entity.Unit;
import org.veo.core.entity.exception.NotFoundException;
import org.veo.core.repository.ClientRepository;
import org.veo.core.repository.RepositoryProvider;
import org.veo.core.repository.UnitRepository;
import org.veo.core.usecase.TransactionalUseCase;
import org.veo.core.usecase.UseCase;
import org.veo.core.usecase.UseCase.EmptyOutput;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DeleteUnitUseCase
        implements TransactionalUseCase<DeleteUnitUseCase.InputData, EmptyOutput> {

    private final ClientRepository clientRepository;
    private final RepositoryProvider repositoryProvider;
    private final UnitRepository unitRepository;

    public DeleteUnitUseCase(ClientRepository clientRepository, UnitRepository unitRepository,
            RepositoryProvider repositoryProvider) {
        this.clientRepository = clientRepository;
        this.repositoryProvider = repositoryProvider;
        this.unitRepository = unitRepository;
    }

    @Override
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

    void removeObjectsInUnit(Unit unit) {
        // delete scope and risk-affected elements first to prevent FK constraint
        // violations
        // FIXME VEO-1124 remove all relations first, then elements
        var associationOwners = List.of(Scope.class, Process.class, Asset.class, Scenario.class);
        associationOwners.forEach((type) -> {
            log.debug("Step 1: First remove the owning side of bi-directional associations "
                    + "members / risks on {}.", type.getSimpleName());
            repositoryProvider.getElementRepositoryFor(type)
                              .deleteByUnit(unit);
        });

        EntityType.ELEMENT_TYPE_CLASSES.stream()
                                       .filter(not(associationOwners::contains))
                                       .sorted(Comparator.comparing(Class::getSimpleName))
                                       .peek((e) -> log.debug("Step 2:Deleting all unit members "
                                               + "of type {}.", e.getSimpleName()))
                                       .forEach(clazz -> repositoryProvider.getElementRepositoryFor(clazz)
                                                                           .deleteByUnit(unit));
    }

    @Valid
    @Value
    public static class InputData implements UseCase.InputData {
        Key<UUID> unitId;
        Client authenticatedClient;
    }

}
