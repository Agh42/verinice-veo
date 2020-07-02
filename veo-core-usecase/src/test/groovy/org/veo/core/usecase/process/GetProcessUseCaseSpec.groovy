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
package org.veo.core.usecase.process

import org.veo.core.entity.Key
import org.veo.core.entity.Process
import org.veo.core.usecase.UseCaseSpec
import org.veo.core.usecase.process.GetProcessUseCase.InputData
import org.veo.core.usecase.repository.ProcessRepository

class GetProcessUseCaseSpec extends UseCaseSpec {

    ProcessRepository processRepository = Mock()

    GetProcessUseCase usecase = new GetProcessUseCase(processRepository)

    def "retrieve a process"() {
        given:
        def id = Key.newUuid()
        Process process = Mock() {
            getOwner() >> existingUnit
            getId() >> id
        }
        when:
        def outputProcess = usecase.execute(new InputData(id,  existingClient))
        then:
        1 * processRepository.findById(id,_) >> Optional.of(process)
        outputProcess != null
        outputProcess.id == id
    }
}