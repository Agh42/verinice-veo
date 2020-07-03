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
package org.veo.persistence.access.jpa;

import java.util.List;

import org.veo.persistence.entity.jpa.AssetData;
import org.veo.persistence.entity.jpa.groups.AssetGroupData;

public interface AssetDataRepository extends EntityLayerSupertypeDataRepository<AssetData> {

    List<AssetGroupData> findGroupsByOwner_ClientId(String uuidValue);

    List<AssetGroupData> findGroupsByOwnerId(String uuidValue);

}