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

import org.veo.core.entity.Control;
import org.veo.core.usecase.repository.ControlRepository;
import org.veo.persistence.access.jpa.ControlDataRepository;
import org.veo.persistence.entity.jpa.ControlData;
import org.veo.persistence.entity.jpa.ModelObjectValidation;
import org.veo.persistence.entity.jpa.groups.ControlGroupData;

@Repository
public class ControlRepositoryImpl extends BaseRepository<Control, ControlData>
        implements ControlRepository {

    public ControlRepositoryImpl(ControlDataRepository dataRepository,
            ModelObjectValidation validation) {
        super(dataRepository, validation, ControlData::from, ControlData::toControl,
                groupData -> ((ControlGroupData) groupData).toControlGroup());
    }
}