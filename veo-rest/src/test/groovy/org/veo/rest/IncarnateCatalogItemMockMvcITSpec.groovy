/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2021  Urs Zeidler
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

import org.springframework.security.test.context.support.WithUserDetails

import org.veo.core.entity.CatalogItem
import org.veo.core.entity.Domain
import org.veo.core.entity.Unit
import org.veo.core.entity.specification.ClientBoundaryViolationException

class IncarnateCatalogItemMockMvcITSpec extends CatalogSpec {
    def basePath ="units"

    @WithUserDetails("user@domain.example")
    def "retrieve the apply info "() {
        given: "the created catalogitems"

        when: "a request is made to the server"
        def result = getIncarnationDescriptions(unit,item1)

        then: "the parameter object is returned"
        result.parameters.size() == 1
    }

    @WithUserDetails("user@domain.example")
    def "retrieve the apply info for two items"() {
        given: "the created catalogitems"

        when: "a request is made to the server"
        def result = getIncarnationDescriptions(unit,item1, item2)

        then: "the parameter object is returned"
        result.parameters.size() == 2
    }

    @WithUserDetails("user@domain.example")
    def "retrieve the apply info unit of other client"() {
        given: "the created catalogitems"

        when: "a request is made to the server"
        def results = get("/${basePath}/${unitSecondClient.id.uuidValue()}/incarnations?itemIds=${item1.id.uuidValue()}", false)

        then: "the data is rejected"
        ClientBoundaryViolationException ex = thrown()
    }

    @WithUserDetails("user@domain.example")
    def "retrieve the apply info of other client catalogItem"() {
        given: "the created catalogitems"

        when: "a request is made to the server"
        def results = get("/${basePath}/${unit.id.uuidValue()}/incarnations?itemIds=${otherItem.id.uuidValue()}", false)

        then: "the data is rejected"
        ClientBoundaryViolationException ex = thrown()
    }

    @WithUserDetails("user@domain.example")
    def "retrieve the apply info for item3 and post"() {
        given: "the created catalogitems"

        when: "a request is made to the server"
        def result = getIncarnationDescriptions(unit,item3)

        then: "the parameter object is returned"
        result.parameters.size() == 3

        when: "post the data"
        def postResult = postIncarnationDescriptions(unit,result)
        then: "the 3 elements are created"
        postResult.size() == 3
    }

    @WithUserDetails("user@domain.example")
    def "retrieve the apply info for item4 and post"() {
        given: "the created catalogitems"

        when: "a request is made to the server to create a c-3 element"
        def result = getIncarnationDescriptions(unit,item3)
        then: "it contains 3 elements to create, item1 item2 item3, because of the tailor references in item3"
        result.parameters.size() == 3

        when: "we create Item1/2/3"
        def postResult = postIncarnationDescriptions(unit,result)
        def elementList = postResult.collect{it.targetUri}
        then: "3 objects are created"
        postResult.size() == 3

        when: "we apply item4, the process links two previously created controls"
        result = getIncarnationDescriptions(unit,item4)
        then: "the parameter object is returned and the links are set to item1 and item2"
        result.parameters.size() == 1
        result.parameters[0].references.size() == 2
        elementList.contains(result.parameters[0].references[0].referencedCatalogable.targetUri)
        elementList.contains(result.parameters[0].references[1].referencedCatalogable.targetUri)

        when: "post the data to create item4 with the unaltered links set to item1 and item2"
        postResult = postIncarnationDescriptions(unit,result)
        then: "the parameter object is returned"
        postResult.size() == 1
        postResult[0].searchesUri == "http://localhost/processes/searches"

        when: "we get the created process"
        def processResult = parseJson(get(postResult[0].targetUri))
        then: "the process is created and linked with the controls created from item1 and item2"
        validateNewElementAgainstCatalogItem(processResult, item4, domain)
        processResult.owner.displayName == 'Test unit'
        processResult.links.link_to_item_1.domains.size() == 1
        processResult.links.link_to_item_2.domains.size() == 1
        elementList.contains(processResult.links.link_to_item_1.target.targetUri[0])
        elementList.contains(processResult.links.link_to_item_2.target.targetUri[0])
    }

