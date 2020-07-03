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
package org.veo.adapter.presenter.api.common;

/**
 * This is a utility class to return instances of different switch obects.
 * Switches can be used to do polymorphic transforms of objects.
 */
public final class Switches {

    private static ToDisplayNameSwitch toDisplayNameSwitch;
    private static ToUUIDReferenceSwitch toUUIDReferenceSwitch;

    public static ToDisplayNameSwitch toDisplayNameSwitch() {
        if (toDisplayNameSwitch == null) {
            toDisplayNameSwitch = new ToDisplayNameSwitch();
        }
        return toDisplayNameSwitch;
    }

    public static ToUUIDReferenceSwitch toUUIDReferenceSwitch() {
        if (toUUIDReferenceSwitch == null) {
            toUUIDReferenceSwitch = new ToUUIDReferenceSwitch();
        }
        return toUUIDReferenceSwitch;
    }
}