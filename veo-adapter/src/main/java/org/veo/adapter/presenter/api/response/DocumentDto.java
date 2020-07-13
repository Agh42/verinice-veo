/*******************************************************************************
 * Copyright (c) 2019 Urs Zeidler.
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
package org.veo.adapter.presenter.api.response;

import javax.validation.Valid;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.veo.adapter.presenter.api.response.transformer.DtoEntityToTargetContext;
import org.veo.adapter.presenter.api.response.transformer.DtoEntityToTargetTransformer;
import org.veo.adapter.presenter.api.response.transformer.DtoTargetToEntityContext;
import org.veo.adapter.presenter.api.response.transformer.DtoTargetToEntityTransformer;
import org.veo.core.entity.Document;

/**
 * Transfer object for complete Documents.
 *
 * Contains all information of the Document.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(onlyExplicitlyIncluded = true, callSuper = true)
public class DocumentDto extends EntityLayerSupertypeDto {

    public static DocumentDto from(@Valid Document document, DtoEntityToTargetContext tcontext) {
        return DtoEntityToTargetTransformer.transformDocument2Dto(tcontext, document);
    }

    public Document toDocument(DtoTargetToEntityContext tcontext) {
        return DtoTargetToEntityTransformer.transformDto2Document(tcontext, this);
    }
}
