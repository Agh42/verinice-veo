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

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.veo.core.entity.EntityLayerSupertype.Lifecycle;
import org.veo.core.entity.asset.Asset;
import org.veo.core.entity.asset.AssetRepository;

/**
 * Associate this asset with a different unit.
 *
 */
public class MoveAssetToUnitUseCase extends UpdateAssetUseCase {

    public MoveAssetToUnitUseCase(AssetRepository assetRepository) {
        super(assetRepository);
    }

    @Override
    @Transactional(TxType.REQUIRED)
    protected Asset update(Asset asset, InputData input) {
        if (!asset.getState()
                  .equals(Lifecycle.STORED_CURRENT)) {
            throw new IllegalStateException("Only the current version of an asset can be moved.");
        }
        asset.moveToUnit(input.getChangedAsset()
                              .getUnit());
        return asset;
    }
}
