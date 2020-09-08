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

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.veo.adapter.presenter.api.common.ModelObjectReference;
import org.veo.adapter.presenter.api.dto.AbstractAssetDto;
import org.veo.adapter.presenter.api.dto.AbstractClientDto;
import org.veo.adapter.presenter.api.dto.AbstractControlDto;
import org.veo.adapter.presenter.api.dto.AbstractCustomLinkDto;
import org.veo.adapter.presenter.api.dto.AbstractCustomPropertiesDto;
import org.veo.adapter.presenter.api.dto.AbstractDocumentDto;
import org.veo.adapter.presenter.api.dto.AbstractDomainDto;
import org.veo.adapter.presenter.api.dto.AbstractPersonDto;
import org.veo.adapter.presenter.api.dto.AbstractProcessDto;
import org.veo.adapter.presenter.api.dto.AbstractUnitDto;
import org.veo.adapter.presenter.api.dto.EntityLayerSupertypeDto;
import org.veo.adapter.presenter.api.dto.EntityLayerSupertypeGroupDto;
import org.veo.adapter.presenter.api.dto.NameAbleDto;
import org.veo.adapter.presenter.api.dto.VersionedDto;
import org.veo.core.entity.Asset;
import org.veo.core.entity.Client;
import org.veo.core.entity.Control;
import org.veo.core.entity.CustomLink;
import org.veo.core.entity.CustomProperties;
import org.veo.core.entity.Document;
import org.veo.core.entity.Domain;
import org.veo.core.entity.EntityLayerSupertype;
import org.veo.core.entity.Key;
import org.veo.core.entity.ModelObject;
import org.veo.core.entity.NameAble;
import org.veo.core.entity.Person;
import org.veo.core.entity.Process;
import org.veo.core.entity.Unit;
import org.veo.core.entity.Versioned;
import org.veo.core.entity.groups.AssetGroup;
import org.veo.core.entity.groups.ControlGroup;
import org.veo.core.entity.groups.DocumentGroup;
import org.veo.core.entity.groups.PersonGroup;
import org.veo.core.entity.groups.ProcessGroup;
import org.veo.core.entity.transform.ClassKey;

/**
 * A collection of transform functions to transform entities to Dto back and
 * forth.
 */
public final class DtoToEntityTransformer {

    // PersonDto->Person
    public static <TProps extends AbstractCustomPropertiesDto, TLink extends AbstractCustomLinkDto> Person transformDto2Person(
            DtoToEntityContext tcontext, AbstractPersonDto<TProps, TLink> source, Key<UUID> key) {
        ClassKey<Key<UUID>> classKey = new ClassKey<>(Person.class, key);
        Map<ClassKey<Key<UUID>>, ? super ModelObject> context = tcontext.getContext();

        Person target = (Person) context.get(classKey);
        if (target != null) {
            return target;
        }

        target = tcontext.getFactory()
                         .createPerson(key, source.getName(), null);
        target.setId(key);
        mapVersionedProperties(source, target, key);
        mapNameAbleProperties(source, target);
        context.put(classKey, target);
        target.setDomains(convertSet(source.getDomains(),
                                     e -> ModelObjectReference.mapToEntity(context, e)));
        target.setLinks(mapLinks(tcontext, target, source));
        target.setCustomAspects(mapCustomAspects(source, tcontext));
        if (source.getOwner() != null) {
            target.setOwner(ModelObjectReference.mapToEntity(context, source.getOwner()));
        }

        return target;
    }

    public static <TProps extends AbstractCustomPropertiesDto, TLink extends AbstractCustomLinkDto> PersonGroup transformDto2PersonGroup(
            DtoToEntityContext tcontext, EntityLayerSupertypeGroupDto<Person, TProps, TLink> source,
            Key<UUID> key) {
        ClassKey<Key<UUID>> classKey = new ClassKey<>(PersonGroup.class, key);
        Map<ClassKey<Key<UUID>>, ? super ModelObject> context = tcontext.getContext();
        PersonGroup target = (PersonGroup) context.get(classKey);
        if (target != null) {
            return target;
        }

        target = tcontext.getFactory()
                         .createPersonGroup();
        target.setId(key);
        mapVersionedProperties(source, target, key);
        mapNameAbleProperties(source, target);
        context.put(classKey, target);
        target.setDomains(convertSet(source.getDomains(),
                                     e -> ModelObjectReference.mapToEntity(context, e)));
        target.setLinks(mapLinks(tcontext, target, source));
        target.setCustomAspects(mapCustomAspects(source, tcontext));
        if (source.getOwner() != null) {
            target.setOwner(ModelObjectReference.mapToEntity(context, source.getOwner()));
        }
        target.setMembers(source.getMembers()
                                .stream()
                                .map(e -> ModelObjectReference.mapToEntity(context, e))
                                .collect(Collectors.toSet()));

        return target;
    }

