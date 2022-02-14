/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2022  Urs Zeidler
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
package org.veo.core.entity.riskdefinition;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.veo.core.entity.Constraints;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Defines the method to determine a risk value from the {@link RiskDefinition}.
 */
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RiskMethod {
    @Size(max = Constraints.DEFAULT_CONSTANT_MAX_LENGTH)
    @NotNull
    @ToString.Include
    @EqualsAndHashCode.Include
    private String impactMethod;
    @Size(max = Constraints.DEFAULT_CONSTANT_MAX_LENGTH)
    private String description;
}