/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2021  Daniel Murygin.
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
package org.veo.rest;

import java.util.Collection;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.github.JanLoebel.jsonschemavalidation.JsonSchemaValidationException;
import com.github.JanLoebel.jsonschemavalidation.provider.JsonSchemaProvider;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import org.veo.core.service.EntitySchemaService;

@ConditionalOnProperty(prefix = "json", name = "schemaProvider", havingValue = "entityType")
@Component
public class EntityTypeSchemaProvider implements JsonSchemaProvider {

    @Autowired
    private EntitySchemaService schemaService;

    private JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);

    @Override
    public JsonSchema loadSchema(String type) {
        final String schema = schemaService.findSchema(type, Collections.emptyList());
        return jsonSchemaFactory.getSchema(schema, getSchemaValidatorsConfig());
    }

    @Override
    public void handleValidationMessages(Collection<ValidationMessage> validationMessages) {
        if (!validationMessages.isEmpty()) {
            throw new JsonSchemaValidationException(validationMessages);
        }
    }

    protected SchemaValidatorsConfig getSchemaValidatorsConfig() {
        final SchemaValidatorsConfig config = new SchemaValidatorsConfig();
        config.setFailFast(false);
        config.setTypeLoose(true);
        config.setHandleNullableField(true);
        return config;
    }

}