    // AssetDto->Asset
    public static <TProps extends AbstractCustomPropertiesDto, TLink extends AbstractCustomLinkDto> Asset transformDto2Asset(
            DtoToEntityContext tcontext, AbstractAssetDto<TProps, TLink> source, Key<UUID> key) {
        ClassKey<Key<UUID>> classKey = new ClassKey<>(Asset.class, key);
        Map<ClassKey<Key<UUID>>, ? super ModelObject> context = tcontext.getContext();

        Asset target = (Asset) context.get(classKey);
        if (target != null) {
            return target;
        }

        target = tcontext.getFactory()
                         .createAsset(key, source.getName(), null);
        target.setId(key);
        mapVersionedProperties(source, target, key);
        mapNameAbleProperties(source, target);
        context.put(classKey, target);
        target.setDomains(convertSet(source.getDomains(),
                                     e -> ModelObjectReference.mapToEntity(context, e)));
        target.setLinks(mapLinks(tcontext, target, source));
        target.setCustomAspects(mapCustomAspects(source, tcontext));
        if (source.getOwner() != null) {
            target.setOwner(ModelObjectReference.mapToEntity(context, source.getOwner()));
        }

        return target;
    }

    public static <TProps extends AbstractCustomPropertiesDto, TLink extends AbstractCustomLinkDto> AssetGroup transformDto2AssetGroup(
            DtoToEntityContext tcontext, EntityLayerSupertypeGroupDto<Asset, TProps, TLink> source,
            Key<UUID> key) {
        ClassKey<Key<UUID>> classKey = new ClassKey<>(AssetGroup.class, key);
        Map<ClassKey<Key<UUID>>, ? super ModelObject> context = tcontext.getContext();
        AssetGroup target = (AssetGroup) context.get(classKey);
        if (target != null) {
            return target;
        }

        target = tcontext.getFactory()
                         .createAssetGroup();
        target.setId(key);
        mapVersionedProperties(source, target, key);
        mapNameAbleProperties(source, target);
        context.put(classKey, target);
        target.setDomains(convertSet(source.getDomains(),
                                     e -> ModelObjectReference.mapToEntity(context, e)));
        target.setLinks(mapLinks(tcontext, target, source));
        target.setCustomAspects(mapCustomAspects(source, tcontext));
        if (source.getOwner() != null) {
            target.setOwner(ModelObjectReference.mapToEntity(context, source.getOwner()));
        }
        target.setMembers(source.getMembers()
                                .stream()
                                .map(e -> ModelObjectReference.mapToEntity(context, e))
                                .collect(Collectors.toSet()));

        return target;
    }

    // ProcessDto->Process
    public static <TProps extends AbstractCustomPropertiesDto, TLink extends AbstractCustomLinkDto> Process transformDto2Process(
            DtoToEntityContext tcontext, AbstractProcessDto<TProps, TLink> source, Key<UUID> key) {
        ClassKey<Key<UUID>> classKey = new ClassKey<>(Process.class, key);
        Map<ClassKey<Key<UUID>>, ? super ModelObject> context = tcontext.getContext();

        Process target = (Process) context.get(classKey);
        if (target != null) {
            return target;
        }

        target = tcontext.getFactory()
                         .createProcess(key, source.getName(), null);
        target.setId(key);
        mapVersionedProperties(source, target, key);
        mapNameAbleProperties(source, target);
        context.put(classKey, target);
        target.setDomains(convertSet(source.getDomains(),
                                     e -> ModelObjectReference.mapToEntity(context, e)));
        target.setLinks(mapLinks(tcontext, target, source));
        target.setCustomAspects(mapCustomAspects(source, tcontext));
        if (source.getOwner() != null) {
            target.setOwner(ModelObjectReference.mapToEntity(context, source.getOwner()));
        }

        return target;
    }

