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

import org.springframework.stereotype.Repository;

import org.veo.core.entity.Person;
import org.veo.core.usecase.repository.PersonRepository;
import org.veo.persistence.access.jpa.PersonDataRepository;
import org.veo.persistence.entity.jpa.ModelObjectValidation;
import org.veo.persistence.entity.jpa.PersonData;

@Repository
public class PersonRepositoryImpl extends AbstractEntityLayerSupertypeRepository<Person, PersonData>
        implements PersonRepository {

    public PersonRepositoryImpl(PersonDataRepository dataRepository,
            ModelObjectValidation validation) {
        super(dataRepository, validation);
    }

}