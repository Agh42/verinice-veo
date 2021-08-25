/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2021  Urs Zeidler.
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
package org.veo.persistence.access;

import java.util.List;

import org.springframework.stereotype.Repository;

import org.veo.core.entity.Client;
import org.veo.core.entity.Domain;
import org.veo.core.repository.DomainRepository;
import org.veo.persistence.access.jpa.DomainDataRepository;
import org.veo.persistence.entity.jpa.DomainData;
import org.veo.persistence.entity.jpa.ValidationService;

@Repository
public class DomainRepositoryImpl extends
        AbstractIdentifiableVersionedRepository<Domain, DomainData> implements DomainRepository {

    public DomainRepositoryImpl(DomainDataRepository dataRepository, ValidationService validator) {
        super(dataRepository, validator);
    }

    public List<Domain> findByClient(Client client) {
        // TODO: VEO-498 Implement Domain Search
        return null;
    }

}
