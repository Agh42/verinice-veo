/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2020  Alexander Koderman.
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
package org.veo.adapter.persistence.schema;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.veo.core.entity.Domain;
import org.veo.core.entity.EntityType;
import org.veo.core.entity.exception.NotFoundException;
import org.veo.core.service.EntitySchemaService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Returns a static JSON file of the full entity from classpath. Ignores the
 * 'domain' parameter. Delivers the full JSON. Ignores the language parameter,
 * returning the full translation file.
 */
@Slf4j
@RequiredArgsConstructor
public class EntitySchemaServiceImpl implements EntitySchemaService {

    private static final List<String> VALID_TYPE_SINGULAR_TERMS = EntityType.ELEMENT_TYPES.stream()
                                                                                          .map(EntityType::getSingularTerm)
                                                                                          .collect(Collectors.toList());
    private final EntitySchemaGenerator generateEntitytSchema;

    @Override
    public String findSchema(String type, Set<Domain> domains) {
        if (!VALID_TYPE_SINGULAR_TERMS.contains(type)) {
            throw new IllegalArgumentException(
                    String.format("Type \"%s\" is not a valid schema.", type));
        }
        log.debug("Getting dynamic JSON schema for type: {}", type);
        return generateEntitytSchema.createSchema(type, domains);
    }

    @Override
    public String roleFilter(List<String> roles, String inputSchema) {
        return inputSchema; // TODO VEO-966 filter schema elements by user role
    }

    @Override
    public String findTranslations(Set<String> languages) {
        log.debug("Getting full static translation file, ignoring requested language filter: {}",
                  languages);
        return extract("/lang/lang.json");
    }

    private String extract(final String file) {
        log.debug("Loading file form classpath: {}", file);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(this.getClass()
                                                                              .getResourceAsStream(file),
                StandardCharsets.UTF_8))) {
            return br.lines()
                     .collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new NotFoundException("Stored JSON file has wrong encoding.");
        }
    }
}