    public static <TProps extends AbstractCustomPropertiesDto, TLink extends AbstractCustomLinkDto> ProcessGroup transformDto2ProcessGroup(
            DtoToEntityContext tcontext,
            EntityLayerSupertypeGroupDto<Process, TProps, TLink> source, Key<UUID> key) {
        ClassKey<Key<UUID>> classKey = new ClassKey<>(EntityLayerSupertypeGroupDto.class, key);
        Map<ClassKey<Key<UUID>>, ? super ModelObject> context = tcontext.getContext();
        ProcessGroup target = (ProcessGroup) context.get(classKey);
        if (target != null) {
            return target;
        }

        target = tcontext.getFactory()
                         .createProcessGroup();
        target.setId(key);
        mapVersionedProperties(source, target, key);
        mapNameAbleProperties(source, target);
        context.put(classKey, target);
        target.setDomains(convertSet(source.getDomains(),
                                     e -> ModelObjectReference.mapToEntity(context, e)));
        target.setLinks(mapLinks(tcontext, target, source));
        target.setCustomAspects(mapCustomAspects(source, tcontext));
        if (source.getOwner() != null) {
            target.setOwner(ModelObjectReference.mapToEntity(context, source.getOwner()));
        }
        target.setMembers(source.getMembers()
                                .stream()
                                .map(e -> ModelObjectReference.mapToEntity(context, e))
                                .collect(Collectors.toSet()));

        return target;
    }

    // DocumentDto->Document
    public static <TProps extends AbstractCustomPropertiesDto, TLink extends AbstractCustomLinkDto> Document transformDto2Document(
            DtoToEntityContext tcontext, AbstractDocumentDto<TProps, TLink> source, Key<UUID> key) {
        ClassKey<Key<UUID>> classKey = new ClassKey<>(Document.class, key);
        Map<ClassKey<Key<UUID>>, ? super ModelObject> context = tcontext.getContext();

        Document target = (Document) context.get(classKey);
        if (target != null) {
            return target;
        }

        target = tcontext.getFactory()
                         .createDocument(key, source.getName(), null);
        target.setId(key);
        mapVersionedProperties(source, target, key);
        mapNameAbleProperties(source, target);
        context.put(classKey, target);
        target.setDomains(convertSet(source.getDomains(),
                                     e -> ModelObjectReference.mapToEntity(context, e)));
        target.setLinks(mapLinks(tcontext, target, source));
        target.setCustomAspects(mapCustomAspects(source, tcontext));
        if (source.getOwner() != null) {
            target.setOwner(ModelObjectReference.mapToEntity(context, source.getOwner()));
        }

        return target;
    }

    public static <TProps extends AbstractCustomPropertiesDto, TLink extends AbstractCustomLinkDto> DocumentGroup transformDto2DocumentGroup(
            DtoToEntityContext tcontext,
            EntityLayerSupertypeGroupDto<Document, TProps, TLink> source, Key<UUID> key) {
        ClassKey<Key<UUID>> classKey = new ClassKey<>(DocumentGroup.class, key);
        Map<ClassKey<Key<UUID>>, ? super ModelObject> context = tcontext.getContext();
        DocumentGroup target = (DocumentGroup) context.get(classKey);
        if (target != null) {
            return target;
        }

        target = tcontext.getFactory()
                         .createDocumentGroup();
        target.setId(key);
        mapVersionedProperties(source, target, key);
        mapNameAbleProperties(source, target);
        context.put(classKey, target);
        target.setDomains(convertSet(source.getDomains(),
                                     e1 -> ModelObjectReference.mapToEntity(context, e1)));
        target.setLinks(mapLinks(tcontext, target, source));
        target.setCustomAspects(mapCustomAspects(source, tcontext));
        if (source.getOwner() != null) {
            target.setOwner(ModelObjectReference.mapToEntity(context, source.getOwner()));
        }
        target.setMembers(source.getMembers()
                                .stream()
                                .map(e -> ModelObjectReference.mapToEntity(context, e))
                                .collect(Collectors.toSet()));

        return target;
    }

