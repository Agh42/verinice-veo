/*******************************************************************************
 * Copyright (c) 2021 Urs Zeidler.
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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;

import org.veo.core.entity.Catalog;
import org.veo.core.entity.CatalogItem;
import org.veo.core.entity.DomainTemplate;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity(name = "catalog")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Data
public class CatalogData extends BaseModelObjectData implements Catalog, NameableData {
    @Id
    @ToString.Include
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String dbId;
    @NotNull
    @Column(name = "name")
    private String name;
    @Column(name = "abbreviation")
    private String abbreviation;
    @Column(name = "description")
    private String description;
    @ToString.Exclude
    @Column(name = "catalogitems")
    @OneToMany(cascade = CascadeType.ALL,
               orphanRemoval = true,
               targetEntity = CatalogItemData.class,
               mappedBy = "catalog",
               fetch = FetchType.LAZY)
    private Set<CatalogItem> catalogItems = new HashSet<>();
    // one to one
    @ToString.Exclude
    @ManyToOne(targetEntity = DomainTemplateData.class)
    @JoinColumn(name = "domaintemplate_id")
    private DomainTemplate domainTemplate;
}
