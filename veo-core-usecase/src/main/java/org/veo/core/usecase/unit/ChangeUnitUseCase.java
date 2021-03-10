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
package org.veo.core.usecase.unit;

import javax.validation.Valid;

import org.veo.core.entity.Client;
import org.veo.core.entity.Unit;
import org.veo.core.entity.exception.NotFoundException;
import org.veo.core.entity.specification.SameClientSpecification;
import org.veo.core.usecase.UseCase;
import org.veo.core.usecase.common.ETag;
import org.veo.core.usecase.common.ETagMismatchException;
import org.veo.core.usecase.repository.UnitRepository;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstract superclass for all operations that change an asset. The
 * <code>update()</code> method must be overwritten to make all necessary
 * changes to the asset.
 */
@Slf4j
public abstract class ChangeUnitUseCase
        implements UseCase<ChangeUnitUseCase.InputData, ChangeUnitUseCase.OutputData> {

    private final UnitRepository unitRepository;

    public ChangeUnitUseCase(UnitRepository repository) {
        this.unitRepository = repository;
    }

    /**
     * Find a persisted unit object and reinstantiate it. Throws a domain exception
     * if the requested unit object was not found in the repository.
     */
    @Override
    public OutputData execute(InputData input) {
        log.info("Updating unit with id {}", input.getChangedUnit()
                                                  .getId()
                                                  .uuidValue());

        var storedUnit = unitRepository.findById(input.getChangedUnit()
                                                      .getId())
                                       .orElseThrow(() -> new NotFoundException(
                                               "Unit %s was not found.", input.getChangedUnit()
                                                                              .getId()
                                                                              .uuidValue()));
        checkSameClient(storedUnit, input);
        checkETag(storedUnit, input);
        var updatedUnit = update(storedUnit, input);
        updatedUnit.version(input.username, storedUnit);
        return output(save(updatedUnit, input));
    }

    protected abstract Unit update(Unit storedUnit, InputData input);

    protected Unit save(Unit unit, InputData input) {
        // Notice: by changing the context here it would be possible to change the view
        // of the entity that is being
        // returned after the save.
        // i.e. to exclude all references and collections:
        // "dataToEntityContext.partialUnit();"
        unit.setClient(input.getAuthenticatedClient());
        return this.unitRepository.save(unit);
    }

    private OutputData output(Unit unit) {
        return new OutputData(unit);
    }

    /**
     * Without this check, it would be possible to overwrite objects from other
     * clients with our own clientID, thereby hijacking these objects!
     *
     * @throws ClientBoundaryViolationException,
     *             if the client in the input and in the stored unit is not the same
     *             as in the authentication object
     */
    private void checkSameClient(Unit storedUnit, InputData input) {
        log.info("Comparing clients {} and {}", input.getAuthenticatedClient()
                                                     .getId()
                                                     .uuidValue(),
                 storedUnit.getClient()
                           .getId()
                           .uuidValue());
        storedUnit.checkSameClient(input.getAuthenticatedClient());
        if (input.getChangedUnit()
                 .getClient() != null) {
            input.getChangedUnit()
                 .checkSameClient(input.getAuthenticatedClient());
        }
    }

    public boolean isSame(Client client1, Client client2) {
        return new SameClientSpecification<>(client1).isSatisfiedBy(client2);
    }

    private void checkETag(Unit storedUnit, InputData input) {
        if (!ETag.matches(storedUnit.getId()
                                    .uuidValue(),
                          storedUnit.getVersion(), input.getETag())) {
            throw new ETagMismatchException(
                    String.format("The eTag does not match for the unit with the ID %s",
                                  storedUnit.getId()
                                            .uuidValue()));
        }
    }

    @Valid
    @Value
    public static class InputData implements UseCase.InputData {
        Unit changedUnit;
        Client authenticatedClient;
        String eTag;
        String username;
    }

    @Valid
    @Value
    public static class OutputData implements UseCase.OutputData {
        @Valid
        Unit unit;

    }
}
