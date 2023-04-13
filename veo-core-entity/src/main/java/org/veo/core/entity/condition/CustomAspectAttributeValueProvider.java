/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2022  Jonas Jordan
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
package org.veo.core.entity.condition;

import javax.validation.constraints.NotNull;

import org.veo.core.entity.Domain;
import org.veo.core.entity.Element;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Provides the value for a certain custom aspect attribute on an element. */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class CustomAspectAttributeValueProvider implements InputProvider {
  private @NotNull String customAspect;
  private @NotNull String attribute;

  @Override
  public Object getValue(Element element, Domain domain) {
    return element.getCustomAspects().stream()
        .filter(ca -> ca.getType().equals(customAspect))
        .findFirst()
        .map(ca -> ca.getAttributes().get(attribute))
        .orElse(null);
  }
}
