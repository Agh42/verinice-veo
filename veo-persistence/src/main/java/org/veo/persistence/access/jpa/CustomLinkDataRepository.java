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
package org.veo.persistence.access.jpa;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import org.veo.persistence.entity.jpa.CustomLinkData;

public interface CustomLinkDataRepository extends JpaRepository<CustomLinkData, String> {
    @Query("SELECT l FROM customlink l where l.target.dbId  IN ?1")
    @Transactional(readOnly = true)
    Set<CustomLinkData> findLinksByTargetIds(Set<String> targetIDs);

    @Query("DELETE FROM customlink l where l.target.dbId  IN ?1")
    @Transactional()
    @Modifying
    void deleteLinksByTargetIds(Set<String> targetIDs);
}
