/*******************************************************************************
 * Copyright (c) 2020 Jonas Jordan.
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
package org.veo.core.entity

import org.veo.persistence.entity.jpa.DomainData
import org.veo.test.VeoSpec

class ClientSpec extends VeoSpec{
    def "Create a new Client"() {
        given: "a Client name"
        String domainName = 'Test domain'
        String clientName = 'Test Client'

        when : "Client is created"
        Domain domain = newDomain() {
            name = domainName
        }
        Client client = newClient() {
            name = clientName
        }
        // domain.client is set by calling client.setDomains():
        client.setDomains([domain] as Set)

        then: "domain is correctly initialized"
        client.getName().equals(clientName)
        client.getDomains().size() == 1
        client.getDomains().first().getName().equals(domainName)
        domain.owner == client
    }
}
