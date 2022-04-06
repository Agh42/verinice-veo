/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2022  Alexander Koderman
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
package org.veo.persistence.entity.jpa;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.veo.core.entity.risk.CategoryRef;
import org.veo.core.entity.risk.ImpactRef;
import org.veo.core.entity.risk.ImplementationStatusRef;
import org.veo.core.entity.risk.ProbabilityRef;
import org.veo.core.entity.risk.RiskDefinitionRef;
import org.veo.core.entity.risk.RiskRef;

public class ReferenceSerializationModule extends SimpleModule {
    transient RiskReferenceFactoryImpl refFactory = RiskReferenceFactoryImpl.getInstance();

    private static final long serialVersionUID = -786416108694458569L;

    public ReferenceSerializationModule() {
        addSerializer(ImpactRef.class, new JsonSerializer<>() {
            @Override
            public void serialize(ImpactRef value, JsonGenerator gen,
                    SerializerProvider serializers) throws IOException {
                gen.writeNumber(value.getIdRef());
            }
        });
        addDeserializer(ImpactRef.class, new JsonDeserializer<>() {
            @Override
            public ImpactRef deserialize(JsonParser p, DeserializationContext ctxt)
                    throws IOException {
                return refFactory.createImpactRef(p.getDecimalValue());
            }
        });
        addKeySerializer(CategoryRef.class, new JsonSerializer<>() {
            @Override
            public void serialize(CategoryRef value, JsonGenerator gen,
                    SerializerProvider serializers) throws IOException {
                gen.writeFieldName(value.getIdRef());
            }
        });
        addKeyDeserializer(CategoryRef.class, new KeyDeserializer() {
            @Override
            public Object deserializeKey(String key, DeserializationContext ctxt) {
                return refFactory.createCategoryRef(key);
            }
        });

        addSerializer(CategoryRef.class, new JsonSerializer<>() {
            @Override
            public void serialize(CategoryRef value, JsonGenerator gen,
                    SerializerProvider serializers) throws IOException {
                gen.writeString(value.getIdRef());
            }
        });
        addDeserializer(CategoryRef.class, new JsonDeserializer<>() {
            @Override
            public CategoryRef deserialize(JsonParser p, DeserializationContext ctxt)
                    throws IOException {
                return refFactory.createCategoryRef(p.getValueAsString());
            }
        });
        addSerializer(ImplementationStatusRef.class, new JsonSerializer<>() {
            @Override
            public void serialize(ImplementationStatusRef value, JsonGenerator gen,
                    SerializerProvider serializers) throws IOException {
                gen.writeNumber(value.getOrdinalValue());
            }
        });
        addDeserializer(ImplementationStatusRef.class, new JsonDeserializer<>() {
            @Override
            public ImplementationStatusRef deserialize(JsonParser p, DeserializationContext ctxt)
                    throws IOException {
                return refFactory.createImplementationStatusRef(p.getIntValue());
            }
        });

        addSerializer(RiskDefinitionRef.class, new JsonSerializer<>() {
            @Override
            public void serialize(RiskDefinitionRef value, JsonGenerator gen,
                    SerializerProvider serializers) throws IOException {
                gen.writeString(value.getIdRef());
            }
        });
        addDeserializer(RiskDefinitionRef.class, new JsonDeserializer<>() {
            @Override
            public RiskDefinitionRef deserialize(JsonParser p, DeserializationContext ctxt)
                    throws IOException {
                return refFactory.createRiskDefinitionRef(p.getValueAsString());
            }
        });
        addKeySerializer(RiskDefinitionRef.class, new JsonSerializer<>() {
            @Override
            public void serialize(RiskDefinitionRef value, JsonGenerator gen,
                    SerializerProvider serializers) throws IOException {
                gen.writeFieldName(value.getIdRef());
            }
        });
        addKeyDeserializer(RiskDefinitionRef.class, new KeyDeserializer() {
            @Override
            public Object deserializeKey(String key, DeserializationContext ctxt) {
                return refFactory.createRiskDefinitionRef(key);
            }
        });
        addSerializer(ProbabilityRef.class, new JsonSerializer<>() {
            @Override
            public void serialize(ProbabilityRef value, JsonGenerator gen,
                    SerializerProvider serializers) throws IOException {
                gen.writeNumber(value.getIdRef());
            }
        });
        addDeserializer(ProbabilityRef.class, new JsonDeserializer<>() {
            @Override
            public ProbabilityRef deserialize(JsonParser p, DeserializationContext ctxt)
                    throws IOException {
                return refFactory.createProbabilityRef(p.getDecimalValue());
            }
        });

        addSerializer(RiskRef.class, new JsonSerializer<>() {
            @Override
            public void serialize(RiskRef value, JsonGenerator gen, SerializerProvider serializers)
                    throws IOException {
                gen.writeNumber(value.getIdRef());
            }
        });

        addDeserializer(RiskRef.class, new JsonDeserializer<>() {
            @Override
            public RiskRef deserialize(JsonParser p, DeserializationContext ctxt)
                    throws IOException {
                return refFactory.createRiskRef(p.getDecimalValue());
            }
        });
    }
}
