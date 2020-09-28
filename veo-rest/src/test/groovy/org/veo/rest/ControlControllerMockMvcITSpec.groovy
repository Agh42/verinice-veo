/*******************************************************************************
 * Copyright (c) 2020 Jochen Kemnade.
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
package org.veo.rest

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

import org.apache.commons.codec.digest.DigestUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.transaction.support.TransactionTemplate

import org.veo.core.VeoMvcSpec
import org.veo.core.entity.Control
import org.veo.core.entity.CustomProperties
import org.veo.core.entity.Domain
import org.veo.core.entity.Key
import org.veo.core.entity.Unit
import org.veo.core.usecase.common.ETag
import org.veo.persistence.access.ClientRepositoryImpl
import org.veo.persistence.access.ControlRepositoryImpl
import org.veo.persistence.access.UnitRepositoryImpl
import org.veo.persistence.entity.jpa.CustomPropertiesData
import org.veo.persistence.entity.jpa.transformer.EntityDataFactory
import org.veo.rest.configuration.WebMvcSecurityConfiguration

import groovy.json.JsonSlurper

/**
 * Integration test for the unit controller. Uses mocked spring MVC environment.
 * Uses JPA repositories with in-memory database.
 * Does not start an embedded server.
 * Uses a test Web-MVC configuration with example accounts and clients.
 */
@SpringBootTest(
webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
classes = [WebMvcSecurityConfiguration]
)
@EnableAsync
@ComponentScan("org.veo.rest")
class ControlControllerMockMvcITSpec extends VeoMvcSpec {

    @Autowired
    private ClientRepositoryImpl clientRepository
    @Autowired
    private UnitRepositoryImpl unitRepository

    @Autowired
    private ControlRepositoryImpl controlRepository

    @Autowired
    TransactionTemplate txTemplate
    @Autowired
    private EntityDataFactory entityFactory

    private Unit unit
    private Domain domain
    private Domain domain1
    private Key clientId = Key.uuidFrom(WebMvcSecurityConfiguration.TESTCLIENT_UUID)
    String salt = "salt-for-etag"

    def setup() {
        txTemplate.execute {
            domain = newDomain {
                description = "ISO/IEC"
                abbreviation = "ISO"
                name = "ISO"
            }

            domain1 = newDomain {
                description = "ISO/IEC2"
                abbreviation = "ISO"
                name = "ISO"
            }

            def client= newClient {
                id = clientId
                domains = [domain, domain1] as Set
            }

            unit = newUnit(client) {
                name = "Test unit"
            }

            unit.client = client
            clientRepository.save(client)
            unitRepository.save(unit)
        }
        ETag.setSalt(salt)
    }


    @WithUserDetails("user@domain.example")
    def "create a control"() {
        given: "a request body"

        Map request = [
            name: 'New Control',
            owner: [
                displayName: 'controlDataProtectionObjectivesEugdprEncryption',
                targetUri: '/units/' + unit.id.uuidValue()
            ]
        ]

        when: "a request is made to the server"

        def results = post('/controls', request)

        then: "the control is created and a status code returned"
        results.andExpect(status().isCreated())

        and: "the location of the new control is returned"
        def result = new JsonSlurper().parseText(results.andReturn().response.contentAsString)
        result.success == true
        def resourceId = result.resourceId
        resourceId != null
        resourceId != ''
        result.message == 'Control created successfully.'
    }

    @WithUserDetails("user@domain.example")
    def "create a control with custom properties"() {
        given: "a request body"

        Map request = [
            name: 'New Control',
            owner: [
                displayName: 'controlDataProtectionObjectivesEugdprEncryption',
                targetUri: '/units/' + unit.id.uuidValue()
            ], customAspects:
            [
                'ControlDataProtectionObjectivesEugdpr' :
                [
                    applicableTo: [
                        "Control"
                    ],
                    domains: [],
                    attributes:  [
                        controlDataProtectionObjectivesEugdprPseudonymization:true,
                        controlDataProtectionObjectivesEugdprEncryption:false
                    ]
                ]
            ]
        ]

        when: "a request is made to the server"

        def results = post('/controls', request)

        then: "the control is created and a status code returned"
        results.andExpect(status().isCreated())

        and: "the location of the new control is returned"
        def result = new JsonSlurper().parseText(results.andReturn().response.contentAsString)
        result.success == true
        def resourceId = result.resourceId
        resourceId != null
        resourceId != ''
        result.message == 'Control created successfully.'

        when:
        Control savedControl = txTemplate.execute {
            controlRepository.findById(Key.uuidFrom(resourceId)).get().tap() {
                // resolve proxy:
                customAspects.first()
            }
        }

        then: 'the custom properties are saved'
        savedControl.customAspects.first().type == 'ControlDataProtectionObjectivesEugdpr'
    }

