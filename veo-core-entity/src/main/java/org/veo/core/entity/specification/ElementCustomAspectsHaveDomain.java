/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2022  Jochen Kemnade
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
package org.veo.core.entity.specification;

import java.util.Set;
import java.util.stream.Collectors;

import org.veo.core.entity.DomainBase;
import org.veo.core.entity.Element;

/** Checks that an element's custom aspects's domain is contained in the element's domains. */
public class ElementCustomAspectsHaveDomain implements EntitySpecification<Element> {

  @Override
  public boolean test(Element element) {
    var domains = getDomains(element);
    return element.getCustomAspects().stream().allMatch(ca -> domains.contains(ca.getDomain()))
        && element.getLinks().stream().allMatch(l -> domains.contains(l.getDomain()));
  }

  private static Set<DomainBase> getDomains(Element element) {
    if (element.getContainingCatalogItem() != null) {
      return Set.of(element.getContainingCatalogItem().getCatalog().getDomainTemplate());
    }
    return element.getDomains().stream().map(DomainBase.class::cast).collect(Collectors.toSet());
  }
}