    // ControlDto->Control
    public static <TProps extends AbstractCustomPropertiesDto, TLink extends AbstractCustomLinkDto> Control transformDto2Control(
            DtoToEntityContext tcontext, AbstractControlDto<TProps, TLink> source, Key<UUID> key) {
        ClassKey<Key<UUID>> classKey = new ClassKey<>(Control.class, key);
        Map<ClassKey<Key<UUID>>, ? super ModelObject> context = tcontext.getContext();

        Control target = (Control) context.get(classKey);
        if (target != null) {
            return target;
        }

        target = tcontext.getFactory()
                         .createControl(key, source.getName(), null);
        target.setId(key);
        mapVersionedProperties(source, target, key);
        mapNameAbleProperties(source, target);
        context.put(classKey, target);
        target.setDomains(convertSet(source.getDomains(),
                                     e -> ModelObjectReference.mapToEntity(context, e)));
        target.setLinks(mapLinks(tcontext, target, source));
        target.setCustomAspects(mapCustomAspects(source, tcontext));
        if (source.getOwner() != null) {
            target.setOwner(ModelObjectReference.mapToEntity(context, source.getOwner()));
        }

        return target;
    }

    public static <TProps extends AbstractCustomPropertiesDto, TLink extends AbstractCustomLinkDto> ControlGroup transformDto2ControlGroup(
            DtoToEntityContext tcontext,
            EntityLayerSupertypeGroupDto<Control, TProps, TLink> source, Key<UUID> key) {
        ClassKey<Key<UUID>> classKey = new ClassKey<>(ControlGroup.class, key);
        Map<ClassKey<Key<UUID>>, ? super ModelObject> context = tcontext.getContext();
        ControlGroup target = (ControlGroup) context.get(classKey);
        if (target != null) {
            return target;
        }

        target = tcontext.getFactory()
                         .createControlGroup();
        target.setId(key);
        mapVersionedProperties(source, target, key);
        mapNameAbleProperties(source, target);
        context.put(classKey, target);
        target.setDomains(convertSet(source.getDomains(),
                                     e -> ModelObjectReference.mapToEntity(context, e)));
        target.setLinks(mapLinks(tcontext, target, source));
        target.setCustomAspects(mapCustomAspects(source, tcontext));
        if (source.getOwner() != null) {
            target.setOwner(ModelObjectReference.mapToEntity(context, source.getOwner()));
        }
        target.setMembers(source.getMembers()
                                .stream()
                                .map(e -> ModelObjectReference.mapToEntity(context, e))
                                .collect(Collectors.toSet()));

        return target;
    }

    // ClientDto->Client
    public static Client transformDto2Client(DtoToEntityContext tcontext, AbstractClientDto source,
            Key<UUID> key) {
        ClassKey<Key<UUID>> classKey = new ClassKey<>(Client.class, key);
        Map<ClassKey<Key<UUID>>, ? super ModelObject> context = tcontext.getContext();

        Client target = (Client) context.get(classKey);
        if (target != null) {
            return target;
        }

        target = tcontext.getFactory()
                         .createClient(key, source.getName());
        target.setId(key);
        mapVersionedProperties(source, target, key);
        target.setName(source.getName());
        context.put(classKey, target);
        target.setDomains(convertSet(source.getDomains(), e -> e.toEntity(tcontext)));

        return target;
    }

    // DomainDto->Domain
    public static Domain transformDto2Domain(DtoToEntityContext tcontext, AbstractDomainDto source,
            Key<UUID> key) {
        ClassKey<Key<UUID>> classKey = new ClassKey<>(Domain.class, key);
        Map<ClassKey<Key<UUID>>, ? super ModelObject> context = tcontext.getContext();

        Domain target = (Domain) context.get(classKey);
        if (target != null) {
            return target;
        }

        target = tcontext.getFactory()
                         .createDomain(key, source.getName());
        target.setId(key);
        mapVersionedProperties(source, target, key);
        mapNameAbleProperties(source, target);
        target.setActive(source.isActive());
        context.put(classKey, target);

        return target;
    }

