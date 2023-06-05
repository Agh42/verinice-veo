/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2021  Jonas Jordan.
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
package org.veo.core.entity.event;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;

import org.veo.core.entity.ClientOwned;
import org.veo.core.entity.Versioned;

import lombok.NonNull;
import lombok.Value;

/**
 * This event should be triggered by the persistence layer when a {@link Versioned} and {@link
 * ClientOwned} element is being persisted, updated or removed.
 */
@Value
public class ClientOwnedEntityVersioningEvent<T extends Versioned & ClientOwned>
    implements VersioningEvent<T> {
  @NonNull @NotNull T entity;
  ModificationType type;
  String author;
  Instant time;
}
