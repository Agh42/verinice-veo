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
package org.veo.core.entity.specification;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.veo.core.entity.Client;
import org.veo.core.entity.EntityLayerSupertype;
import org.veo.core.entity.Unit;

/**
 * Clients must be strictly separated. All references from one object to another
 * including composite relationships must be references to objects that belong
 * to the same client.
 *
 * Provides methods to check for equality of client and unit objects and
 * collections in addition to the entity checks from the interface
 * <code>IEntitySecification</code>.
 *
 */
public class SameClientSpecification<T extends EntityLayerSupertype<T>>
        implements EntitySpecification<T> {

    private Client client;

    public SameClientSpecification(Client client) {
        this.client = client;
    }

    @Override
    public boolean isSatisfiedBy(T entity) {
        return entity.getUnit()
                     .getClient()
                     .equals(this.client);
    }

    public boolean isSatisfiedBy(Client otherClient) {
        return this.client.equals(otherClient);
    }

    public boolean isSatisfiedBy(Collection<Unit> otherUnits) {
        return selectSatisfyingUnits(otherUnits).size() != otherUnits.size();
    }

    public Set<Unit> selectSatisfyingUnits(Collection<Unit> collection) {
        return collection.stream()
                         .filter(u -> (isSatisfiedBy(u.getClient())))
                         .collect(Collectors.toSet());
    }

}