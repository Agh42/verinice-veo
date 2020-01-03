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
package org.veo.rest.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.veo.core.usecase.asset.CreateAssetUseCase;
import org.veo.core.usecase.process.CreateProcessUseCase;
import org.veo.core.usecase.process.GetProcessUseCase;
import org.veo.core.usecase.unit.GetUnitUseCase;
import org.veo.persistence.access.AssetRepositoryImpl;
import org.veo.persistence.access.ProcessRepositoryImpl;
import org.veo.persistence.access.UnitRepositoryImpl;

/**
 * This configuration takes care of wiring classes from core modules
 * (Entity-Layer, Use Case-Layer) that have no dependency to the Spring
 * framework. They are therefore not picked up and autowired by Spring.
 *
 */
@Configuration
public class ModuleConfiguration {

    @Bean
    public CreateProcessUseCase createProcessUseCase(ProcessRepositoryImpl processRepository,
            AssetRepositoryImpl assetRepository, GetUnitUseCase getUnitUseCase) {
        return new CreateProcessUseCase(processRepository, assetRepository, getUnitUseCase);
    }

    @Bean
    public GetProcessUseCase getProcessUseCase(ProcessRepositoryImpl processRepository) {
        return new GetProcessUseCase(processRepository);
    }

    @Bean
    public CreateAssetUseCase createAssetUseCase(AssetRepositoryImpl assetRepository) {
        return new CreateAssetUseCase(assetRepository);
    }

    @Bean
    public GetUnitUseCase getUnitUseCase(UnitRepositoryImpl unitRepository) {
        return new GetUnitUseCase(unitRepository);
    }
}