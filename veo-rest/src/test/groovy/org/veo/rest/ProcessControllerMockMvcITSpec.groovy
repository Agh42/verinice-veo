/*******************************************************************************
 * Copyright (c) 2019 Alexander Koderman.
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

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.bind.MethodArgumentNotValidException

import org.veo.core.VeoMvcSpec
import org.veo.core.entity.Client
import org.veo.core.entity.CustomProperties
import org.veo.core.entity.Domain
import org.veo.core.entity.Key
import org.veo.core.entity.Process
import org.veo.core.entity.Unit
import org.veo.persistence.access.ClientRepositoryImpl
import org.veo.persistence.access.ProcessRepositoryImpl
import org.veo.persistence.access.UnitRepositoryImpl
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
class ProcessControllerMockMvcITSpec extends VeoMvcSpec {

    @Autowired
    private ClientRepositoryImpl clientRepository

    @Autowired
    private ProcessRepositoryImpl processRepository

    @Autowired
    private UnitRepositoryImpl unitRepository

    @Autowired
    TransactionTemplate txTemplate
    @Autowired
    private EntityDataFactory entityFactory

    private Unit unit
    private Unit unit2
    private Domain domain
    private Domain domain1
    private Key clientId = Key.uuidFrom(WebMvcSecurityConfiguration.TESTCLIENT_UUID)

    def setup() {
        txTemplate.execute {
            domain = entityFactory.createDomain()
            domain.description = "ISO/IEC"
            domain.abbreviation = "ISO"
            domain.name = "ISO"
            domain.id = Key.newUuid()

            domain1 = entityFactory.createDomain()
            domain1.description = "ISO/IEC2"
            domain1.abbreviation = "ISO"
            domain1.name = "ISO"
            domain1.id = Key.newUuid()

            def client= entityFactory.createClient()
            client.id = clientId
            client.domains = [domain, domain1] as Set

            unit = entityFactory.createUnit()
            unit.name = "Test unit"
            unit.id = Key.newUuid()

            unit.client = client
            Client c = clientRepository.save(client)

            unit2 = entityFactory.createUnit()
            unit2.name = "Test unit2"
            unit2.id = Key.newUuid()

            unit2.client = client

            clientRepository.save(client)
            unitRepository.save(unit)
            unitRepository.save(unit2)
        }
    }

    @WithUserDetails("user@domain.example")
    def "create a process"() {
        given: "a request body"

        Map request = [
            name: 'New process',
            owner: [
                displayName: 'test2',
                href: '/units/' + unit.id.uuidValue()
            ]
        ]

        when: "a request is made to the server"

        def results = post('/processes', request)

        then: "the process is created and a status code returned"
        results.andExpect(status().isCreated())

        and: "the location of the new unit is returned"
        def result = new JsonSlurper().parseText(results.andReturn().response.contentAsString)
        result.success == true
        def resourceId = result.resourceId
        resourceId != null
        resourceId != ''
        result.message == 'Process created successfully.'
    }

    @WithUserDetails("user@domain.example")
    def "try to create a process without owner"() {
        given: "a request body without an owner"

        Map request = [
            name: 'New process'
        ]

        when: "a request is made to the server"
        def results = post('/processes', request, false)

        then: "the process is not created"
        MethodArgumentNotValidException ex = thrown()

        and: "the reason is given"
        ex.message ==~ /.*Validation failed for argument.*owner must be present.*/
    }

    @WithUserDetails("user@domain.example")
    def "retrieve a process"() {
        given: "a saved process"
        def id = Key.newUuid()
        def process = entityFactory.createProcess()
        process.id = id
        process.name = 'Test process'
        process.owner = unit

        process = txTemplate.execute {
            processRepository.save(process)
        }

        when: "a request is made to the server"
        def results = get("/processes/${process.id.uuidValue()}")

        then: "the process is found"
        results.andExpect(status().isOk())
        def result = new JsonSlurper().parseText(results.andReturn().response.contentAsString)
        result.name == 'Test process'
        result.owner.href == "/units/"+unit.id.uuidValue()
    }

    @WithUserDetails("user@domain.example")
    def "try to put a process without a name"() {
        given: "a saved process"

        Key<UUID> id = Key.newUuid()
        def process = entityFactory.createProcess()
        process.id = id
        process.owner = unit
        process.name = 'Test process-put-noname'
        process.domains = [domain1] as Set

        process = txTemplate.execute {
            processRepository.save(process)
        }

        Map request = [
            id: id.uuidValue(),
            // note that currently the name must not be null but it can be empty ("")
            abbreviation: 'u-2',
            description: 'desc',
            owner:
            [
                href: '/units/'+unit.id.uuidValue(),
                displayName: 'test unit'
            ]
            ,
            domains: [
                [
                    href: '/domains/'+domain.id.uuidValue(),
                    displayName: 'test ddd'
                ]
            ]
        ]

        when: "a request is made to the server"
        def results = put("/processes/${process.id.uuidValue()}", request, false)

        then: "the process is not updated"
        MethodArgumentNotValidException ex = thrown()

        and: "the reason is given"
        ex.message ==~ /.*Validation failed for argument.*name must be present.*/

    }

    @WithUserDetails("user@domain.example")
    def "put a process"() {
        given: "a saved process"

        Key<UUID> id = Key.newUuid()
        def process = entityFactory.createProcess()
        process.id = id
        process.owner = unit
        process.name = 'Test process-put'
        process.domains = [domain1] as Set

        process = txTemplate.execute {
            processRepository.save(process)
        }

        Map request = [
            id: id.uuidValue(),
            name: 'New Process-2',
            abbreviation: 'u-2',
            description: 'desc',
            owner:
            [
                href: '/units/'+unit.id.uuidValue(),
                displayName: 'test unit'
            ]
            ,
            domains: [
                [
                    href: '/domains/'+domain.id.uuidValue(),
                    displayName: 'test ddd'
                ]
            ]
        ]

        when: "a request is made to the server"
        def results = put("/processes/${process.id.uuidValue()}", request)

        then: "the process is found"
        results.andExpect(status().isOk())
        def result = new JsonSlurper().parseText(results.andReturn().response.contentAsString)
        result.name == 'New Process-2'
        result.abbreviation == 'u-2'
        result.domains.first().displayName == domain.abbreviation+" "+domain.name
        result.owner.href == "/units/"+unit.id.uuidValue()
    }

    @WithUserDetails("user@domain.example")
    def "delete a process"() {

        given: "an existing process"
        Key<UUID> id = Key.newUuid()
        def process = entityFactory.createProcess()
        process.id = id
        process.owner = unit
        process.name = 'Test process-delete'
        process.domains = [domain1] as Set

        process = txTemplate.execute {
            processRepository.save(process)
        }

        when: "a delete request is sent to the server"
        def results = delete("/processes/${process.id.uuidValue()}")

        then: "the process is deleted"
        results.andExpect(status().isOk())
        processRepository.exists(id) == false
    }

    @WithUserDetails("user@domain.example")
    def "put a process with custom aspect"() {
        given: "a saved process"

        CustomProperties cp = entityFactory.createCustomProperties()
        cp.setType("my.new.type")
        cp.setApplicableTo(['Process'] as Set)
        cp.setId(Key.newUuid())
        Key<UUID> id = Key.newUuid()


        def process = entityFactory.createProcess()
        process.id = id
        process.name = 'Test process-put'
        process.owner = unit
        process.domains = [domain1] as Set
        process.customAspects = [cp] as Set

        process = txTemplate.execute {
            processRepository.save(process)
        }

        Map request = [
            id: id.uuidValue(),
            name: 'New Process-2',
            abbreviation: 'u-2',
            description: 'desc',
            owner:
            [
                href: '/units/'+unit.id.uuidValue(),
                displayName: 'test unit'
            ]
            ,
            domains: [
                [
                    href: '/domains/'+domain.id.uuidValue(),
                    displayName: 'test ddd'
                ]
            ],
            customAspects:
            [
                'my.aspect-test' :
                [
                    id: '00000000-0000-0000-0000-000000000000',
                    type : 'my.aspect-test1',
                    applicableTo: [
                        "Process"
                    ],
                    domains: [],
                    attributes: [
                        test1:'value1',
                        test2:'value2'
                    ]
                ]

            ]
        ]

        when: "a request is made to the server"
        def results = put("/processes/${process.id.uuidValue()}", request)

        then: "the process is found"
        results.andExpect(status().isOk())
        def result = new JsonSlurper().parseText(results.andReturn().response.contentAsString)
        result.name == 'New Process-2'
        result.abbreviation == 'u-2'
        result.domains.first().displayName == domain.abbreviation+" "+domain.name
        result.owner.href == "/units/"+unit.id.uuidValue()

        when:
        def entity = txTemplate.execute {
            processRepository.findById(id).get().tap {
                // make sure that the proxy is resolved
                customAspects.first()
            }
        }

        then:
        entity.name == 'New Process-2'
        entity.abbreviation == 'u-2'
        entity.customAspects.first().type == 'my.aspect-test1'
        entity.customAspects.first().applicableTo == ['Process'] as Set
        entity.customAspects.first().stringProperties.test1 == 'value1'
        entity.customAspects.first().stringProperties.test2 == 'value2'
    }

    @WithUserDetails("user@domain.example")
    def "overwrite a custom aspect attribute"() {
        given: "a saved process"

        CustomProperties cp = entityFactory.createCustomProperties()
        cp.setType("my.new.type")
        cp.setApplicableTo(['Process'] as Set)
        cp.setId(Key.newUuid())
        cp.setProperty('test1', 'value1')
        Key<UUID> id = Key.newUuid()
        def process = entityFactory.createProcess()
        process.id = id
        process.owner = unit
        process.name = 'Test process-put'
        process.domains = [domain1] as Set
        process.customAspects = [cp] as Set

        process = txTemplate.execute {
            processRepository.save(process)
        }

        when: "a request is made to the server"
        Map request = [
            id: id.uuidValue(),
            name: 'New Process-2',
            abbreviation: 'u-2',
            description: 'desc',
            owner:
            [
                href: '/units/'+unit.id.uuidValue(),
                displayName: 'test unit'
            ]
            ,
            domains: [
                [
                    href: '/domains/'+domain.id.uuidValue(),
                    displayName: 'test ddd'
                ]
            ],
            customAspects:
            [
                'my.new.type' :
                [
                    id: cp.id.uuidValue(),
                    type : 'my.new.type',
                    applicableTo: [
                        "Process"
                    ],
                    domains: [],
                    attributes: [
                        test1:'value2'
                    ]
                ]
            ]
        ]
        def results = put("/processes/${process.id.uuidValue()}", request)

        then: "the process is found"
        results.andExpect(status().isOk())
        def result = new JsonSlurper().parseText(results.andReturn().response.contentAsString)
        result.name == 'New Process-2'
        result.abbreviation == 'u-2'
        result.domains.first().displayName == domain.abbreviation+" "+domain.name
        result.owner.href == "/units/"+unit.id.uuidValue()

        when:
        def entity = txTemplate.execute {
            processRepository.findById(id).get().tap {
                // make sure that the proxy is resolved
                customAspects.first()
            }
        }

        then:
        entity.name == 'New Process-2'
        entity.abbreviation == 'u-2'
        entity.customAspects.first().type == 'my.new.type'
        entity.customAspects.first().applicableTo == ['Process'] as Set
        entity.customAspects.first().stringProperties.test1 == 'value2'
    }

    @WithUserDetails("user@domain.example")
    def "id is required in custom aspects"() {
        given: "a saved process"

        CustomProperties cp = entityFactory.createCustomProperties()
        cp.setType("my.new.type")
        cp.setApplicableTo(['Process'] as Set)
        cp.setId(Key.newUuid())
        Key<UUID> id = Key.newUuid()
        def process = entityFactory.createProcess()
        process.id = id
        process.owner = unit
        process.name = 'Test process-put'
        process.domains = [domain1] as Set
        process.customAspects = [cp] as Set

        process = txTemplate.execute {
            processRepository.save(process)
        }

        Map request = [
            id: id.uuidValue(),
            name: 'New Process-2',
            abbreviation: 'u-2',
            description: 'desc',
            owner:
            [
                href: '/units/'+unit.id.uuidValue(),
                displayName: 'test unit'
            ]
            ,
            domains: [
                [
                    href: '/domains/'+domain.id.uuidValue(),
                    displayName: 'test ddd'
                ]
            ],
            customAspects:
            [
                'my.aspect-test' :
                [
                    type : 'my.aspect-test1',
                    applicableTo: [
                        "Process"
                    ],
                    domains: [],
                    attributes: [
                        test1:'value1',
                        test2:'value2'
                    ]
                ]

            ]
        ]

        when: "a request is made to the server"
        def results = put("/processes/${process.id.uuidValue()}", request, false)

        then: "the process is not updated"
        MethodArgumentNotValidException ex = thrown()

        and: "the reason is given"
        ex.message ==~ /.*Validation failed for argument.*must not be null.*/
    }

    @WithUserDetails("user@domain.example")
    def "put a process with link"() {
        given: "a created asset and process"

        Map createAssetRequest = [
            name: 'New Asset',
            owner: [
                displayName: 'test2',
                href: '/units/' + unit.id.uuidValue()
            ]
        ]

        def creatAssetResponse = post('/assets', createAssetRequest)

        def createAssetResult = new JsonSlurper().parseText(creatAssetResponse.andReturn().response.contentAsString)

        Map createProcessRequest = [
            name: 'New process',
            owner: [
                displayName: 'test2',
                href: '/units/' + unit.id.uuidValue()
            ]
        ]

        def createProcessResponse = post('/processes', createProcessRequest)
        def createProcessResult = new JsonSlurper().parseText(createProcessResponse.andReturn().response.contentAsString)
        def processId = createProcessResult.resourceId

        Map putProcessRequest = [
            id: processId,
            name: 'New Process-2',
            abbreviation: 'u-2',
            description: 'desc',
            owner:
            [
                href: '/units/'+unit.id.uuidValue(),
                displayName: 'test unit'
            ]
            ,
            domains: [
                [
                    href: '/domains/'+domain.id.uuidValue(),
                    displayName: 'test ddd'
                ]
            ],
            links:
            [
                'my.link-test' : [
                    [
                        id: '00000000-0000-0000-0000-000000000000',
                        type : 'my.link-test',
                        applicableTo: [
                            "Process"
                        ],
                        name:'test link prcess->asset',
                        domains: [],
                        attributes: [
                            test1:'value1',
                            test2:'value2'
                        ],
                        target:
                        [
                            href: '/assets/'+createAssetResult.resourceId,
                            displayName: 'test ddd'
                        ]
                    ]]
            ]
        ]

        when: "a request is made to the server"
        def results = put("/processes/${createProcessResult.resourceId}", putProcessRequest)

        then: "the process is found"
        results.andExpect(status().isOk())
        def result = new JsonSlurper().parseText(results.andReturn().response.contentAsString)
        result.name == 'New Process-2'
        result.abbreviation == 'u-2'
        result.domains.first().displayName == domain.abbreviation+" "+domain.name
        result.owner.href == "/units/"+unit.id.uuidValue()
        and: 'there is one type of links'
        def links = result.links
        links.size() == 1
        and: 'there is one link of the expected type'
        def linksOfExpectedType = links.'my.link-test'
        linksOfExpectedType.size() == 1
        and: 'the expected link is present'
        linksOfExpectedType.first().name == 'test link prcess->asset'
    }

    @WithUserDetails("user@domain.example")
    def "post a process with link"() {
        when:
        def result = parseJson(post('/assets', [
            name : 'My asset',
            owner: [
                href: '/units/'+unit.id.uuidValue()
            ]
        ]))
        def assetId = result.resourceId
        result = parseJson(post('/processes', [
            name : 'My process',
            owner: [
                href: '/units/'+unit.id.uuidValue()
            ],
            links: [
                'Process_depends_on_Asset': [
                    [
                        id    : '00000000-0000-0000-0000-000000000000',
                        type  : 'Process_depends_on_Asset',
                        name  : 'requires',
                        target:
                        [
                            href: "/assets/$assetId"
                        ]
                    ]
                ]
            ]
        ]))
        def processId = result.resourceId
        def process1 = txTemplate.execute{
            Process process = processRepository.findById(Key.uuidFrom(processId)).get()
            with(process.links) {
                //need to be in the open session
                size() == 1
                first().type == 'Process_depends_on_Asset'
                first().name == 'requires'
                first().target.id.uuidValue() == assetId
            }
            return process
        }
        then:
        process1 != null
    }

    @WithUserDetails("user@domain.example")
    def "retrieve all processes for a client"() {
        given: "a saved process"

        def process = entityFactory.createProcess()
        process.id = Key.newUuid()
        process.name = 'Test process-1'
        process.owner = unit


        def process2 = entityFactory.createProcess()
        process2.id = Key.newUuid()
        process2.name = 'Test process-2'
        process2.owner = unit2


        (process, process2) = txTemplate.execute {
            [process, process2].collect(processRepository.&save)
        }

        when: "a request is made to the server"
        def results = get("/processes")

        then: "the processes are returned"
        results.andExpect(status().isOk())
        when:
        def result = new JsonSlurper().parseText(results.andReturn().response.contentAsString)
        then:
        result.size == 2
        when:
        def sortedResult = result.sort{ it.name }
        then:
        sortedResult.first().name == 'Test process-1'
        sortedResult.first().owner.href == "/units/${unit.id.uuidValue()}"
        sortedResult[1].name == 'Test process-2'
        sortedResult[1].owner.href == "/units/${unit2.id.uuidValue()}"
    }

    @WithUserDetails("user@domain.example")
    def "retrieve all processes for a unit"() {
        given: "a saved process"

        def process = entityFactory.createProcess()
        process.id = Key.newUuid()
        process.name = 'Test process-1'
        process.owner = unit


        def process2 = entityFactory.createProcess()
        process2.id = Key.newUuid()
        process2.name = 'Test process-2'
        process2.owner = unit2


        (process, process2) = txTemplate.execute {
            [process, process2].collect(processRepository.&save)
        }

        when: "a request is made to the server"
        def results = get("/processes?parent=${unit.id.uuidValue()}")

        then: "the processes are returned"
        results.andExpect(status().isOk())

        when:
        def result = new JsonSlurper().parseText(results.andReturn().response.contentAsString)

        then:
        result.size == 1
        result.first().name == 'Test process-1'
        result.first().owner.href == "/units/"+unit.id.uuidValue()

        when: "a request is made to the server"
        results = get("/processes?parent=${unit2.id.uuidValue()}")

        then: "the processes are returned"
        results.andExpect(status().isOk())

        when:
        result = new JsonSlurper().parseText(results.andReturn().response.contentAsString)

        then:
        result.size == 1
        result.first().name == 'Test process-2'
        result.first().owner.href == "/units/"+unit2.id.uuidValue()
    }
}
