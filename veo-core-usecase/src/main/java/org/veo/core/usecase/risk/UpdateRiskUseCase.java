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
package org.veo.core.usecase.risk;

import javax.transaction.Transactional;

import org.veo.core.entity.AbstractRisk;
import org.veo.core.entity.Control;
import org.veo.core.entity.Domain;
import org.veo.core.entity.Person;
import org.veo.core.entity.RiskAffected;
import org.veo.core.entity.Scenario;
import org.veo.core.repository.RepositoryProvider;
import org.veo.core.usecase.common.ETag;
import org.veo.core.usecase.common.ETagMismatchException;

public class UpdateRiskUseCase<T extends RiskAffected<T, R>, R extends AbstractRisk<T, R>>
        extends AbstractRiskUseCase<T, R> {

    private final Class<T> entityClass;
    private final RepositoryProvider repositoryProvider;

    public UpdateRiskUseCase(RepositoryProvider repositoryProvider, Class<T> entityClass) {
        super(repositoryProvider);
        this.entityClass = entityClass;
        this.repositoryProvider = repositoryProvider;
    }

    @Transactional
    @Override
    public OutputData<R> execute(InputData input) {
        // Retrieve required entities for operation:
        var riskAffected = repositoryProvider.getRepositoryFor(entityClass)
                                             .findById(input.getRiskAffectedRef())
                                             .orElseThrow();

        var scenario = repositoryProvider.getRepositoryFor(Scenario.class)
                                         .findById(input.getScenarioRef())
                                         .orElseThrow();

        var domains = repositoryProvider.getRepositoryFor(Domain.class)
                                        .getByIds(input.getDomainRefs());

        var controlRepository = repositoryProvider.getRepositoryFor(Control.class);
        var mitigation = input.getControlRef()
                              .flatMap(controlRepository::findById);

        var personRepository = repositoryProvider.getRepositoryFor(Person.class);
        var riskOwner = input.getRiskOwnerRef()
                             .flatMap(personRepository::findById);

        var risk = riskAffected.getRisk(input.getScenarioRef())
                               .orElseThrow();

        // Validate input:
        checkETag(risk, input);
        riskAffected.checkSameClient(input.getAuthenticatedClient());
        scenario.checkSameClient(input.getAuthenticatedClient());
        checkClients(input.getAuthenticatedClient(), domains);
        mitigation.ifPresent(control -> control.checkSameClient(input.getAuthenticatedClient()));
        riskOwner.ifPresent(person -> person.checkSameClient(input.getAuthenticatedClient()));

        // Execute requested operation:
        return new OutputData<>(riskAffected.updateRisk(risk, domains, mitigation.orElse(null),
                                                        riskOwner.orElse(null)));
    }

    private void checkETag(AbstractRisk<T, R> risk, InputData input) {
        var riskAffectedId = risk.getEntity()
                                 .getId()
                                 .uuidValue();
        var scenarioId = risk.getScenario()
                             .getId()
                             .uuidValue();
        if (!ETag.matches(riskAffectedId, scenarioId, risk.getVersion(), input.getETag())) {
            throw new ETagMismatchException(
                    String.format("The eTag does not match for the element with the ID %s_%s",
                                  riskAffectedId, scenarioId));
        }
    }
}