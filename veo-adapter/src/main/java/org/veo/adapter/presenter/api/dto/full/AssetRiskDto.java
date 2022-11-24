/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2020  Alexander Koderman.
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
package org.veo.adapter.presenter.api.dto.full;

import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import org.veo.adapter.presenter.api.Patterns;
import org.veo.adapter.presenter.api.common.IdRef;
import org.veo.adapter.presenter.api.common.ReferenceAssembler;
import org.veo.adapter.presenter.api.common.RiskRef;
import org.veo.adapter.presenter.api.dto.AbstractRiskDto;
import org.veo.core.entity.Asset;
import org.veo.core.entity.AssetRisk;
import org.veo.core.entity.Control;
import org.veo.core.entity.Domain;
import org.veo.core.entity.Person;
import org.veo.core.entity.Scenario;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Singular;

@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Data
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class AssetRiskDto extends AbstractRiskDto {

  @Valid private IdRef<Asset> asset;

  @Builder
  public AssetRiskDto(
      @Valid @Singular Set<IdRef<Domain>> domains,
      @Valid @NotNull(message = "A scenario must be present.") IdRef<Scenario> scenario,
      @Valid IdRef<Control> mitigatedBy,
      @Valid IdRef<Person> riskOwner,
      @Pattern(regexp = Patterns.DATETIME) String createdAt,
      String createdBy,
      @Pattern(regexp = Patterns.DATETIME) String updatedAt,
      String updatedBy,
      @Valid IdRef<Asset> asset,
      RiskRef selfRef,
      long version,
      String designator) {
    super(designator, domains, scenario, mitigatedBy, riskOwner);
    this.asset = asset;
    setSelfRef(selfRef);
    setCreatedAt(createdAt);
    setCreatedBy(createdBy);
    setUpdatedAt(updatedAt);
    setUpdatedBy(updatedBy);
    setVersion(version);
  }

  public static AssetRiskDto from(@Valid AssetRisk risk, ReferenceAssembler referenceAssembler) {
    return AssetRiskDto.builder()
        .designator(risk.getDesignator())
        .asset(IdRef.from(risk.getEntity(), referenceAssembler))
        .scenario(IdRef.from(risk.getScenario(), referenceAssembler))
        .riskOwner(IdRef.from(risk.getRiskOwner(), referenceAssembler))
        .mitigatedBy(IdRef.from(risk.getMitigation(), referenceAssembler))
        .createdAt(risk.getCreatedAt().toString())
        .createdBy(risk.getCreatedBy())
        .updatedAt(risk.getUpdatedAt().toString())
        .updatedBy(risk.getUpdatedBy())
        .version(risk.getVersion())
        .domains(
            risk.getDomains().stream()
                .map(o -> IdRef.from(o, referenceAssembler))
                .collect(Collectors.toSet()))
        .selfRef(new RiskRef(referenceAssembler, risk))
        .build();
  }
}
