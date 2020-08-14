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
package org.veo.core.usecase.person;

import java.util.UUID;

import javax.validation.Valid;

import org.veo.core.entity.Client;
import org.veo.core.entity.Key;
import org.veo.core.entity.Person;
import org.veo.core.entity.exception.NotFoundException;
import org.veo.core.usecase.UseCase;
import org.veo.core.usecase.repository.PersonRepository;

import lombok.Value;

/**
 * Reinstantiate a persisted person object.
 */
public class GetPersonUseCase<R>
        extends UseCase<GetPersonUseCase.InputData, GetPersonUseCase.OutputData, R> {

    private final PersonRepository repository;

    public GetPersonUseCase(PersonRepository repository) {
        this.repository = repository;
    }

    @Override
    public OutputData execute(InputData input) {
        Person person = repository.findById(input.getId())
                                  .orElseThrow(() -> new NotFoundException(input.getId()
                                                                                .uuidValue()));
        // TODO VEO-124 this check should always be done implicitly by UnitImpl or
        // ModelValidator. Without this check, it would be possible to overwrite
        // objects from other clients with our own clientID, thereby hijacking these
        // objects!
        checkSameClient(input.authenticatedClient, person);
        return new OutputData(person);
    }

    @Valid
    @Value
    public static class InputData implements UseCase.InputData {
        Key<UUID> id;
        Client authenticatedClient;
    }

    @Valid
    @Value
    public static class OutputData implements UseCase.OutputData {
        @Valid
        Person person;
    }
}
