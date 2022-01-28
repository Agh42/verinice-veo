/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2022  Alexander Koderman
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.veo.core.entity.risk;

import org.veo.core.entity.riskdefinition.RiskDefinition;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * References a complete risk definition. Each {@code Scope} must have 0..1
 * references to a risk definition of a domain known to it.
 *
 * @see org.veo.core.entity.Scope
 */
@Value
@Builder
@Jacksonized
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class RiskDefinitionRef {
    String idRef;

    public static RiskDefinitionRef from(RiskDefinition rd) {
        return rd == null ? null : new RiskDefinitionRef(rd.getId());
    }
}
