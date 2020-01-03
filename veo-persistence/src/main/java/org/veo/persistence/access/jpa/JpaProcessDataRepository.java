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
package org.veo.persistence.access.jpa;

import java.util.Collection;
import java.util.Set;

import org.springframework.data.repository.CrudRepository;

import org.veo.core.entity.asset.Asset;
import org.veo.core.entity.process.Process;
import org.veo.persistence.entity.jpa.ProcessData;
import org.veo.persistence.entity.jpa.SimpleKey;

/**
 * A CRUD repository that will be implemented at runtime by magic and/or Spring
 * Boot.
 *
 */
public interface JpaProcessDataRepository extends CrudRepository<ProcessData, SimpleKey> {

    Collection<ProcessData> findByNameContainingIgnoreCase(String search);

    Set<Process> findDistinctByAssetsIn(Set<Asset> assets);

}