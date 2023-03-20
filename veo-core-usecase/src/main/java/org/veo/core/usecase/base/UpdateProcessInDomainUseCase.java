/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2023  Jonas Jordan
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
package org.veo.core.usecase.base;

import java.util.HashMap;

import org.veo.core.entity.Domain;
import org.veo.core.entity.Process;
import org.veo.core.repository.DomainRepository;
import org.veo.core.repository.ProcessRepository;
import org.veo.core.usecase.decision.Decider;

public class UpdateProcessInDomainUseCase extends UpdateElementInDomainUseCase<Process> {
  public UpdateProcessInDomainUseCase(
      DomainRepository domainRepository, ProcessRepository repo, Decider decider) {
    super(domainRepository, repo, decider);
  }

  @Override
  protected void applyChanges(Process source, Process target, Domain domain) {
    super.applyChanges(source, target, domain);
    target.setImpactValues(domain, source.getImpactValues(domain).orElse(new HashMap<>()));
  }
}
