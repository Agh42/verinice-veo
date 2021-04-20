/*******************************************************************************
 * Copyright (c) 2021 Jonas Jordan.
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
package org.veo.core

import static com.vladmihalcea.sql.SQLStatementCountValidator.assertSelectCount
import static com.vladmihalcea.sql.SQLStatementCountValidator.reset

import javax.persistence.EntityManager

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.test.context.ActiveProfiles

import org.veo.core.entity.Client
import org.veo.core.entity.Domain
import org.veo.core.entity.Unit
import org.veo.core.repository.DomainRepository
import org.veo.persistence.access.AssetRepositoryImpl
import org.veo.persistence.access.ClientRepositoryImpl
import org.veo.persistence.access.ProcessRepositoryImpl
import org.veo.persistence.access.UnitRepositoryImpl
import org.veo.persistence.entity.jpa.CustomLinkData
import org.veo.persistence.entity.jpa.CustomPropertiesData
import org.veo.persistence.entity.jpa.ProcessData

@SpringBootTest(classes = EntityLayerSupertypeQueryImplPerformanceSpec.class)
@ComponentScan("org.veo")
@ActiveProfiles(["test", "stats"])
class EntityLayerSupertypeQueryImplPerformanceSpec extends VeoSpringSpec {

    @Autowired
    private ClientRepositoryImpl clientRepository

    @Autowired
    private DomainRepository domainRepository

    @Autowired
    private UnitRepositoryImpl unitRepository

    @Autowired
    private AssetRepositoryImpl assetRepository

    @Autowired
    private ProcessRepositoryImpl processRepository

    private Client client
    Domain domain
    private Unit unit

    @Autowired
    private EntityManager entityManager

    def setup() {
        client = clientRepository.save(newClient {})
        unit = unitRepository.save(newUnit(client))
        domain = domainRepository.save(newDomain {
            owner = this.client
        })
    }

    def "query efficiently fetches results"() {
        given:
        reset()
        final def testProcessCount = 10

        def asset = assetRepository.save(newAsset(unit))
        def processes = new HashSet<ProcessData>()
        for(int i = 0; i < testProcessCount; i++) {
            processes.add(newProcess(unit) {
                domains = [domain] as Set
                customAspects = [
                    new CustomPropertiesData().tap {
                        it.applicableTo = []
                        it.type = "my_custom_aspect"
                        it.setProperty("foo", "bar")
                    }] as Set
                links = [
                    new CustomLinkData().tap {
                        it.applicableTo = []
                        it.name = "no_name"
                        it.type = "my_little_link"
                        it.target = asset
                    }
                ] as Set
                setSubType(domain, "VT")
            })
        }
        processRepository.saveAll(processes)
        assertSelectCount(0)

        when:
        def result = processRepository.query(client).execute()

        then: "all data has been fetched"
        result.size() == testProcessCount
        with(result[0]) {
            customAspects.first().stringProperties["foo"] == "bar"
            domains.first() != null
            getSubType(domain) != null
            links.first() != null
        }

        // TODO: VEO-448 Reduce query selects to 2 by joining all that is EAGER now.
        // assertSelectCount(2)
        assertSelectCount(2 + testProcessCount)
    }
}
