/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2022  Jonas Jordan
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
package org.veo.rest.test

import static java.util.UUID.randomUUID

class ClientSeparationRestTest extends VeoRestTest {
    private String defaultClientUnitUrl
    private String defaultClientDomainId

    private String secondaryClientUnitUrl
    private String secondaryClientDomainId

    def setup() {
        defaultClientUnitUrl = "$baseUrl/units/" + post("/units", [name: "default client's unit"], 201, UserType.DEFAULT).body.resourceId
        defaultClientDomainId = get("/domains", 200, UserType.DEFAULT).body.find{it.name == "test-domain"}.id

        secondaryClientUnitUrl = "$baseUrl/units/" + post("/units", [name: "secondary client's unit"], 201, UserType.SECONDARY_CLIENT_USER).body.resourceId
        secondaryClientDomainId = get("/domains", 200, UserType.SECONDARY_CLIENT_USER).body.find{it.name == "test-domain"}.id
    }

    def "secondary client's element cannot be accessed"() {
        given: "a control in secondary client's unit"
        def secondaryClientControlId = post("/controls", [
            name: "secondary client's control",
            owner: [targetUri: secondaryClientUnitUrl],
            domains: [
                (secondaryClientDomainId): [
                    "subType": "TOM",
                    "status": "NEW",
                ]
            ],
        ], 201, UserType.SECONDARY_CLIENT_USER).body.resourceId

        expect: "the secondary client to be able to retrieve the control"
        get("/controls/$secondaryClientControlId", 200, UserType.SECONDARY_CLIENT_USER)
                .body
                .name == "secondary client's control"

        when: "default client tries to retrieve the control"
        def illegalGetResponse = get("/controls/$secondaryClientControlId", 404, UserType.DEFAULT)

        then: "the existence of secondary client's control is concealed"
        illegalGetResponse.body.message == "Control with ID $secondaryClientControlId not found"

        and: "response is indistinguishable from a non-existing control response"
        with(randomUUID().toString()) {nonExistingId ->

            def nonExistingControlGetResponse = get("/controls/$nonExistingId", 404)
            illegalGetResponse.body.message == nonExistingControlGetResponse.body.message.replace(nonExistingId, secondaryClientControlId)
        }

        when: "default client tries to update the control and move it into their own unit"
        def eTag = get("/controls/$secondaryClientControlId", 200, UserType.SECONDARY_CLIENT_USER).parseETag()
        def body = [
            name: "secondary client's control",
            owner: [targetUri: defaultClientUnitUrl],
            domains: [
                (defaultClientDomainId): [
                    "subType": "TOM",
                    "status": "NEW",
                ]
            ],
        ]
        def illegalPutResponse = put("/controls/$secondaryClientControlId", body, eTag, 404, UserType.DEFAULT)

        then: "the existence of secondary client's control is concealed"
        illegalPutResponse.body.message == "Control with ID $secondaryClientControlId not found"

        and: "response is indistinguishable from a non-existing control response"
        with(randomUUID().toString()) {nonExistingId ->
            def nonExistingControlPutResponse = put("/controls/$nonExistingId", body, eTag, 404)
            illegalPutResponse.body.message == nonExistingControlPutResponse.body.message.replace(nonExistingId, secondaryClientControlId)
        }

        when: "default client tries to delete the control"
        def illegalDeleteResponse = delete("/controls/$secondaryClientControlId", 404, UserType.DEFAULT)

        then: "the existence of secondary client's control is concealed"
        illegalDeleteResponse.body.message == "Control with ID $secondaryClientControlId not found"

        and: "response is indistinguishable from a non-existing control response"
        with(randomUUID().toString()) {nonExistingId ->
            def nonExistingControlDeleteResponse = delete("/controls/$nonExistingId", 404)
            illegalDeleteResponse.body.message == nonExistingControlDeleteResponse.body.message.replace(nonExistingId, secondaryClientControlId)
        }
    }
}
