/*******************************************************************************
 * Copyright (c) 2020 Alexander Koderman.
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
package org.veo.core.usecase


import org.veo.core.entity.Asset
import org.veo.core.entity.Control
import org.veo.core.entity.Document
import org.veo.core.entity.Incident
import org.veo.core.entity.Key
import org.veo.core.entity.ModelGroup
import org.veo.core.entity.Person
import org.veo.core.entity.Process
import org.veo.core.entity.Scenario
import org.veo.core.entity.Unit
import org.veo.core.usecase.repository.AssetRepository
import org.veo.core.usecase.repository.ControlRepository
import org.veo.core.usecase.repository.DocumentRepository
import org.veo.core.usecase.repository.EntityGroupRepository
import org.veo.core.usecase.repository.IncidentRepository
import org.veo.core.usecase.repository.PersonRepository
import org.veo.core.usecase.repository.ProcessRepository
import org.veo.core.usecase.repository.ScenarioRepository
import org.veo.core.usecase.unit.DeleteUnitUseCase

public class DeleteUnitUseCaseSpec extends UseCaseSpec {
    def "Delete a unit with subunits" () {
        given: "starting values for a unit"
        def uid = Key.newUuid()
        Unit existingUnit = Mock(Unit)
        existingUnit.id >> uid
        existingUnit.client >> existingClient
        Unit subUnit = Mock(Unit)
        def assetReporitory = Mock(AssetRepository)
        def controlReporitory = Mock(ControlRepository)
        def documentReporitory = Mock(DocumentRepository)
        def incidentReporitory = Mock(IncidentRepository)
        def personReporitory = Mock(PersonRepository)
        def processReporitory = Mock(ProcessRepository)
        def scenarioReporitory = Mock(ScenarioRepository)
        def entityGroupRepository = Mock(EntityGroupRepository)
        when: "the unit is deleted"
        def input = new DeleteUnitUseCase.InputData(existingUnit.getId(), existingClient)
        def usecase = new DeleteUnitUseCase(clientRepository, unitRepository, repositoryProvider)
        usecase.execute(input)

        then: "the client for the unit is retrieved"
        1 * repositoryProvider.getEntityLayerSupertypeRepositoryFor(ModelGroup) >> entityGroupRepository
        1 * repositoryProvider.getEntityLayerSupertypeRepositoryFor(Asset) >> assetReporitory
        1 * repositoryProvider.getEntityLayerSupertypeRepositoryFor(Control) >> controlReporitory
        1 * repositoryProvider.getEntityLayerSupertypeRepositoryFor(Document) >> documentReporitory
        1 * repositoryProvider.getEntityLayerSupertypeRepositoryFor(Incident) >> incidentReporitory
        1 * repositoryProvider.getEntityLayerSupertypeRepositoryFor(Person) >> personReporitory
        1 * repositoryProvider.getEntityLayerSupertypeRepositoryFor(Process) >> processReporitory
        1 * repositoryProvider.getEntityLayerSupertypeRepositoryFor(Scenario) >> scenarioReporitory
        1 * clientRepository.findById(_) >> Optional.of(existingClient)
        1 * unitRepository.findById(_) >> Optional.of(existingUnit)
        1 * entityGroupRepository.deleteByUnit(existingUnit)
        1 * assetReporitory.deleteByUnit(existingUnit)
        1 * controlReporitory.deleteByUnit(existingUnit)
        1 * documentReporitory.deleteByUnit(existingUnit)
        1 * incidentReporitory.deleteByUnit(existingUnit)
        1 * personReporitory.deleteByUnit(existingUnit)
        1 * processReporitory.deleteByUnit(existingUnit)
        1 * scenarioReporitory.deleteByUnit(existingUnit)
        and: "the unit is deleted"
        1 * unitRepository.delete(_)
    }
}
