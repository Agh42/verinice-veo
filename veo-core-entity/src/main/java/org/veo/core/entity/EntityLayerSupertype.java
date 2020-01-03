/*******************************************************************************
 * Copyright (c) 2019 Alexander Koderman.
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

import java.time.Instant;
import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.PositiveOrZero;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import org.veo.core.entity.specification.ClientBoundaryViolationException;
import org.veo.core.entity.specification.InvalidUnitException;
import org.veo.core.entity.specification.SameClientSpecification;
import org.veo.core.entity.specification.ValidUnitSpecification;

/**
 * Implements common fields and methods for objects in the entity layer.
 *
 *
 */
@RequiredArgsConstructor
@Setter
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class EntityLayerSupertype<T extends EntityLayerSupertype<T>> {

    /**
     * Lifecycle state of an entity. When a lifecycle state changes, the version
     * number needs to be increased in most cases.
     *
     * The possible states are defined as follows:
     *
     * <ul>
     * <li>CREATING: a newly created entity that has not yet been persisted into the
     * repository</li>
     * <li>STORED_CURRENT: a persisted entity in its currently valid version that
     * can be changed</li>
     * <li>STORED_ARCHIVED: a persisted entity in an older version that can no
     * longer be changed</li>
     * <li>STORED_DRAFT: a persisted entity based on the current entity that is
     * currently being edited and can be persisted into the repository. A draft must
     * have a higher version number than the object with STORED_CURRENT status.</li>
     * <li>STORED_DELETED: a entity that is marked as deleted and can no longer be
     * edited</li>
     * <li>DELETING: an entity that is in the process of being deleted from the
     * repository.</li>
     * </ul>
     */

    public enum Lifecycle {
        CREATING, STORED_CURRENT, STORED_ARCHIVED, STORED_DRAFT, STORED_DELETED, DELETING
    }

    // @formatter:off
    /**
     * The version number starts a 0 for a new object and is increased whenever the entity is
     * edited by the user and saved.
     *
     * DRAFTs will have their version number increased. Whenever a DRAFT becomes the STORED_CURRENT version,
     * the state of the previous version will be set to STORED_ARCHIVED (and may be moved to a separate
     * database table that contains only archived data).
     *
     * When a draft is discarded, it will simply be deleted and the STORED_CURRENT remains unchanged.
     * Then the object is finally deleted it will simply be marked as such:
     *
     *                                 discard draft
     *                                ┌──<────────────┐
     *   ┌────────┐   save   ┌────────┴─────┐      ┌──┴──┐     save(*)  ┌──────────────┐ del  ┌──────────────┐(**)
     *   │CREATING├─────────>┤STORED_CURRENT├─────>┤DRAFT├─────────────>┤STORED_CURRENT├─────>┤STORED_DELETED│
     *   │   v0   │          │      v1      │ edit │  v2 ├─┐            │     v2       │      │     v2       │
     *   └────────┘          └──────────────┘      └───┬─┘ │            └──────────────┘      └──────────────┘
     *                                                 ▲   │
     *                                                 └───┘
     *                                             overwrite draft
     *                                               (autosave)
     *
     * (*)  When the DRAFT is saved, v1 will have its state set to STORED_ARCHIVED and its "validUntil" timestamp
     *      set to now().
     * (**) A deleted version will keep the version number but have its state set to STORED_DELETED
     *      and the "validUntil" field set to the timestamp of the delete operation. It will represent
     *      the last known state of the object.
     *
     */
    // @formatter:on

    @NotNull
    @EqualsAndHashCode.Include
    private final Key<UUID> id;

    protected @NotNull Unit unit;

    protected @NotNull Lifecycle state;

    @PastOrPresent(message = "The start of the entity's validity must be in the past.")
    @NotNull(message = "The start of the entity's validity must be in the past.")
    protected Instant validFrom;

    @PastOrPresent(message = "The end of the entity's validity must be be set in the past or set to 'null' if it is currently still valid.")
    protected Instant validUntil;

    @PositiveOrZero
    protected long version;

    protected EntityLayerSupertype(Key<UUID> id, Unit unit, Lifecycle state, Instant validFrom,
            Instant validUntil, long version) {
        checkValidUnit(unit);
        this.id = id;
        this.unit = unit;
        this.state = state;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.version = version;
    }

    /**
     * Associate this entity with a new valid unit. If it already is associated with
     * a unit, make sure that the new one belongs to the same client.
     *
     * @param unit
     */
    public void setUnit(Unit unit) {
        checkValidUnit(unit);
        if (this.unit != null) {
            checkSameClient(unit.getClient());
        }
        this.unit = unit;
    }

    private void checkValidUnit(Unit unit) {
        if (unit == null || !(new ValidUnitSpecification<T>()).isSatisfiedBy(unit))
            throw new InvalidUnitException("The supplied unit is not a valid unit object: %s",
                    unit);
    }

    private void checkSameClient(Client client) {
        if (!(new SameClientSpecification<T>(client).isSatisfiedBy((T) this))) {
            throw new ClientBoundaryViolationException("The client boundary would be "
                    + "violated by the attempted operation on element: " + this.toString()
                    + " from client " + client.toString());
        }
    }

    public void checkSameClient(EntityLayerSupertype<?> otherObject) {
        checkSameClient(otherObject.getUnit()
                                   .getClient());
    }

}