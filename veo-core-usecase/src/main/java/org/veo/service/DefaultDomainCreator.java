/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2022  Jonas Jordan
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
package org.veo.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.veo.core.entity.Client;
import org.veo.core.entity.Key;
import org.veo.core.repository.DomainTemplateRepository;
import org.veo.core.service.DomainTemplateService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Looks up latest default domain templates and incarnates them in clients. The names of the default
 * domain templates are injected.
 */
@RequiredArgsConstructor
@Slf4j
public class DefaultDomainCreator {
  private final Set<String> defaultDomainTemplateNames;
  private final DomainTemplateService domainService;
  private final DomainTemplateRepository domainTemplateRepository;

  public void addDefaultDomains(Client client) {
    if (!client.getDomains().isEmpty()) {
      throw new IllegalArgumentException("The client already owns domains.");
    }
    Set<String> templateIdsWithClasspathFiles = domainService.getTemplateIdsWithClasspathFiles();
    defaultDomainTemplateNames.forEach(
        name -> {
          List<Key<UUID>> templateIds = domainTemplateRepository.getDomainTemplateIds(name);
          if (templateIds.isEmpty()) {
            log.warn("Default domain template {} not found.", name);
          } else {
            templateIds.stream()
                .map(Key::uuidValue)
                .filter(templateIdsWithClasspathFiles::contains)
                .findFirst()
                .ifPresentOrElse(
                    templateId -> {
                      log.debug(
                          "Adding default domain {} ({}) to client {}",
                          templateId,
                          name,
                          client.getIdAsString());
                      client.addToDomains(domainService.createDomain(client, templateId));
                    },
                    () ->
                        log.warn("Could not create domain {}, no demo unit elements found.", name));
          }
        });
  }
}
