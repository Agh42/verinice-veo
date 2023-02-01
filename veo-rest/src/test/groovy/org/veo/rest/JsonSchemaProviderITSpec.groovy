/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2022  Jochen Kemnade
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

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithUserDetails

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.JanLoebel.jsonschemavalidation.provider.JsonSchemaProvider

import org.veo.core.VeoSpringSpec
import org.veo.core.entity.definitions.CustomAspectDefinition
import org.veo.core.entity.definitions.attribute.EnumAttributeDefinition

@WithUserDetails("user@domain.example")
class JsonSchemaProviderITSpec extends VeoSpringSpec {

    @Autowired
    def JsonSchemaProvider jsonSchemaProvider

    @Autowired
    def ObjectMapper objectMapper

    def "only active domains are considered for the schema generation"() {
        given:
        def client = createTestClient()
        def domain1 = domainDataRepository.save(newDomain(client) {
            active = false
            applyElementTypeDefinition(newElementTypeDefinition("asset", it) {
                customAspects = [
                    inactiveFoo: new CustomAspectDefinition().tap{
                        attributeDefinitions = [
                            inactiveFooAttr: new EnumAttributeDefinition(["one", "two", "three"])
                        ]
                    }
                ]
            })
        })
        def domain2 = domainDataRepository.save(newDomain(client) {
            applyElementTypeDefinition(newElementTypeDefinition("asset", it) {
                customAspects = [
                    foo: new CustomAspectDefinition().tap{
                        attributeDefinitions = [
                            fooAttr: new EnumAttributeDefinition(["one", "two", "three"])
                        ]
                    }
                ]
            })
        })

        when:
        def schema = jsonSchemaProvider.loadSchema('asset')
        def schemaText = schema.schemaNode.toString()

        then:
        schemaText.contains('foo')
        schemaText.contains('fooAttr')
        !schemaText.contains('inactiveFoo')
        !schemaText.contains('inactiveFooAttr')
    }
}
