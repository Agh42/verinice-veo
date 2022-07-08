/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2022  Urs Zeidler.
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

import org.apache.http.HttpStatus

import groovy.util.logging.Slf4j
import spock.util.concurrent.PollingConditions

class DomainRestTestITSpec extends VeoRestTest {

    def setup() {
        postNewUnit("some unit")
    }

    def "export the test domain"() {
        when: "the catalog is retrieved"
        def dsgvoId = getDomains().find { it.name == "test-domain" }.id
        def domainDto = exportDomain(dsgvoId)

        then: "the domain is exported"
        with(domainDto.catalogs[0]) {
            catalogItems.size() == 6
            name == "TEST-Controls"
            domainTemplate.displayName == "td test-domain"
        }
    }

    def "export the dsgvo domain"() {
        when: "the catalog is retrieved"
        def dsgvoId = getDomains().find { it.name == "DS-GVO" }.id
        def domainDto = exportDomain(dsgvoId)

        def catalog = domainDto.catalogs[0]
        def vvt = catalog.catalogItems.find { it.element.abbreviation == "VVT" }
        def tomi = catalog.catalogItems.find { it.element.abbreviation == "TOM-I" }
        def dsg23 = catalog.catalogItems.find { it.element.abbreviation == "DS-G.23" }

        then: "the domain is exported"
        with(catalog) {
            catalogItems.size() == 65
            name == "DS-GVO"
        }
        with (vvt) {
            namespace == "TOM.VVT"
            tailoringReferences.size() == 8
            with (element) {
                description == "VVT-Prozess"
                domains[dsgvoId].subType == "PRO_DataProcessing"
                domains[dsgvoId].status == "NEW"
            }
        }
        with (tomi.element) {
            customAspects.size() == 1
            customAspects.control_dataProtection.attributes.size() == 1
            customAspects.control_dataProtection.attributes.control_dataProtection_objectives == [
                "control_dataProtection_objectives_integrity"
            ]
            domains[dsgvoId].subType == "CTL_TOM"
            domains[dsgvoId].status == "NEW"
        }
        with (dsg23) {
            with (element) {
                name == "Keine Widerspruchsmöglichkeit für Betroffene gegen die Datenverarbeitung"
                domains[dsgvoId].subType == "SCN_Scenario"
                domains[dsgvoId].status == "NEW"
            }
        }
    }

    def "create domaintemplate from domain"() {
        when: "we create a domain with a profile"

        def domain = getDomains().find { it.name == "DS-GVO" }
        def dsgvoId = domain.id
        def oldVersion = domain.templateVersion

        def unitId = postNewUnit("the unit formerly known as demo unit").resourceId

        def assetId = post("/assets", [
            name: "target asset for process",
            domains: [
                (dsgvoId): [
                    subType: "AST_Datatype",
                    status: "NEW"
                ]
            ],
            owner: [targetUri: "$baseUrl/units/$unitId"],
        ]).body.resourceId

        def rest = post("/domains/${dsgvoId}/createdomaintemplate",
                [version : "1.4.1",
                    profiles: ["demoUnit": (unitId)]
                ],
                201,
                UserType.CONTENT_CREATOR)

        def templateId = rest.body.targetUri.split('/').last()

        then:"the domain template is created"
        rest.body.displayName == "DS-GVO DS-GVO"

        when: "we create a domain from the template"

        post("/domaintemplates/${templateId}/createdomains", [:], HttpStatus.SC_NO_CONTENT, UserType.ADMIN)

        then: "the client gets the new domain"
        new PollingConditions().within(5) {
            with(getDomains().find { it.name == "DS-GVO" && it.templateVersion=="1.4.1" }) {
                oldVersion != templateVersion
            }
        }
    }
}