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

import javax.validation.Valid;

import lombok.Value;

import org.veo.core.entity.Client;
import org.veo.core.entity.Key;
import org.veo.core.entity.Person;
import org.veo.core.entity.Unit;
import org.veo.core.entity.exception.NotFoundException;
import org.veo.core.entity.transform.EntityFactory;
import org.veo.core.usecase.UseCase;
import org.veo.core.usecase.repository.PersonRepository;
import org.veo.core.usecase.repository.UnitRepository;

public class CreatePersonUseCase<R>
        extends UseCase<CreatePersonUseCase.InputData, CreatePersonUseCase.OutputData, R> {

    private final UnitRepository unitRepository;
    private final PersonRepository personRepository;
    private final EntityFactory entityFactory;

    public CreatePersonUseCase(UnitRepository unitRepository, PersonRepository personRepository,
            EntityFactory entityFactory) {
        this.unitRepository = unitRepository;
        this.personRepository = personRepository;
        this.entityFactory = entityFactory;
    }

    @Override
    public OutputData execute(InputData input) {
        Person person = input.newPerson;
        person.setId(Key.newUuid());
        Unit unit = unitRepository.findById(person.getOwner()
                                                  .getId())
                                  .orElseThrow(() -> new NotFoundException("Unit %s not found.",
                                          person.getOwner()
                                                .getId()
                                                .uuidValue()));// remove
        checkSameClient(input.authenticatedClient, unit, unit);
        return new OutputData(personRepository.save(person));
    }

    @Valid
    @Value
    public static class InputData implements UseCase.InputData {
        Person newPerson;
        Client authenticatedClient;
    }

    @Valid
    @Value
    public static class OutputData implements UseCase.OutputData {
        @Valid
        Person person;
    }
}
