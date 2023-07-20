/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2021  Urs Zeidler.
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
package org.veo.adapter.presenter.api.dto.composite;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;

import org.veo.adapter.presenter.api.dto.AbstractCatalogItemDto;
import org.veo.adapter.presenter.api.dto.AbstractElementDto;
import org.veo.adapter.presenter.api.dto.CustomAspectMapDto;
import org.veo.adapter.presenter.api.dto.full.FullAssetDto;
import org.veo.adapter.presenter.api.dto.full.FullControlDto;
import org.veo.adapter.presenter.api.dto.full.FullDocumentDto;
import org.veo.adapter.presenter.api.dto.full.FullIncidentDto;
import org.veo.adapter.presenter.api.dto.full.FullPersonDto;
import org.veo.adapter.presenter.api.dto.full.FullProcessDto;
import org.veo.adapter.presenter.api.dto.full.FullScenarioDto;
import org.veo.adapter.presenter.api.dto.full.FullScopeDto;
import org.veo.core.entity.Asset;
import org.veo.core.entity.Control;
import org.veo.core.entity.Document;
import org.veo.core.entity.Incident;
import org.veo.core.entity.Person;
import org.veo.core.entity.Process;
import org.veo.core.entity.Scenario;
import org.veo.core.entity.Scope;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CompositeCatalogItemDto extends AbstractCatalogItemDto {

  @NotNull
  @Schema(description = "Entity type identifier", example = "person")
  private String elementType;

  @NotNull
  @Schema(description = "The subtype for the Element.", example = "PER")
  private String subType;

  @NotNull
  @Schema(description = "The status for the Element.", example = "NEW")
  private String status;

  @Valid
  @Schema(
      description = "Groups of customizable attributes - see '/schemas'",
      title = "CustomAspect")
  private CustomAspectMapDto customAspects = new CustomAspectMapDto();

  @Deprecated
  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.EXISTING_PROPERTY, property = "type")
  @JsonSubTypes({
    @JsonSubTypes.Type(value = FullAssetDto.class, name = Asset.SINGULAR_TERM),
    @JsonSubTypes.Type(value = FullControlDto.class, name = Control.SINGULAR_TERM),
    @JsonSubTypes.Type(value = FullDocumentDto.class, name = Document.SINGULAR_TERM),
    @JsonSubTypes.Type(value = FullIncidentDto.class, name = Incident.SINGULAR_TERM),
    @JsonSubTypes.Type(value = FullPersonDto.class, name = Person.SINGULAR_TERM),
    @JsonSubTypes.Type(value = FullProcessDto.class, name = Process.SINGULAR_TERM),
    @JsonSubTypes.Type(value = FullScenarioDto.class, name = Scenario.SINGULAR_TERM),
    @JsonSubTypes.Type(value = FullScopeDto.class, name = Scope.SINGULAR_TERM)
  })
  @Schema(description = "Deprectated declaration of the catalog item content", title = "element")
  private AbstractElementDto element;
}
