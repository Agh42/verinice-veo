/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2019  Alexander Koderman.
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
package org.veo.core.usecase.process;

import java.util.UUID;

import javax.validation.Valid;

import org.veo.core.entity.Client;
import org.veo.core.entity.Key;
import org.veo.core.entity.Process;
import org.veo.core.entity.exception.NotFoundException;
import org.veo.core.repository.ProcessRepository;
import org.veo.core.usecase.TransactionalUseCase;
import org.veo.core.usecase.UseCase;
import org.veo.core.usecase.base.GetElementUseCase;

import lombok.EqualsAndHashCode;
import lombok.Value;

/** Reinstantiate a persisted process object. */
public class GetProcessUseCase
    implements TransactionalUseCase<UseCase.IdAndClient, GetElementUseCase.OutputData<Process>> {

  private final ProcessRepository processRepository;

  public GetProcessUseCase(ProcessRepository repository) {
    processRepository = repository;
  }

  public GetElementUseCase.OutputData<Process> execute(IdAndClient input) {
    var process =
        processRepository
            .findById(input.getId(), shouldEmbedRisks(input))
            .orElseThrow(() -> new NotFoundException(input.getId(), Process.class));
    process.checkSameClient(input.getAuthenticatedClient());
    return new GetElementUseCase.OutputData<>(process);
  }

  private boolean shouldEmbedRisks(IdAndClient input) {
    if (input instanceof InputData inputData) {
      return inputData.embedRisks;
    }
    return false;
  }

  @Value
  @EqualsAndHashCode(callSuper = true)
  @Valid
  public static class InputData extends IdAndClient {
    boolean embedRisks;

    public InputData(Key<UUID> id, Client authenticatedClient, boolean embedRisks) {
      super(id, authenticatedClient);
      this.embedRisks = embedRisks;
    }
  }
}
