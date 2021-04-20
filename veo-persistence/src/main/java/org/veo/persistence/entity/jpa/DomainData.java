/*******************************************************************************
 * Copyright (c) 2019 Urs Zeidler.
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
package org.veo.persistence.entity.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.veo.core.entity.Client;
import org.veo.core.entity.Domain;
import org.veo.core.entity.DomainTemplate;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity(name = "domain")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Data
public class DomainData extends DomainTemplateData implements NameableData, Domain {

    @Column(name = "active")
    private boolean active = true;
    // one to one
    @ManyToOne(targetEntity = DomainTemplateData.class)
    private DomainTemplate domainTemplate;
    // This enforces the composition association Client-Domain
    @ManyToOne(targetEntity = ClientData.class, optional = false, fetch = FetchType.LAZY)
    @NotNull
    private Client owner;

    @Override
    public boolean isActive() {
        return active;
    }

}
