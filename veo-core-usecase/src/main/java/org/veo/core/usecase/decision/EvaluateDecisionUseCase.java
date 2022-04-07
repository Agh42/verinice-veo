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
package org.veo.core.usecase.decision;

import static javax.transaction.Transactional.TxType.NEVER;

import java.util.UUID;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.veo.core.entity.Client;
import org.veo.core.entity.Element;
import org.veo.core.entity.Key;
import org.veo.core.entity.Process;
import org.veo.core.entity.decision.DecisionResult;
import org.veo.core.entity.exception.NotFoundException;
import org.veo.core.repository.DomainRepository;
import org.veo.core.repository.ProcessRepository;
import org.veo.core.repository.RepositoryProvider;
import org.veo.core.usecase.TransactionalUseCase;
import org.veo.core.usecase.UseCase;

import lombok.RequiredArgsConstructor;
import lombok.Value;

/**
 * Evaluates a decision for a transient element and returns the decision result.
 * Does not persist any changes. This must NOT run in a transaction, so JPA does
 * not automatically persist anything. Therefore, it does not implement
 * {@link TransactionalUseCase}
 */
@RequiredArgsConstructor
public class EvaluateDecisionUseCase
        implements UseCase<EvaluateDecisionUseCase.InputData, EvaluateDecisionUseCase.OutputData> {
    private final DomainRepository domainRepository;
    private final RepositoryProvider repositoryProvider;

    @Override
    @Transactional(NEVER)
    public OutputData execute(InputData input) {
        var domain = domainRepository.findById(input.getDomainId(), input.getAuthenticatedClient()
                                                                         .getId())
                                     .orElseThrow(() -> new NotFoundException("Domain {} not found",
                                             input.getDomainId()
                                                  .uuidValue()));
        var decision = domain.getDecision(input.decisionKey)
                             .orElseThrow(() -> new NotFoundException(
                                     "Decision {} not found in domain {}", input.decisionKey,
                                     input.domainId));

        // FIXME VEO-209 support risk values on all risk affected types
        if (input.element.getId() != null && input.element instanceof Process) {
            loadRisks((Process) input.element);
        }

        return new OutputData(decision.evaluate(input.element, domain));
    }

    /** Load persisted risks and add them to element */
    private void loadRisks(Process element) {
        var repo = repositoryProvider.getRepositoryFor(element.getModelInterface());
        if (repo instanceof ProcessRepository) {
            var riskAffectedRepo = (ProcessRepository) repo;
            var storedElement = riskAffectedRepo.findByIdWithRiskValues(element.getId())
                                                .get();
            element.setRisks(storedElement.getRisks());
        }
    }

    @Valid
    @Value
    public static class InputData implements UseCase.InputData {
        Client authenticatedClient;
        Key<UUID> domainId;
        String decisionKey;
        Element element;
    }

    @Valid
    @Value
    public static class OutputData implements UseCase.OutputData {
        DecisionResult decisionResult;
    }
}
