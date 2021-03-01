/*******************************************************************************
 * Copyright (c) 2020 Alexander Koderman.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.veo.core.entity;

import java.util.Date;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;

import lombok.NonNull;

/**
 * Defines the behaviour of a risk object that is generic enough to be usable in
 * different standards such as ISO 27001:2013, NIST 800-30 and others.
 * <p>
 * <p>
 * The 'scenario' object may either represent a single discrete threat event or
 * a group of discrete threat events that together form a 'threat scenario' (as
 * defined by NIST 800-30).
 * <p>
 * A risk may be mitigated by a control or a set of controls. This is optional.
 * <p>
 * A risk may have an appointed risk owner as defined in ISO/IEC 27000: "a
 * person or entity with the accountability and authority to manage a risk". (In
 * NIST 800-30 terms this would be the information owner - not the risk
 * assessor). This is also optional.
 */
public interface AbstractRisk extends CompoundKeyEntity {

    Set<Domain> getDomains();

    boolean addToDomains(Domain aDomain);

    void setDomains(@NonNull @NotEmpty Set<Domain> newDomains);

    boolean removeFromDomains(Domain aDomain);

    Control getMitigation();

    /**
     * Mitigate this risk by applying the effects of a control.
     *
     * @param control
     *            the mitigating control or {@code null} for no control.
     */
    AbstractRisk mitigate(@Nullable Control control);

    Scenario getScenario();

    Person getRiskOwner();

    /**
     * Appoint a person or entity with the accountability and authority to manage a
     * risk.
     *
     * @param riskOwner
     *            the person who should be the new risk owner for this risk or
     *            {@code null} to appoint no one.
     */
    AbstractRisk appoint(@Nullable Person riskOwner);

    public Date getCreatedOn();

    public Date getLastModified();

    public String getLastModifiedBy();

    public String getCreatedBy();

    public long getVersion();

    /**
     * Remove this risk from its associated entity.
     *
     * @return {@code true} if the risk could be removed. {@code false} otherwise.
     */
    boolean remove();
}