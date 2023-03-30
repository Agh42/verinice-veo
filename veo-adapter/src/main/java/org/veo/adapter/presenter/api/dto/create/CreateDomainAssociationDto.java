/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2023  Jonas Jordan
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
package org.veo.adapter.presenter.api.dto.create;

import static org.veo.core.entity.aspects.SubTypeAspect.STATUS_DESCRIPTION;
import static org.veo.core.entity.aspects.SubTypeAspect.STATUS_MAX_LENGTH;
import static org.veo.core.entity.aspects.SubTypeAspect.STATUS_NOT_NULL_MESSAGE;
import static org.veo.core.entity.aspects.SubTypeAspect.SUB_TYPE_DESCRIPTION;
import static org.veo.core.entity.aspects.SubTypeAspect.SUB_TYPE_MAX_LENGTH;
import static org.veo.core.entity.aspects.SubTypeAspect.SUB_TYPE_NOT_NULL_MESSAGE;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CreateDomainAssociationDto {

  @Schema(description = SUB_TYPE_DESCRIPTION)
  @NotNull(message = SUB_TYPE_NOT_NULL_MESSAGE)
  @Size(min = 1, max = SUB_TYPE_MAX_LENGTH)
  private String subType;

  @Schema(description = STATUS_DESCRIPTION)
  @NotNull(message = STATUS_NOT_NULL_MESSAGE)
  @Size(min = 1, max = STATUS_MAX_LENGTH)
  private String status;
}
