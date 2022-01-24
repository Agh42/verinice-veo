/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2019  Urs Zeidler.
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
package org.veo.persistence.entity.jpa;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.validation.Valid;

import org.veo.core.entity.Control;
import org.veo.core.entity.DomainTemplate;
import org.veo.core.entity.Identifiable;
import org.veo.core.entity.risk.ControlRiskValues;
import org.veo.core.entity.risk.RiskDefinitionRef;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Entity(name = "control")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@ToString(onlyExplicitlyIncluded = true, callSuper = true)
public class ControlData extends ElementData implements Control {

    @Override
    public Class<? extends Identifiable> getModelInterface() {
        return Control.class;
    }

    @ManyToMany(targetEntity = ControlData.class,
                cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "control_parts",
               joinColumns = @JoinColumn(name = "composite_id"),
               inverseJoinColumns = @JoinColumn(name = "part_id"))
    @Valid
    @Getter
    private final Set<Control> parts = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL,
               orphanRemoval = true,
               targetEntity = ControlRiskValuesAspectData.class,
               mappedBy = "owner",
               fetch = FetchType.LAZY)
    @Valid
    private final Set<ControlRiskValuesAspectData> riskValuesAspects = new HashSet<>();

    @Override
    public void setRiskValues(DomainTemplate domain,
            Map<RiskDefinitionRef, ControlRiskValues> riskValues) {
        var aspect = findAspectByDomain(riskValuesAspects, domain).orElseGet(() -> {
            var newAspect = new ControlRiskValuesAspectData(domain, this);
            riskValuesAspects.add(newAspect);
            return newAspect;
        });
        aspect.setValues(riskValues);
    }

    public Optional<Map<RiskDefinitionRef, ControlRiskValues>> getRiskValues(
            DomainTemplate domain) {
        return findAspectByDomain(riskValuesAspects, domain).map(a -> a.getValues());
    }
}
