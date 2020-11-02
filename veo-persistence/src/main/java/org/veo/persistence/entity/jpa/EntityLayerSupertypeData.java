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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

import org.veo.core.entity.CustomLink;
import org.veo.core.entity.CustomProperties;
import org.veo.core.entity.Domain;
import org.veo.core.entity.EntityLayerSupertype;
import org.veo.core.entity.Unit;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity(name = "entitylayersupertype")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@ToString(onlyExplicitlyIncluded = true, callSuper = true)
@Data
public abstract class EntityLayerSupertypeData extends BaseModelObjectData
        implements NameableData, EntityLayerSupertype {

    @NotNull
    @Column(name = "name")
    @ToString.Include
    private String name;

    @Column(name = "abbreviation")
    private String abbreviation;

    @Column(name = "description")
    private String description;

    @Column(name = "domains")
    @ManyToMany(targetEntity = DomainData.class, fetch = FetchType.LAZY)
    private Set<Domain> domains = new HashSet<>();

    @Column(name = "links")
    @OneToMany(cascade = CascadeType.ALL,
               orphanRemoval = true,
               targetEntity = CustomLinkData.class,
               mappedBy = "source",
               fetch = FetchType.LAZY)
    private Set<CustomLink> links = new HashSet<>();

    @Column(name = "customaspects")
    @OneToMany(cascade = CascadeType.ALL,
               orphanRemoval = true,
               targetEntity = CustomPropertiesData.class,
               mappedBy = "owner",
               fetch = FetchType.LAZY)
    private Set<CustomProperties> customAspects = new HashSet<>();

    // one to one entitylayersupertype-> unit
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = UnitData.class)
    @JoinColumn(name = "owner_id")
    private Unit owner;

    public void setLinks(Set<CustomLink> newLinks) {
        this.links.clear();
        newLinks.forEach(l -> l.setSource(this));
        this.links.addAll(newLinks);
    }

    public void setCustomAspects(Set<CustomProperties> aCustomAspects) {
        this.customAspects.clear();
        aCustomAspects.forEach(aspect -> {
            if (aspect instanceof CustomPropertiesData) {
                ((CustomPropertiesData) aspect).setOwner(this);
            }
        });
        this.customAspects.addAll(aCustomAspects);
    }

    public void setDomains(Set<Domain> newDomains) {
        this.domains.clear();
        this.domains.addAll(newDomains);
    }

    /**
     * Add the given Domain to the collection domains.
     *
     * @return true if added
     */
    public boolean addToDomains(Domain aDomain) {
        return this.domains.add(aDomain);
    }

    /**
     * Remove the given Domain from the collection domains.
     *
     * @return true if removed
     */
    public boolean removeFromDomains(Domain aDomain) {
        return this.domains.remove(aDomain);
    }

    /**
     * Add the given CustomLink to the collection links. opposite of
     * EntityLayerSupertype.source
     *
     * @return true if added
     */
    public boolean addToLinks(CustomLink aCustomLink) {
        aCustomLink.setSource(this);
        return this.links.add(aCustomLink);
    }

    /**
     * Remove the given CustomLink from the collection links. opposite of
     * EntityLayerSupertype.source
     *
     * @return true if removed
     */
    public boolean removeFromLinks(CustomLink aCustomLink) {
        aCustomLink.setSource(null);
        return this.links.remove(aCustomLink);
    }

    /**
     * Add the given CustomProperties to the collection customAspects.
     *
     * @return true if added
     */
    public boolean addToCustomAspects(CustomProperties aCustomProperties) {
        if (aCustomProperties instanceof CustomPropertiesData) {
            ((CustomPropertiesData) aCustomProperties).setOwner(this);
        }
        return this.customAspects.add(aCustomProperties);
    }

    /**
     * Remove the given CustomProperties from the collection customAspects.
     *
     * @return true if removed
     */
    public boolean removeFromCustomAspects(CustomProperties aCustomProperties) {
        if (aCustomProperties instanceof CustomPropertiesData) {
            CustomPropertiesData propertiesData = (CustomPropertiesData) aCustomProperties;
            propertiesData.setOwner(null);
        }
        return this.customAspects.remove(aCustomProperties);
    }

}
