/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2020  Jochen Kemnade.
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
package org.veo.rest

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc

import org.veo.core.VeoSpringSpec
import org.veo.core.entity.ModelObjectType
import org.veo.rest.configuration.WebMvcSecurityConfiguration

import groovy.json.JsonSlurper
import groovy.transform.Memoized

@SpringBootTest(
webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
classes = [WebMvcSecurityConfiguration]
)
@AutoConfigureMockMvc
class SwaggerSpec extends VeoSpringSpec {

    @Autowired
    private MockMvc mvc


    def "Swagger documentation is available"() {
        when:
        def response = mvc.perform(get('/swagger-ui.html')).andReturn().response
        def redirectedUrl = response.redirectedUrl
        then:
        redirectedUrl != null
        when:
        response = mvc.perform(get(redirectedUrl)).andReturn().response
        then:
        response.contentAsString.contains('Swagger UI')
        and:
        apiDocsString.contains('verinice.VEO')
    }

    def "response DTO contains links property"() {
        when:
        def assetDtoSchema = parsedApiDocs.components.schemas.FullAssetDto
        then:
        assetDtoSchema.properties.links != null
        assetDtoSchema.properties.links.description == 'The links for the asset.'
    }


    def "displayName is not required for parts when putting composite entities"() {
        when:
        def scenarioDtoSchema = parsedApiDocs.components.schemas.FullScenarioDto
        then:
        scenarioDtoSchema.properties.parts != null
        scenarioDtoSchema.properties.parts.type == 'array'
        scenarioDtoSchema.properties.parts.items != null
        scenarioDtoSchema.properties.parts.items.'$ref' == '#/components/schemas/PartReference'
        when:
        def partReferenceSchema = parsedApiDocs.components.schemas.PartReference
        then:
        !partReferenceSchema.required.contains('displayName')
    }

    def "targetUri is required for parts when putting composite entities"() {
        when:
        def scenarioDtoSchema = parsedApiDocs.components.schemas.FullScenarioDto
        then:
        scenarioDtoSchema.properties.parts != null
        scenarioDtoSchema.properties.parts.type == 'array'
        scenarioDtoSchema.properties.parts.items != null
        scenarioDtoSchema.properties.parts.items.'$ref' == '#/components/schemas/PartReference'
        when:
        def partReferenceSchema = parsedApiDocs.components.schemas.PartReference
        then:
        partReferenceSchema.required.contains('targetUri')
    }

    def "targetUri is required for scope owner"() {
        when:
        def scopeDtoSchema = parsedApiDocs.components.schemas.FullScopeDto
        then:
        scopeDtoSchema.properties.owner.'$ref' == '#/components/schemas/OwnerReference'
        when:
        def ownerReferenceSchema = parsedApiDocs.components.schemas.OwnerReference
        then:
        ownerReferenceSchema.required.contains('targetUri')
    }

    def "catalog item element is a reference"() {
        when:
        def catalogItemDtoSchema = parsedApiDocs.components.schemas.FullCatalogItemDto
        then:
        catalogItemDtoSchema.properties.element.'$ref' == '#/components/schemas/CatalogItemElement'
        when:
        def elementReferenceSchema = parsedApiDocs.components.schemas.CatalogItemElement
        then:
        elementReferenceSchema.required.contains('targetUri')
    }

    def "allowed entity schema types are listed"() {
        given: "existing entity types"
        def schemaTypes = ModelObjectType.ENTITY_TYPES
                .collect{it.singularTerm}
                .sort()

        when: "fetching allowed schemas from OpenAPI parameter doc"
        List<String> allowedTypes = parsedApiDocs.paths["/schemas/{type}"].get.parameters[0].schema.enum
        then: "they also contain all entity types"
        allowedTypes.sort() == schemaTypes
    }

    def "Process DTO contains status property"() {
        when:
        def processDtoSchema = parsedApiDocs.components.schemas.FullProcessDto
        then:
        with(processDtoSchema.properties.status) {
            it != null
            it.description == 'The status for the Process'
            it.enum == [
                'NEW',
                'IN_PROGRESS',
                'FOR_REVIEW',
                'RELEASED',
                'ARCHIVED'
            ]
        }
        processDtoSchema.required.contains('status')
    }

    @Memoized
    String getApiDocsString() {
        mvc.perform(get('/v3/api-docs')).andReturn().response.contentAsString
    }

    def getParsedApiDocs() {
        new JsonSlurper().parseText(apiDocsString)
    }
}
