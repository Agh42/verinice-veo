/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2019  Urs Zeidler.
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
package org.veo.core.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import org.veo.core.entity.Client;
import org.veo.core.entity.Key;
import org.veo.core.entity.exception.NotFoundException;

/**
 * A repository for <code>Client</code> entities.
 *
 * <p>Implements basic CRUD operations from the superinterface and extends them with more specific
 * methods - i.e. queries based on particular fields.
 */
public interface ClientRepository extends IdentifiableVersionedRepository<Client> {
  static Predicate<Client> IS_CLIENT_ACTIVE = c -> c.getState() == Client.ClientState.ACTIVATED;

  Optional<Client> findByIdFetchCatalogs(Key<UUID> id);

  Optional<Client> findByIdFetchCatalogsAndItems(Key<UUID> id);

  Optional<Client> findByIdFetchCatalogsAndItemsAndTailoringReferences(Key<UUID> id);

  Optional<Client> findByIdFetchTranslations(Key<UUID> id);

  List<Client> findAll();

  default Client getById(Key<UUID> clientId) {
    return findById(clientId).orElseThrow(() -> new NotFoundException(clientId, Client.class));
  }

  default Client getActiveById(Key<UUID> clientId) {
    Client client =
        findById(clientId).orElseThrow(() -> new NotFoundException(clientId, Client.class));
    if (!IS_CLIENT_ACTIVE.test(client)) {
      throw new IllegalStateException("Client not active. " + client.getState());
    }
    return client;
  }

  default Optional<Client> findActiveById(Key<UUID> clientId) {
    Optional<Client> oClient = findById(clientId);
    if (oClient.isPresent()) {
      if (IS_CLIENT_ACTIVE.test(oClient.get())) {
        return oClient;
      }
    }
    return Optional.empty();
  }
}
