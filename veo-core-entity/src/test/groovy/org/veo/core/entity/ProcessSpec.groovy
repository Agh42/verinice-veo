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
package org.veo.core.entity

import java.time.Instant

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

import org.veo.core.entity.Key
import org.veo.core.entity.asset.Asset
import org.veo.core.entity.process.Process
import spock.lang.Specification

public class ProcessSpec extends Specification {

    Unit unit;

    def setup() {
        this.unit = Unit.newUnitBelongingToClient(Client.newClient("New Client"), "New Unit");
    }

    def "Create a new process object" () {
        given: "a Key object"
        Instant beforeCreation = Instant.now();

        when: "a new process is created"
        Process process = Process.newProcess(unit, "New Process");

        then: "the process was initialized with expected values"
        process.getName().equals("New Process");
        process.getState().equals(EntityLayerSupertype.Lifecycle.CREATING);
        process.getVersion().equals(0L);
        process.getValidFrom()?.isAfter(beforeCreation);
        process.getValidFrom()?.isBefore(Instant.now());
        process.getValidUntil() == null;
    }


    def "Create a new process with assets" () {
        given: "an array of assets"
        Key key = Key.newUuid();

        Set assets = [
            Asset.newAsset(unit, "Asset 1"),
            Asset.newAsset(unit, "Asset 2"),
            Asset.newAsset(unit, "Asset 3"),
        ];


        when: "a new process is created with these assets"
        Process process = Process.newProcess(unit, "New Process");
        process.addAssets(assets);

        then: "the process was created as expected"
        process.getAssets().size() == 3;
        process.getAssets().equals(assets)
    }
}