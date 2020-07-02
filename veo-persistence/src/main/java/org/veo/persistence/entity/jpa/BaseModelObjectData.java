/*******************************************************************************
 * Copyright (c) 2020 Urs Zeidler.
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

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author urszeidler
 */
@MappedSuperclass
@EqualsAndHashCode(of = "id")
@ToString(onlyExplicitlyIncluded = true)
public class BaseModelObjectData {

    @Id
    @ToString.Include
    private String id;

    @ToString.Include
    private long version;

    // @Enumerated(EnumType.STRING)
    // @Column(nullable = false)
    // Lifecycle state;

    @Column(name = "valid_from", nullable = false)
    private Instant validFrom = Instant.now();

    @Column(name = "valid_until", nullable = true)
    private Instant validUntil;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdAsString() {
        if (id == null) {
            return null;
        }
        return id;
    }

    /**
     * @return the version
     */
    public long getVersion() {
        return version;
    }

    /**
     * @param version
     *            the version to set
     */
    public void setVersion(long version) {
        this.version = version;
    }

    /**
     * @return the validFrom
     */
    public Instant getValidFrom() {
        return validFrom;
    }

    /**
     * @param validFrom
     *            the validFrom to set
     */
    public void setValidFrom(Instant validFrom) {
        this.validFrom = validFrom;
    }

    /**
     * @return the validUntil
     */
    public Instant getValidUntil() {
        return validUntil;
    }

    /**
     * @param validUntil
     *            the validUntil to set
     */
    public void setValidUntil(Instant validUntil) {
        this.validUntil = validUntil;
    }
}
