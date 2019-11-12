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
package org.veo.core.entity.asset;

import java.time.Instant;
import java.util.UUID;

import org.veo.core.entity.EntityLayerSupertype;
import org.veo.core.entity.Key;
import org.veo.core.entity.Unit;

public class Asset extends EntityLayerSupertype {

    private String name;

    private Asset(Key id, Unit unit, String name, Lifecycle status, Instant validFrom,
            Instant validUntil, long version) {
        super(id, unit, status, validFrom, validUntil, version);
        this.name = name;
    }

    public static Asset newAsset(Unit unit, String name) {
        return new Asset(Key.newUuid(), unit, name, Lifecycle.CREATING, Instant.now(), null, 0);
    }

    public static Asset existingAsset(Key<UUID> id, Unit unit, String name, Lifecycle state,
            Instant validFrom, Instant validUntil, long version) {
        return new Asset(id, unit, name, state, validFrom, validUntil, version);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
