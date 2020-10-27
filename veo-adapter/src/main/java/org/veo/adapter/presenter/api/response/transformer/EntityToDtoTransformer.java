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
package org.veo.adapter.presenter.api.response.transformer;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.veo.adapter.presenter.api.common.ModelObjectReference;
import org.veo.adapter.presenter.api.common.ReferenceAssembler;
import org.veo.adapter.presenter.api.dto.CustomLinkDto;
import org.veo.adapter.presenter.api.dto.CustomPropertiesDto;
import org.veo.adapter.presenter.api.dto.EntityLayerSupertypeDto;
import org.veo.adapter.presenter.api.dto.NameableDto;
import org.veo.adapter.presenter.api.dto.VersionedDto;
import org.veo.adapter.presenter.api.dto.full.FullAssetDto;
import org.veo.adapter.presenter.api.dto.full.FullAssetGroupDto;
import org.veo.adapter.presenter.api.dto.full.FullClientDto;
import org.veo.adapter.presenter.api.dto.full.FullControlDto;
import org.veo.adapter.presenter.api.dto.full.FullControlGroupDto;
import org.veo.adapter.presenter.api.dto.full.FullDocumentDto;
import org.veo.adapter.presenter.api.dto.full.FullDocumentGroupDto;
import org.veo.adapter.presenter.api.dto.full.FullDomainDto;
import org.veo.adapter.presenter.api.dto.full.FullEntityLayerSupertypeGroupDto;
import org.veo.adapter.presenter.api.dto.full.FullPersonDto;
import org.veo.adapter.presenter.api.dto.full.FullPersonGroupDto;
import org.veo.adapter.presenter.api.dto.full.FullProcessDto;
import org.veo.adapter.presenter.api.dto.full.FullProcessGroupDto;
import org.veo.adapter.presenter.api.dto.full.FullUnitDto;
import org.veo.adapter.presenter.api.response.IdentifiableDto;
import org.veo.core.entity.Asset;
import org.veo.core.entity.Client;
import org.veo.core.entity.Control;
import org.veo.core.entity.CustomLink;
import org.veo.core.entity.CustomProperties;
import org.veo.core.entity.Document;
import org.veo.core.entity.Domain;
import org.veo.core.entity.EntityLayerSupertype;
import org.veo.core.entity.ModelGroup;
import org.veo.core.entity.ModelObject;
import org.veo.core.entity.Nameable;
import org.veo.core.entity.Person;
import org.veo.core.entity.Process;
import org.veo.core.entity.Unit;
import org.veo.core.entity.Versioned;
import org.veo.core.entity.groups.AssetGroup;
import org.veo.core.entity.groups.ControlGroup;
import org.veo.core.entity.groups.DocumentGroup;
import org.veo.core.entity.groups.PersonGroup;
import org.veo.core.entity.groups.ProcessGroup;

/**
 * A collection of transform functions to transform entities to Dto back and
 * forth.
 */
public final class EntityToDtoTransformer {

    public static EntityLayerSupertypeDto transform2Dto(ReferenceAssembler referenceAssembler,
            EntityLayerSupertype source) {
        if (source instanceof ModelGroup) {
            return transformGroup2Dto(referenceAssembler, (ModelGroup<?>) source);
        }
        if (source instanceof Person) {
            return transformPerson2Dto(referenceAssembler, (Person) source);
        }
        if (source instanceof Asset) {
            return transformAsset2Dto(referenceAssembler, (Asset) source);
        }
        if (source instanceof Process) {
            return transformProcess2Dto(referenceAssembler, (Process) source);
        }
        if (source instanceof Document) {
            return transformDocument2Dto(referenceAssembler, (Document) source);
        }
        if (source instanceof Control) {
            return transformControl2Dto(referenceAssembler, (Control) source);
        }
        throw new IllegalArgumentException("No transform method defined for " + source.getClass()
                                                                                      .getSimpleName());
    }

    public static FullEntityLayerSupertypeGroupDto<?> transformGroup2Dto(
            ReferenceAssembler referenceAssembler, ModelGroup<?> source) {
        if (source instanceof PersonGroup) {
            return transformPersonGroup2Dto(referenceAssembler, (PersonGroup) source);
        }
        if (source instanceof AssetGroup) {
            return transformAssetGroup2Dto(referenceAssembler, (AssetGroup) source);
        }
        if (source instanceof ProcessGroup) {
            return transformProcessGroup2Dto(referenceAssembler, (ProcessGroup) source);
        }
        if (source instanceof DocumentGroup) {
            return transformDocumentGroup2Dto(referenceAssembler, (DocumentGroup) source);
        }
        if (source instanceof ControlGroup) {
            return transformControlGroup2Dto(referenceAssembler, (ControlGroup) source);
        }
        throw new IllegalArgumentException("No transform method defined for " + source.getClass()
                                                                                      .getSimpleName());
    }

