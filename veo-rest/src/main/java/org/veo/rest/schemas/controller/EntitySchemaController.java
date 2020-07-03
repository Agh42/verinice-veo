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
package org.veo.rest.schemas.controller;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import org.veo.core.service.EntitySchemaService;
import org.veo.core.service.SchemaIdentifiersDTO;
import org.veo.rest.schemas.resource.EntitySchemaResource;
import org.veo.rest.security.ApplicationUser;

/**
 * REST service which provides methods to query schemas for business entities.
 * The schemas that are delivered will be generated according to: - the entity,
 * i.e. "Process" - the domain, i.e. "GDPR" - the user's granted authorities (to
 * be determined - currently all properties are returned regardless of the user)
 */
@Component
@RequiredArgsConstructor

public class EntitySchemaController implements EntitySchemaResource {

    private final EntitySchemaService schemaService;
    // TODO need the new meta-schema to validate
    // private final SchemaValidator validator;
    // private final LinkSchemaValidator linkValidator;

    @Override
    public ResponseEntity<String> getSchema(Authentication auth, @PathVariable String type,
            @RequestParam(value = "domains", required = true) List<String> domains) {

        List<String> userRoles = Collections.emptyList();
        if (auth.getPrincipal() instanceof ApplicationUser) {
            ApplicationUser user = (ApplicationUser) auth.getPrincipal();
            userRoles = user.getAuthorities()
                            .stream()
                            .map(grant -> grant.getAuthority())
                            .collect(Collectors.toList());
        }
        // TODO define schema-roles for users
        // TODO use valid 'domain' class

        String schema = schemaService.roleFilter(userRoles,
                                                 schemaService.findSchema(type, domains));
        return ResponseEntity.ok()
                             .body(schema);
    }

    @Override
    public SchemaIdentifiersDTO getSchemas(Authentication auth) {
        return schemaService.listValidSchemaNames();
    }
}