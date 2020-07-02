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
package org.veo.core

import javax.validation.ConstraintViolationException

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.transaction.TransactionSystemException
import org.springframework.transaction.support.TransactionTemplate

import org.veo.core.entity.Domain
import org.veo.core.entity.Key
import org.veo.persistence.access.ProcessRepositoryImpl
import org.veo.persistence.access.jpa.ProcessDataRepository
import org.veo.persistence.entity.jpa.ProcessData

/**
 * Integration test for the process repository. Uses mocked spring MVC environment.
 * Uses JPA repositories with in-memory database.
 * Does not start an embedded server.
 * Uses a test Web-MVC configuration with example accounts and clients.
 */
@SpringBootTest(
webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
classes = ProcessRepositoryITSpec.class)
@ComponentScan("org.veo.rest")
class ProcessRepositoryITSpec extends VeoMvcSpec {

    @Autowired
    private ProcessRepositoryImpl processRepository

    @Autowired
    ProcessDataRepository processDataRepository

    @Autowired
    TransactionTemplate txTemplate

    private Domain domain1

    def "try to persist an invalid process at the persistence layer"() {
        given: "an invalid process object"
        Key<UUID> id = Key.newUuid()
        def processData = new ProcessData()
        processData.id = id

        when: "the process is saved using the repository"
        processDataRepository.save(processData)

        then: "the process is not saved"
        TransactionSystemException ex = thrown()

        and: "the reason is given"
        ConstraintViolationException cvex = ex.mostSpecificCause
        cvex.constraintViolations.size() == 2
        assert cvex.constraintViolations*.propertyPath*.toString() as Set == ["owner", "name"] as Set
        assert cvex.constraintViolations*.messageTemplate.every() {it == "{javax.validation.constraints.NotNull.message}" }
    }

    def "try to persist an invalid process entity"() {
        given: "an invalid process object"
        def process = newProcess null

        when: "the process is saved using the repository"
        txTemplate.execute {
            processRepository.save(process)
        }

        then: "the process is not saved"
        ConstraintViolationException cvex = thrown()

        and: "the reason is given"
        cvex.constraintViolations.size() == 1
        assert cvex.constraintViolations*.propertyPath*.toString() == ["owner"]
    }
}
