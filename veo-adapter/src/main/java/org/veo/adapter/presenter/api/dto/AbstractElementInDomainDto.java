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
package org.veo.adapter.presenter.api.dto;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;
import static org.veo.core.entity.aspects.SubTypeAspect.STATUS_MAX_LENGTH;
import static org.veo.core.entity.aspects.SubTypeAspect.SUB_TYPE_MAX_LENGTH;

import java.util.Optional;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.veo.adapter.presenter.api.common.ElementInDomainIdRef;
import org.veo.adapter.presenter.api.common.IdRef;
import org.veo.adapter.presenter.api.openapi.IdRefOwner;
import org.veo.core.entity.Element;
import org.veo.core.entity.ElementOwner;
import org.veo.core.entity.EntityType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * An element from the viewpoint of a domain. Contains both basic and domain-specific properties.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(onlyExplicitlyIncluded = true, callSuper = true)
@SuppressWarnings("PMD.AbstractClassWithoutAnyMethod")
public abstract class AbstractElementInDomainDto<TElement extends Element>
    extends AbstractVersionedDto implements NameableDto {
  @JsonIgnore
  @Getter(AccessLevel.NONE)
  private ElementInDomainIdRef<TElement> selfRef;

  @JsonProperty(value = "_self", access = READ_ONLY)
  @Schema(description = "Absolute URL for this element in this domain", format = "uri")
  public String getSelf() {
    return Optional.ofNullable(selfRef)
        .map(ElementInDomainIdRef::getTargetInDomainUri)
        .orElse(null);
  }

  @Size(min = 1, max = NAME_MAX_LENGTH)
  private String name;

  @Size(min = 1, max = ABBREVIATION_MAX_LENGTH)
  private String abbreviation;

  @Size(min = 1, max = DESCRIPTION_MAX_LENGTH)
  private String description;

  @JsonProperty(access = READ_ONLY)
  private String designator;

  @Schema(description = "Unit that this element belongs to", implementation = IdRefOwner.class)
  @NotNull(message = "An owner must be present.")
  private IdRef<ElementOwner> owner;

  @Schema(
      description =
          "Domain-specific sub type - available sub types are listed in the domain's element type definition. The sub type cannot be changed once the element has been associated with the domain.")
  @NotNull(message = "A sub type must be present")
  @Size(min = 1, max = SUB_TYPE_MAX_LENGTH)
  private String subType;

  @Schema(
      description =
          "Domain-specific status - available statuses depend on the sub type and are specified in the domain's element type definition.")
  @NotNull(message = "A status must be present")
  @Size(min = 1, max = STATUS_MAX_LENGTH)
  private String status;

  @Schema(
      description =
          "Domain-specific relations to other elements that contain a target element and a set of domain-specific attributes - available link types are specified in the domain's element type definition. For each link type, multiple target elements can be linked. The map uses link types as keys and lists of links as values.")
  @Valid
  private LinkMapDto links = new LinkMapDto();

  @Schema(
      description =
          "Domain-specific sets of attributes - available custom aspect types are specified in the domain's element type definition. The map uses custom aspect types as keys and custom aspects as values.",
      example =
          "{\"address\": {\"street\":\"Bahnhofsallee 1b\", \"postalCode\": \"37081\", \"city\": \"Göttingen\"}}")
  @Valid
  private CustomAspectMapDto customAspects = new CustomAspectMapDto();

  @Schema(description = "Element type identifier", accessMode = Schema.AccessMode.READ_ONLY)
  public String getType() {
    return EntityType.getSingularTermByType(getModelInterface());
  }
}
