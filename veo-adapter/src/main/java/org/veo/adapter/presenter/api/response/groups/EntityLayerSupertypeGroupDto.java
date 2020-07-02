/*******************************************************************************
 * Copyright (c) 2020 Urs Zeidler.
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
package org.veo.adapter.presenter.api.response.groups;

import java.util.Collection;
import java.util.Set;

import org.veo.adapter.presenter.api.common.ModelObjectReference;
import org.veo.adapter.presenter.api.response.transformer.DtoEntityToTargetContext;
import org.veo.adapter.presenter.api.response.transformer.DtoEntityToTargetTransformer;
import org.veo.core.entity.ModelObject;
import org.veo.core.entity.impl.BaseModelGroup;

/**
 * @author urszeidler
 */
public interface EntityLayerSupertypeGroupDto<T extends ModelObject> {

    public abstract Set<ModelObjectReference<T>> getMembers();

    public abstract void setMembers(Set<ModelObjectReference<T>> members);

    public abstract Collection<ModelObjectReference<? extends ModelObject>> getReferences();

    public static EntityLayerSupertypeGroupDto from(BaseModelGroup<?> group,
            DtoEntityToTargetContext tcontext) {
        return (EntityLayerSupertypeGroupDto) DtoEntityToTargetTransformer.transformEntityLayerSupertype2Dto(tcontext,
                                                                                                             group);
    }

}
