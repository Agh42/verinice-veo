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
package org.veo.rest.schemas.controller;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

import org.veo.core.service.EntitySchemaService;
import org.veo.rest.schemas.resource.TranslationsResource;

import lombok.RequiredArgsConstructor;

/**
 * REST service which provides methods to UI translations in JSON format.
 */
@Component
@RequiredArgsConstructor
public class TranslationController implements TranslationsResource {

    private final EntitySchemaService schemaService;

    @Override
    public CompletableFuture<ResponseEntity<String>> getSchema(Authentication auth,
            @RequestParam(value = "languages") Set<String> languages) {
        return CompletableFuture.supplyAsync(() -> {
            String t10n = schemaService.findTranslations(languages);
            return ResponseEntity.ok()
                                 .body(t10n);
        });
    }
}
