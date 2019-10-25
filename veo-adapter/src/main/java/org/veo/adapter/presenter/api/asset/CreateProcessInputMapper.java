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
package org.veo.adapter.presenter.api.asset;

import org.modelmapper.ModelMapper;

import org.veo.adapter.presenter.api.dto.ProcessDto;
import org.veo.core.usecase.process.CreateProcessUseCase;

/**
 * Map between the request DTO received from a client and the input expected by
 * the data source. (This is not needed for simple input data.)
 *
 */
public final class CreateProcessInputMapper {

    public static CreateProcessUseCase.InputData map(ProcessDto dto) {
        ModelMapper mapper = new ModelMapper();
        return mapper.map(dto, CreateProcessUseCase.InputData.class);
    }
}