    public static FullPersonGroupDto transformPersonGroup2Dto(ReferenceAssembler referenceAssembler,
            PersonGroup source) {
        FullPersonGroupDto target = new FullPersonGroupDto();
        mapModelGroup(referenceAssembler, source, target);
        return target;
    }

    // Person ->
    // PersonDto
    public static FullPersonDto transformPerson2Dto(ReferenceAssembler referenceAssembler,
            Person source) {
        if (source instanceof ModelGroup<?>) {
            return transformPersonGroup2Dto(referenceAssembler, (PersonGroup) source);
        }
        FullPersonDto target = new FullPersonDto();
        mapEntityLayerSupertype(referenceAssembler, source, target);
        return target;
    }

    public static FullAssetGroupDto transformAssetGroup2Dto(ReferenceAssembler referenceAssembler,
            AssetGroup source) {
        FullAssetGroupDto target = new FullAssetGroupDto();
        mapModelGroup(referenceAssembler, source, target);
        return target;
    }

    // Asset -> AssetDto
    public static FullAssetDto transformAsset2Dto(ReferenceAssembler referenceAssembler,
            Asset source) {
        FullAssetDto target = new FullAssetDto();
        mapEntityLayerSupertype(referenceAssembler, source, target);
        return target;
    }

    public static FullProcessGroupDto transformProcessGroup2Dto(
            ReferenceAssembler referenceAssembler, ProcessGroup source) {
        FullProcessGroupDto target = new FullProcessGroupDto();
        mapModelGroup(referenceAssembler, source, target);
        return target;
    }

    // Process ->
    // ProcessDto
    public static FullProcessDto transformProcess2Dto(ReferenceAssembler referenceAssembler,
            Process source) {
        FullProcessDto target = new FullProcessDto();
        mapEntityLayerSupertype(referenceAssembler, source, target);
        return target;
    }

    public static FullDocumentGroupDto transformDocumentGroup2Dto(
            ReferenceAssembler referenceAssembler, DocumentGroup source) {
        FullDocumentGroupDto target = new FullDocumentGroupDto();
        mapModelGroup(referenceAssembler, source, target);
        return target;
    }

    // Document ->
    // DocumentDto
    public static FullDocumentDto transformDocument2Dto(ReferenceAssembler referenceAssembler,
            Document source) {
        FullDocumentDto target = new FullDocumentDto();
        mapEntityLayerSupertype(referenceAssembler, source, target);
        return target;
    }

    public static FullControlGroupDto transformControlGroup2Dto(
            ReferenceAssembler referenceAssembler, ControlGroup source) {
        FullControlGroupDto target = new FullControlGroupDto();
        mapModelGroup(referenceAssembler, source, target);
        return target;
    }

    // Control ->
    // ControlDto
    public static FullControlDto transformControl2Dto(ReferenceAssembler referenceAssembler,
            Control source) {
        FullControlDto target = new FullControlDto();
        mapEntityLayerSupertype(referenceAssembler, source, target);
        return target;
    }

    // Client ->
    // ClientDto
    public static FullClientDto transformClient2Dto(Client source) {
        var target = new FullClientDto();
        target.setId(source.getId()
                           .uuidValue());
        target.setVersion(source.getVersion());
        target.setName(source.getName());
        target.setDomains(convertSet(source.getDomains(),
                                     EntityToDtoTransformer::transformDomain2Dto));
        return target;
    }

    // Domain ->
    // DomainDto
    public static FullDomainDto transformDomain2Dto(Domain source) {
        var target = new FullDomainDto();
        target.setId(source.getId()
                           .uuidValue());
        target.setVersion(source.getVersion());
        mapVersionedProperties(source, target);
        mapNameableProperties(source, target);
        target.setActive(source.isActive());

        return target;
    }

