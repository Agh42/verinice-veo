/*******************************************************************************
 * Copyright (c) 2020 Jonas Jordan.
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
package org.veo.core.entity

import spock.lang.Specification

class EntityLayerSupertypeSpec extends Specification {
    def "formats complete display name"() {
        given: "an EntityLayerSupertype spy"
        def spy = Spy(EntityLayerSupertype)

        when: "setting abbreviation, name & owner"
        spy.abbreviation >> "FF"
        spy.name >> "Fun foo"
        spy.owner >> Mock(Unit) {
            it.name >> "Super unit"
        }
        then: "all three elements appear in the display name"
        spy.displayName == "FF - Fun foo (Super unit)"
    }

    def "formats display name without abbreviation"() {
        given: "an EntityLayerSupertype spy"
        def spy = Spy(EntityLayerSupertype)

        when: "setting the abbreviation to null"
        spy.abbreviation >> null
        spy.name >> "Fun foo"
        spy.owner >> Mock(Unit) {
            it.name >> "Super unit"
        }
        then: "the abbreviation is omitted"
        spy.displayName == "- Fun foo (Super unit)"
    }
}
