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

import javax.validation.Valid;

import lombok.Value;

import org.veo.core.entity.Asset;
import org.veo.core.entity.Client;
import org.veo.core.entity.Key;
import org.veo.core.entity.Unit;
import org.veo.core.entity.exception.NotFoundException;
import org.veo.core.entity.transform.EntityFactory;
import org.veo.core.usecase.UseCase;
import org.veo.core.usecase.repository.AssetRepository;
import org.veo.core.usecase.repository.UnitRepository;

public class CreateAssetUseCase<R>
        extends UseCase<CreateAssetUseCase.InputData, CreateAssetUseCase.OutputData, R> {

    private final UnitRepository unitRepository;
    private final AssetRepository assetRepository;
    private final EntityFactory entityFactory;

    public CreateAssetUseCase(UnitRepository unitRepository, AssetRepository assetRepository,
            EntityFactory entityFactory) {
        this.unitRepository = unitRepository;
        this.assetRepository = assetRepository;
        this.entityFactory = entityFactory;
    }

    @Override
    public OutputData execute(InputData input) {
        Asset asset = input.getNewAsset();
        asset.setId(Key.newUuid());
        Unit unit = unitRepository.findById(asset.getOwner()
                                                 .getId())
                                  .orElseThrow(() -> new NotFoundException("Unit %s not found.",
                                          asset.getOwner()
                                               .getId()
                                               .uuidValue()));
        checkSameClient(input.authenticatedClient, unit, unit);

        return new OutputData(assetRepository.save(asset));
    }

    @Valid
    @Value
    public static class InputData implements UseCase.InputData {
        Asset newAsset;
        Client authenticatedClient;
    }

    @Valid
    @Value
    public static class OutputData implements UseCase.OutputData {
        @Valid
        Asset asset;
    }
}
