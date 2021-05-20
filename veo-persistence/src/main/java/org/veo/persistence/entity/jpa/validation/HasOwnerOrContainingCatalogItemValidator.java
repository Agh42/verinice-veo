/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2021  Jochen Kemnade
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
package org.veo.persistence.entity.jpa.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.veo.persistence.entity.jpa.CatalogableData;

public class HasOwnerOrContainingCatalogItemValidator
        implements ConstraintValidator<HasOwnerOrContainingCatalogItem, CatalogableData> {

    @Override
    public boolean isValid(CatalogableData catalogable, ConstraintValidatorContext context) {
        // that's an XOR
        return catalogable.getOwner() != null ^ catalogable.getContainingCatalogItem() != null;
    }
}