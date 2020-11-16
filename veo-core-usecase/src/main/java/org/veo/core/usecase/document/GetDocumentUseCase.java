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
package org.veo.core.usecase.document;

import java.util.UUID;

import javax.validation.Valid;

import org.veo.core.entity.Client;
import org.veo.core.entity.Document;
import org.veo.core.entity.Key;
import org.veo.core.entity.exception.NotFoundException;
import org.veo.core.usecase.UseCase;
import org.veo.core.usecase.repository.DocumentRepository;

import lombok.Value;

/**
 * Reinstantiate a persisted document object.
 */
public class GetDocumentUseCase
        extends UseCase<GetDocumentUseCase.InputData, GetDocumentUseCase.OutputData> {

    private final DocumentRepository repository;

    public GetDocumentUseCase(DocumentRepository repository) {
        this.repository = repository;
    }

    public OutputData execute(InputData input) {
        Document document = repository.findById(input.getId())
                                      .orElseThrow(() -> new NotFoundException(input.getId()
                                                                                    .uuidValue()));
        document.checkSameClient(input.authenticatedClient);
        return new OutputData(document);
    }

    @Valid
    @Value
    public static class InputData implements UseCase.InputData {
        Key<UUID> id;
        Client authenticatedClient;
    }

    @Valid
    @Value
    public static class OutputData implements UseCase.OutputData {
        @Valid
        Document document;
    }
}