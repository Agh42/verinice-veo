/*******************************************************************************
 * Copyright (c) 2020 Alexander Ben Nasrallah.
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
package org.veo.core.usecase.scenario;

import org.veo.core.entity.Scenario;
import org.veo.core.usecase.base.CreateEntityUseCase;
import org.veo.core.usecase.repository.ScenarioRepository;
import org.veo.core.usecase.repository.UnitRepository;

public class CreateScenarioUseCase extends CreateEntityUseCase<Scenario> {

    public CreateScenarioUseCase(UnitRepository unitRepository, ScenarioRepository entityRepo) {
        super(unitRepository, entityRepo);
    }
}