    @WithUserDetails("user@domain.example")
    def "retrieve a control"() {
        given: "a saved control"
        def control = txTemplate.execute {
            controlRepository.save(newControl(unit) {
                name = 'Test control-1'
            })
        }


        when: "a request is made to the server"
        def results = get("/controls/${control.id.uuidValue()}")
        String expectedETag = DigestUtils.sha256Hex(control.id.uuidValue() + "_" + salt + "_" + Long.toString(control.getVersion()))

        then: "the control is found"
        results.andExpect(status().isOk())
        and: "the eTag is set"
        String eTag = results.andReturn().response.getHeader("ETag")
        eTag != null
        getTextBetweenQuotes(eTag).equals(expectedETag)
        and:
        def result = new JsonSlurper().parseText(results.andReturn().response.contentAsString)
        result.name == 'Test control-1'
        result.owner.targetUri == "http://localhost/units/"+unit.id.uuidValue()
    }

    @WithUserDetails("user@domain.example")
    def "retrieve all controls for a unit"() {
        given: "a saved asset"
        def control = newControl(unit) {
            name = 'Test control-1'
        }
        def control2 = newControl(unit) {
            name = 'Test control-2'
        }
        (control, control2) = txTemplate.execute {
            [control, control2].collect(controlRepository.&save)
        }

        when: "a request is made to the server"
        def results = get("/controls?unit=${unit.id.uuidValue()}")

        then: "the controls are returned"
        results.andExpect(status().isOk())
        when:
        def result = new JsonSlurper().parseText(results.andReturn().response.contentAsString)
        then:
        result.size == 2

        result.sort{it.name}.first().name == 'Test control-1'
        result.sort{it.name}.first().owner.targetUri == "http://localhost/units/"+unit.id.uuidValue()
        result.sort{it.name}[1].name == 'Test control-2'
        result.sort{it.name}[1].owner.targetUri == "http://localhost/units/"+unit.id.uuidValue()
    }

    @WithUserDetails("user@domain.example")
    def "retrieveíng all controls for a unit does not return groups"() {
        given: "a saved control and a saved control group"

        def control = newControl(unit) {
            name = 'Test control-1'
        }

        def controlGroup = newControlGroup(unit) {
            name = 'Group 1'
        }

        txTemplate.execute {
            [control, controlGroup].collect(controlRepository.&save)
        }

        when: "a request is made to the server"
        def results = get("/controls?unit=${unit.id.uuidValue()}")

        then: "the controls are returned"
        results.andExpect(status().isOk())
        when:
        def result = new JsonSlurper().parseText(results.andReturn().response.contentAsString)
        then:
        result.size == 1
        result.first().name == 'Test control-1'
    }

    @WithUserDetails("user@domain.example")
    def "put a control"() {
        given: "a saved control"
        def control = txTemplate.execute {
            controlRepository.save(newControl(unit) {
                domains = [domain1] as Set
            })
        }

        Map request = [
            name: 'New control-2',
            abbreviation: 'u-2',
            description: 'desc',
            owner:
            [
                targetUri: '/units/'+unit.id.uuidValue(),
                displayName: 'test unit'
            ],  domains: [
                [
                    targetUri: '/domains/'+domain.id.uuidValue(),
                    displayName: 'test ddd'
                ]
            ]
        ]


        when: "a request is made to the server"
        Map headers = [
            'If-Match': ETag.from(control.id.uuidValue(), 1)
        ]
        def results = put("/controls/${control.id.uuidValue()}", request, headers)

        then: "the control is found"
        results.andExpect(status().isOk())
        def result = new JsonSlurper().parseText(results.andReturn().response.contentAsString)
        result.name == 'New control-2'
        result.abbreviation == 'u-2'
        result.domains.first().displayName == domain.abbreviation+" "+domain.name
        result.owner.targetUri == "http://localhost/units/"+unit.id.uuidValue()
    }