    // Unit -> UnitDto
    public static FullUnitDto transformUnit2Dto(ReferenceAssembler referenceAssembler,
            Unit source) {
        var target = new FullUnitDto();
        target.setId(source.getId()
                           .uuidValue());
        target.setVersion(source.getVersion());
        target.setUnits(convertSet(source.getUnits(),
                                   u -> ModelObjectReference.from(u, referenceAssembler)));
        mapVersionedProperties(source, target);
        mapNameableProperties(source, target);

        target.setDomains(convertReferenceSet(source.getDomains(), referenceAssembler));
        if (source.getClient() != null) {
            target.setClient(ModelObjectReference.from(source.getClient(), referenceAssembler));
        }
        if (source.getParent() != null) {
            target.setParent(ModelObjectReference.from(source.getParent(), referenceAssembler));
        }

        return target;
    }

    // CustomLink ->
    // CustomLinkDto
    public static CustomLinkDto transformCustomLink2Dto(ReferenceAssembler referenceAssembler,
            CustomLink source) {
        var target = new CustomLinkDto();
        target.setApplicableTo(source.getApplicableTo());
        mapNameableProperties(source, target);

        target.setAttributes(source.getAllProperties());

        if (source.getTarget() != null) {
            target.setTarget(ModelObjectReference.from(source.getTarget(), referenceAssembler));
        }

        return target;

    }

    private static <TDto extends EntityLayerSupertypeDto & IdentifiableDto> void mapEntityLayerSupertype(
            ReferenceAssembler referenceAssembler, EntityLayerSupertype source, TDto target) {
        target.setId(source.getId()
                           .uuidValue());
        target.setVersion(source.getVersion());
        mapVersionedProperties(source, target);
        mapNameableProperties(source, target);

        target.setDomains(convertReferenceSet(source.getDomains(), referenceAssembler));
        target.setLinks(mapLinks(source.getLinks(), referenceAssembler));
        target.setCustomAspects(mapCustomAspects(source.getCustomAspects()));

        if (source.getOwner() != null) {
            target.setOwner(ModelObjectReference.from(source.getOwner(), referenceAssembler));
        }
    }

    private static <TEntity extends EntityLayerSupertype> void mapModelGroup(
            ReferenceAssembler referenceAssembler, ModelGroup<TEntity> source,
            FullEntityLayerSupertypeGroupDto<TEntity> target) {
        mapEntityLayerSupertype(referenceAssembler, source, target);
        target.setMembers(convertReferenceSet(source.getMembers(), referenceAssembler));
    }

    // CustomProperties ->
    // CustomPropertiesDto
    public static CustomPropertiesDto transformCustomProperties2Dto(CustomProperties source) {
        var target = new CustomPropertiesDto();
        target.setApplicableTo(source.getApplicableTo());

        target.setAttributes(source.getAllProperties());
        return target;
    }

    private static void mapNameableProperties(Nameable source, NameableDto target) {
        target.setName(source.getName());
        target.setAbbreviation(source.getAbbreviation());
        target.setDescription(source.getDescription());
    }

    private static void mapVersionedProperties(Versioned source, VersionedDto target) {
        target.setCreatedAt(source.getCreatedAt()
                                  .toString());
        target.setCreatedBy(source.getCreatedBy());
        target.setUpdatedAt(source.getUpdatedAt()
                                  .toString());
        target.setUpdatedBy(source.getUpdatedBy());
    }

    private static <TIn, TOut> Set<TOut> convertSet(Set<TIn> input, Function<TIn, TOut> mapper) {
        return input.stream()
                    .map(mapper)
                    .collect(Collectors.toSet());
    }

    private static <T extends ModelObject> Set<ModelObjectReference<T>> convertReferenceSet(
            Set<T> domains, ReferenceAssembler referenceAssembler) {
        return domains.stream()
                      .map(o -> ModelObjectReference.from(o, referenceAssembler))
                      .collect(Collectors.toSet());
    }

    private static Map<String, List<CustomLinkDto>> mapLinks(Set<CustomLink> links,
            ReferenceAssembler referenceAssembler) {
        return links.stream()
                    .collect(groupingBy(CustomLink::getType))
                    .entrySet()
                    .stream()
                    .collect(toMap(Map.Entry::getKey, entry -> entry.getValue()
                                                                    .stream()
                                                                    .map(link -> transformCustomLink2Dto(referenceAssembler,
                                                                                                         link))
                                                                    .collect(toList())));
    }

    private static Map<String, CustomPropertiesDto> mapCustomAspects(
            Set<CustomProperties> customAspects) {
        return customAspects.stream()
                            .collect(toMap(CustomProperties::getType,
                                           EntityToDtoTransformer::transformCustomProperties2Dto));
    }

    private EntityToDtoTransformer() {
    }
}
