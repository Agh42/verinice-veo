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
package org.veo.core.usecase.asset;

import java.util.UUID;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.veo.core.entity.AssetRisk;
import org.veo.core.entity.Client;
import org.veo.core.entity.Key;
import org.veo.core.usecase.UseCase;
import org.veo.core.usecase.repository.AssetRepository;
import org.veo.core.usecase.repository.ScenarioRepository;

import lombok.Value;

public class GetAssetRiskUseCase
        extends UseCase<GetAssetRiskUseCase.InputData, GetAssetRiskUseCase.OutputData> {

    private final AssetRepository assetRepository;

    private final ScenarioRepository scenarioRepository;

    public GetAssetRiskUseCase(AssetRepository assetRepository,
            ScenarioRepository scenarioRepository) {
        this.assetRepository = assetRepository;
        this.scenarioRepository = scenarioRepository;
    }

    @Transactional
    public OutputData execute(InputData input) {
        var asset = assetRepository.findById(input.assetRef)
                                   .orElseThrow();
        var scenario = scenarioRepository.findById(input.scenarioRef)
                                         .orElseThrow();

        asset.checkSameClient(input.authenticatedClient);
        scenario.checkSameClient(input.authenticatedClient);

        return new OutputData(asset.getRisk(scenario)
                                   .orElseThrow());
    }

    @Valid
    @Value
    public static class InputData implements UseCase.InputData {
        Client authenticatedClient;
        Key<UUID> assetRef;
        Key<UUID> scenarioRef;
    }

    @Valid
    @Value
    public static class OutputData implements UseCase.OutputData {
        @Valid
        AssetRisk assetRisk;
    }
}