    // UnitDto->Unit
    public static Unit transformDto2Unit(DtoToEntityContext tcontext, AbstractUnitDto source,
            Key<UUID> key) {
        ClassKey<Key<UUID>> classKey = new ClassKey<>(Unit.class, key);
        Map<ClassKey<Key<UUID>>, ? super ModelObject> context = tcontext.getContext();

        Unit target = (Unit) context.get(classKey);
        if (target != null) {
            return target;
        }

        target = tcontext.getFactory()
                         .createUnit(key, source.getName(), null);
        target.setId(key);
        mapVersionedProperties(source, target, key);
        mapNameAbleProperties(source, target);
        context.put(classKey, target);

        target.setDomains(convertSet(source.getDomains(),
                                     e -> ModelObjectReference.mapToEntity(context, e)));
        if (source.getClient() != null) {
            target.setClient(ModelObjectReference.mapToEntity(context, source.getClient()));
        }
        if (source.getParent() != null) {
            target.setParent(ModelObjectReference.mapToEntity(context, source.getParent()));
        }

        return target;
    }

    // CustomLinkDto->CustomLink
    public static CustomLink transformDto2CustomLink(DtoToEntityContext tcontext,
            AbstractCustomLinkDto source) {
        Map<ClassKey<Key<UUID>>, ? super ModelObject> context = tcontext.getContext();

        EntityLayerSupertype linkTarget = null;
        if (source.getTarget() != null) {
            linkTarget = ModelObjectReference.mapToEntity(context, source.getTarget());
        }

        var target = tcontext.getFactory()
                             .createCustomLink(source.getName(), linkTarget, null);

        target.setType(source.getType());
        target.setApplicableTo(source.getApplicableTo());
        mapNameAbleProperties(source, target);
        getPropertyTransformer().applyDtoPropertiesToEntity(source.getAttributes(), target);

        return target;

    }

    // CustomPropertiesDto->CustomProperties
    public static CustomProperties transformDto2CustomProperties(DtoToEntityContext tcontext,
            AbstractCustomPropertiesDto source) {
        var target = tcontext.getFactory()
                             .createCustomProperties(source.getType());
        target.setApplicableTo(source.getApplicableTo());

        getPropertyTransformer().applyDtoPropertiesToEntity(source.getAttributes(), target);
        return target;

    }

    private static <TProps extends AbstractCustomPropertiesDto, TLink extends AbstractCustomLinkDto> Set<CustomLink> mapLinks(
            DtoToEntityContext context, EntityLayerSupertype entity,
            EntityLayerSupertypeDto<TProps, TLink> dto) {
        return dto.getLinks()
                  .entrySet()
                  .stream()
                  .flatMap(e -> e.getValue()
                                 .stream())
                  .map(linkDto -> {
                      CustomLink link = linkDto.toEntity(context);
                      link.setSource(entity);
                      return link;
                  })
                  .collect(Collectors.toSet());
    }

    private static void mapVersionedProperties(VersionedDto source, Versioned target,
            Key<UUID> key) {
        // target.setValidFrom(Instant.parse(source.getValidFrom()));
    }

    private static void mapNameAbleProperties(NameAbleDto source, NameAble target) {
        target.setName(source.getName());
        target.setAbbreviation(source.getAbbreviation());
        target.setDescription(source.getDescription());
    }

    private static <TIn, TOut extends ModelObject> Set<TOut> convertSet(Set<TIn> source,
            Function<TIn, TOut> mapper) {
        if (mapper != null) {
            return source.stream()
                         .map(mapper)
                         .collect(Collectors.toSet());
        }
        return new HashSet<>();
    }

    private static <TProps extends AbstractCustomPropertiesDto, TLink extends AbstractCustomLinkDto> Set<CustomProperties> mapCustomAspects(
            EntityLayerSupertypeDto<TProps, TLink> dto, DtoToEntityContext tcontext) {
        return dto.getCustomAspects()
                  .entrySet()
                  .stream()
                  .map(Entry<String, TProps>::getValue)
                  .map(customAspectDto -> customAspectDto.toEntity(tcontext))
                  .collect(Collectors.toSet());
    }

    private static PropertyTransformer getPropertyTransformer() {
        return new PropertyTransformer();
    }

    private DtoToEntityTransformer() {
    }
}
