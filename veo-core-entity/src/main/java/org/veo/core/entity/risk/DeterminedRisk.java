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

import java.beans.Transient;
import java.util.Optional;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import org.veo.core.entity.Constraints;

/**
 * A risk value as determined by a risk service.
 * <p>
 * <p>
 * The risk value will be determined based on a method defined in the risk
 * definition (i.e. risk matrix, high-water-mark, sum, product, ...)
 *
 * @see org.veo.core.entity.riskdefinition.RiskDefinition
 */
@Valid
public interface DeterminedRisk {

    int EXPLANATION_MAX_LENGTH = Constraints.DEFAULT_DESCRIPTION_MAX_LENGTH;

    void setRiskTreatmentExplanation(
            @Size(max = EXPLANATION_MAX_LENGTH) String riskTreatmentExplanation);

    /**
     * A risk value that is determined by the risk service according to the method
     * defined in the risk definition.
     *
     * @return inherent risk or null
     * @see Probability
     * @see Impact
     */
    RiskRef getInherentRisk();

    /**
     * The residual risk (aka net risk) entered manually by the user as result of
     * taking control effects into account.
     */
    RiskRef getResidualRisk();

    /**
     * The inherent risk becomes the effective risk - unless it is overruled by the
     * user-defined residual risk.
     *
     * @return effective risk or null (if there is no residual risk and no inherent
     *         risk)
     */
    @Transient
    default RiskRef getEffectiveRisk() {
        return Optional.ofNullable(getResidualRisk())
                       .orElse(getInherentRisk());
    }

    void setResidualRiskExplanation(
            @Size(max = EXPLANATION_MAX_LENGTH) String residualRiskExplanation);

    String getResidualRiskExplanation();

    Set<RiskTreatmentOption> getRiskTreatments();

    String getRiskTreatmentExplanation();

    /**
     * The risk after existing controls have been taken into account.
     */
    void setResidualRisk(RiskRef residualRisk);

    void setRiskTreatments(Set<RiskTreatmentOption> riskTreatments);

    CategoryRef getCategory();
}