    @WithUserDetails("user@domain.example")
    def "retrieve the apply info for item4, relink and post"() {
        given: "the created catalogitems c-1 c-2 c-3"
        def result = getIncarnationDescriptions(unit,item3)
        def postResult = postIncarnationDescriptions(unit,result)
        def elementList = postResult.collect{it.targetUri}
        result = parseJson(get("/${basePath}/${unit.id.uuidValue()}/incarnations?itemIds=${item4.id.uuidValue()}"))

        when: "post the data to create item4 with the altered links set to c-3"
        result.parameters[0].references[0].referencedCatalogable.targetUri = elementList[0]
        result.parameters[0].references[1].referencedCatalogable.targetUri = elementList[0]

        postResult = postIncarnationDescriptions(unit,result)
        then: "the parameter object is returned"
        postResult.size() == 1
        postResult[0].searchesUri == "http://localhost/processes/searches"

        when: "we get the created process"
        def processResult = parseJson(get(postResult[0].targetUri))
        then: "the process is created and linked with c-3"
        processResult.links.link_to_item_1.target.targetUri[0] == elementList[0]
        processResult.links.link_to_item_2.target.targetUri[0] == elementList[0]
    }


    @WithUserDetails("user@domain.example")
    def "retrieve the apply info for item5 and post"() {
        given: "the created catalogitems"
        when: "a request is made to the server to create a p2 element"
        def result = getIncarnationDescriptions(unit,item5)

        then: "it contains 1 elements to create, item5"
        result.parameters.size() == 1

        when: "we create Item5"
        def postResult = postIncarnationDescriptions(unit,result)
        then: "1 objects are created"
        postResult.size() == 1

        when: "we get the created process"
        def processResult = parseJson(get(postResult[0].targetUri))
        then: "the process is created and the subtype is set"

        validateNewElementAgainstCatalogItem(processResult, item5, domain)
        processResult.owner.displayName == 'Test unit'
        processResult.subType[domain.id.uuidValue()] == "MY_SUBTYPE"
    }

    @WithUserDetails("user@domain.example")
    def "retrieve the apply info for item6 and post"() {
        given: "the created catalogitems and the control c1"

        def result = getIncarnationDescriptions(unit,item1)
        def postResult = postIncarnationDescriptions(unit,result)

        when: "a request is made to the server to create a p3-all-features element"
        result = getIncarnationDescriptions(unit,item6)

        then: "it contains 1 elements to create, item6"
        result.parameters.size() == 1

        when: "we create Item6"
        postResult = postIncarnationDescriptions(unit,result)
        then: "1 object is created"
        postResult.size() == 1

        when: "we get the created process"
        def processResult = parseJson(get(postResult[0].targetUri))
        then: "the process is created and all the features are set"

        validateNewElementAgainstCatalogItem(processResult, item6, domain)
        processResult.owner.displayName == 'Test unit'
        processResult.subType[domain.id.uuidValue()] == "MY_SUBTYPE"
    }

    @WithUserDetails("user@domain.example")
    def "retrieve the apply info for item3 and post in other client unit"() {
        given: "the created catalogitems"

        when: "a request is made to the server"
        def result = getIncarnationDescriptions(unit,item3)
        then: "the parameter object is returned"
        result.parameters.size() == 3

        when: "post the data"
        def postResults = post("/${basePath}/${unitSecondClient.id.uuidValue()}/incarnations",result, false)
        then: "the data is rejected"
        ClientBoundaryViolationException ex = thrown()
    }

    private getIncarnationDescriptions(Unit unit, CatalogItem... items) {
        parseJson(get("/${basePath}/${unit.id.uuidValue()}/incarnations?itemIds=${items.collect{it.id.uuidValue()}.join(',')}"))
    }

    private postIncarnationDescriptions(Unit unit, incarnationDescriptions) {
        parseJson(post("/${basePath}/${unit.id.uuidValue()}/incarnations",incarnationDescriptions))
    }


    private validateNewElementAgainstCatalogItem(catalogable, CatalogItem catalogItem, Domain domain) {
        verifyAll(catalogItem.element) {
            it.name == catalogable.name
            it.abbreviation == catalogable.abbreviation
            it.description == catalogable.description
            it.links.size() == catalogable.links.size()
            it.customAspects.size() == catalogable.customAspects.size()
        }
        verifyAll(catalogable) {
            it.domains.size() == 1
            it.domains[0].targetUri.endsWith(domain.id.uuidValue())
        }
        catalogable.links.each {
            assert it.value.domains.size() == 1
            assert it.value.domains[0].targetUri[0].endsWith(domain.id.uuidValue())
        }
        catalogable.customAspects.each {
            assert it.value.domains.size() == 1
            assert it.value.domains[0].targetUri.endsWith(domain.id.uuidValue())
        }
        true
    }
}