    @WithUserDetails("user@domain.example")
    def "put a control with custom properties"() {
        given: "a saved control"

        CustomProperties cp = entityFactory.createCustomProperties()
        cp.setType("my.new.type")
        cp.setApplicableTo(['Control'] as Set)

        def control = txTemplate.execute {
            controlRepository.save(newControl(unit) {
                customAspects = [cp] as Set
                domains = [domain1] as Set
            })
        }
        Map request = [
            name: 'New control-2',
            abbreviation: 'u-2',
            description: 'desc',
            owner:
            [
                targetUri: '/units/'+unit.id.uuidValue(),
                displayName: 'test unit'
            ], domains: [
                [
                    targetUri: '/domains/'+domain.id.uuidValue(),
                    displayName: 'test ddd'
                ]
            ], customAspects:
            [
                'ControlDataProtectionObjectivesEugdpr' :
                [
                    applicableTo: [
                        "Control"
                    ],
                    domains: [],
                    attributes:  [
                        controlDataProtectionObjectivesEugdprPseudonymization:true,
                        controlDataProtectionObjectivesEugdprEncryption:false
                    ]
                ]
            ]
        ]

        when: "a request is made to the server"
        Map headers = [
            'If-Match': ETag.from(control.id.uuidValue(), 1)
        ]
        def results = put("/controls/${control.id.uuidValue()}", request, headers)

        then: "the control is found"
        results.andExpect(status().isOk())
        def result = new JsonSlurper().parseText(results.andReturn().response.contentAsString)
        result.name == 'New control-2'
        result.abbreviation == 'u-2'
        result.domains.first().displayName == domain.abbreviation+" "+domain.name
        result.owner.targetUri == "http://localhost/units/"+unit.id.uuidValue()
    }

    @WithUserDetails("user@domain.example")
    def "put a control with a string property that is too long"() {
        given: "a saved control"

        CustomProperties cp = new CustomPropertiesData()
        cp.setType("my.new.type")
        cp.setApplicableTo(['Control'] as Set)

        def control = txTemplate.execute {
            controlRepository.save(newControl(unit) {
                customAspects = [cp] as Set
                domains = [domain1] as Set
            })
        }
        Map request = [
            name: 'New control-2',
            abbreviation: 'u-2',
            description: 'desc',
            owner:
            [
                targetUri: '/units/'+unit.id.uuidValue(),
                displayName: 'test unit'
            ], domains: [
                [
                    targetUri: '/domains/'+domain.id.uuidValue(),
                    displayName: 'test ddd'
                ]
            ], customAspects:
            [
                'ControlDataProtectionObjectivesEugdpr' :
                [
                    applicableTo: [
                        "Control"
                    ],
                    domains: [],
                    attributes:  [
                        test: 'X' * 20000
                    ]
                ]
            ]
        ]

        when: "a request is made to the server"
        Map headers = [
            'If-Match': ETag.from(control.id.uuidValue(), 1)
        ]
        def results = put("/controls/${control.id.uuidValue()}", request, headers, false)

        then: "the data is rejected"
        HttpMessageNotReadableException ex = thrown()

        and: "the reason is given"
        ex.message =~ /Property value for test exceeds maximum length of 18.000 characters./
    }

    @WithUserDetails("user@domain.example")
    def "delete a control"() {

        given: "an existing control"
        def control = txTemplate.execute {
            controlRepository.save(newControl(unit))
        }


        when: "a delete request is sent to the server"

        def results = delete("/controls/${control.id.uuidValue()}")

        then: "the control is deleted"
        results.andExpect(status().isOk())
        controlRepository.findById(control.id).empty
    }

    @WithUserDetails("user@domain.example")
    def "can't put a control with another control's ID"() {
        given: "two controls"
        def control1 = txTemplate.execute({
            controlRepository.save(newControl(unit, {
                name = "old name 1"
            }))
        })
        def control2 = txTemplate.execute({
            controlRepository.save(newControl(unit, {
                name = "old name 2"
            }))
        })
        when: "a put request tries to update control 1 using the ID of control 2"
        Map headers = [
            'If-Match': ETag.from(control1.id.uuidValue(), 1)
        ]
        put("/controls/${control2.id.uuidValue()}", [
            id: control1.id.uuidValue(),
            name: "new name 1",
            owner: [targetUri: '/units/' + unit.id.uuidValue()]
        ], headers, false)
        then: "an exception is thrown"
        thrown(DeviatingIdException)
    }
}
