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
package org.veo.persistence.access.jpa;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import org.veo.core.entity.Client;
import org.veo.persistence.entity.jpa.ClientData;

public interface ClientDataRepository extends CrudRepository<ClientData, String> {

    @Query("select c from #{#entityName} c left join fetch c.domains where c.dbId = ?1")
    Optional<ClientData> findById(String id);

    @Query("select c from #{#entityName} c left join fetch c.domains where c.dbId = ?1")
    Optional<Client> findByIdFetchUnits(String id);

}
