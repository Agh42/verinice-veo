/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2019  Urs Zeidler.
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
package org.veo.adapter.presenter.api.response.transformer;

import static java.util.stream.Collectors.toSet;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.veo.adapter.IdRefResolver;
import org.veo.adapter.presenter.api.common.IdRef;
import org.veo.adapter.presenter.api.dto.AbstractAssetDto;
import org.veo.adapter.presenter.api.dto.AbstractCatalogDto;
import org.veo.adapter.presenter.api.dto.AbstractCatalogItemDto;
import org.veo.adapter.presenter.api.dto.AbstractControlDto;
import org.veo.adapter.presenter.api.dto.AbstractDocumentDto;
import org.veo.adapter.presenter.api.dto.AbstractDomainDto;
import org.veo.adapter.presenter.api.dto.AbstractDomainTemplateDto;
import org.veo.adapter.presenter.api.dto.AbstractIncidentDto;
import org.veo.adapter.presenter.api.dto.AbstractPersonDto;
import org.veo.adapter.presenter.api.dto.AbstractProcessDto;
import org.veo.adapter.presenter.api.dto.AbstractScenarioDto;
import org.veo.adapter.presenter.api.dto.AbstractScopeDto;
import org.veo.adapter.presenter.api.dto.AbstractTailoringReferenceDto;
import org.veo.adapter.presenter.api.dto.AbstractUnitDto;
import org.veo.adapter.presenter.api.dto.CatalogableDto;
import org.veo.adapter.presenter.api.dto.CompositeEntityDto;
import org.veo.adapter.presenter.api.dto.CustomAspectDto;
import org.veo.adapter.presenter.api.dto.CustomLinkDto;
import org.veo.adapter.presenter.api.dto.EntityLayerSupertypeDto;
import org.veo.adapter.presenter.api.dto.NameableDto;
import org.veo.adapter.presenter.api.dto.VersionedDto;
import org.veo.adapter.presenter.api.dto.composite.CompositeCatalogDto;
import org.veo.adapter.presenter.api.dto.composite.CompositeCatalogItemDto;
import org.veo.adapter.presenter.api.dto.full.FullProcessDto;
import org.veo.adapter.presenter.api.dto.reference.ReferenceCatalogDto;
import org.veo.adapter.presenter.api.dto.reference.ReferenceCatalogItemDto;
import org.veo.adapter.presenter.api.response.IdentifiableDto;
import org.veo.core.entity.Asset;
import org.veo.core.entity.Catalog;
import org.veo.core.entity.CatalogItem;
import org.veo.core.entity.Catalogable;
import org.veo.core.entity.CompositeEntity;
import org.veo.core.entity.Control;
import org.veo.core.entity.CustomAspect;
import org.veo.core.entity.CustomLink;
import org.veo.core.entity.Document;
import org.veo.core.entity.Domain;
import org.veo.core.entity.DomainTemplate;
import org.veo.core.entity.EntityLayerSupertype;
import org.veo.core.entity.Identifiable;
import org.veo.core.entity.Incident;
import org.veo.core.entity.Key;
import org.veo.core.entity.Nameable;
import org.veo.core.entity.Person;
import org.veo.core.entity.Process;
import org.veo.core.entity.Scenario;
import org.veo.core.entity.Scope;
import org.veo.core.entity.TailoringReference;
import org.veo.core.entity.Unit;
import org.veo.core.entity.transform.EntityFactory;

/**
 * A collection of transform functions to transform entities to Dto back and
 * forth.
 */
public final class DtoToEntityTransformer {

    public DtoToEntityTransformer(EntityFactory entityFactory,
            EntitySchemaLoader entitySchemaLoader, SubTypeTransformer subTypeTransformer) {
        this.factory = entityFactory;
        this.entitySchemaLoader = entitySchemaLoader;
        this.subTypeTransformer = subTypeTransformer;
    }

    private final EntityFactory factory;
    private final EntitySchemaLoader entitySchemaLoader;
    private final SubTypeTransformer subTypeTransformer;

    // PersonDto->Person
    public Person transformDto2Person(AbstractPersonDto source, IdRefResolver idRefResolver) {
        var target = factory.createPerson(source.getName(), null);
        mapCompositeEntity(source, target, idRefResolver);
        return target;
    }

    // AssetDto->Asset
    public Asset transformDto2Asset(AbstractAssetDto source, IdRefResolver idRefResolver) {
        var target = factory.createAsset(source.getName(), null);
        mapCompositeEntity(source, target, idRefResolver);
        return target;
    }

    // ProcessDto->Process
    public Process transformDto2Process(AbstractProcessDto source, IdRefResolver idRefResolver) {
        var target = factory.createProcess(source.getName(), null);
        mapCompositeEntity(source, target, idRefResolver);
        if (source instanceof FullProcessDto) {
            target.setStatus(((FullProcessDto) source).getStatus());
        }
        return target;
    }

    // DocumentDto->Document
    public Document transformDto2Document(AbstractDocumentDto source, IdRefResolver idRefResolver) {
        var target = factory.createDocument(source.getName(), null);
        mapCompositeEntity(source, target, idRefResolver);
        return target;
    }

    // ControlDto->Control
    public Control transformDto2Control(AbstractControlDto source, IdRefResolver idRefResolver) {
        var target = factory.createControl(source.getName(), null);
        mapCompositeEntity(source, target, idRefResolver);
        return target;
    }

    // IncidentDto->Incident
    public Incident transformDto2Incident(AbstractIncidentDto source, IdRefResolver idRefResolver) {
        var target = factory.createIncident(source.getName(), null);
        mapCompositeEntity(source, target, idRefResolver);
        return target;
    }

    // ScenarioDto->Scenario
    public Scenario transformDto2Scenario(AbstractScenarioDto source, IdRefResolver idRefResolver) {
        var target = factory.createScenario(source.getName(), null);
        mapCompositeEntity(source, target, idRefResolver);
        return target;
    }

    public Scope transformDto2Scope(AbstractScopeDto source, IdRefResolver idRefResolver) {
        var target = factory.createScope(source.getName(), null);
        mapEntityLayerSupertype(source, target, idRefResolver);
        Set<IdRef<EntityLayerSupertype>> memberReferences = source.getMembers();
        Map<Class<EntityLayerSupertype>, Set<IdRef<EntityLayerSupertype>>> memberReferencesByType = memberReferences.stream()
                                                                                                                    .collect(Collectors.groupingBy(IdRef::getType,
                                                                                                                                                   Collectors.toSet()));
        Set<EntityLayerSupertype> members = memberReferencesByType.values()
                                                                  .stream()
                                                                  .flatMap(refs -> idRefResolver.resolve(refs)
                                                                                                .stream())
                                                                  .collect(Collectors.toSet());

        target.setMembers(members);
        return target;
    }

    // DomainDto->Domain
    public Domain transformDto2Domain(AbstractDomainDto source, Key<UUID> key) {
        var target = factory.createDomain(source.getName(), "", "", "");
        mapIdentifiableProperties(source, target);
        mapNameableProperties(source, target);
        target.setActive(true);

        return target;
    }

    public Domain transformDomainTemplateDto2Domain(AbstractDomainTemplateDto source,
            IdRefResolver idRefResolver) {
        var target = factory.createDomain(source.getName(), source.getAuthority(),
                                          source.getTemplateVersion(), source.getRevision());
        target.setActive(true);
        mapDomainTemplate(source, idRefResolver, target);
        return target;
    }

    public DomainTemplate transformDto2DomainTemplate(AbstractDomainTemplateDto source,
            IdRefResolver idRefResolver) {
        var target = factory.createDomainTemplate(source.getName(), source.getAuthority(),
                                                  source.getTemplateVersion(), source.getRevision(),
                                                  null);
        mapIdentifiableProperties(source, target);
        mapDomainTemplate(source, idRefResolver, target);

        return target;
    }

    public Catalog transformDto2Catalog(AbstractCatalogDto source, IdRefResolver idRefResolver) {
        var target = factory.createCatalog(idRefResolver.resolve(source.getDomainTemplate()));
        mapIdentifiableProperties(source, target);
        mapNameableProperties(source, target);
        if (source instanceof ReferenceCatalogDto) {
            ReferenceCatalogDto catalogDto = (ReferenceCatalogDto) source;
            target.setCatalogItems(idRefResolver.resolve(catalogDto.getCatalogItems()));
        } else if (source instanceof CompositeCatalogDto) {
            CompositeCatalogDto catalogDto = (CompositeCatalogDto) source;
            target.setCatalogItems(convertSet(catalogDto.getCatalogItems(),
                                              ci -> transformDto2CatalogItem(ci, idRefResolver,
                                                                             target)));
        }

        return target;
    }

    public TailoringReference transformDto2TailoringReference(AbstractTailoringReferenceDto source,
            CatalogItem owner, IdRefResolver idRefResolver) {
        var target = factory.createTailoringReference(owner);
        mapIdentifiableProperties(source, target);
        if (source.getCatalogItem() != null) {
            CatalogItem resolve = idRefResolver.resolve(source.getCatalogItem());
            target.setCatalogItem(resolve);
        }
        target.setReferenceType(source.getReferenceType());
        return target;
    }

    // UnitDto->Unit
    public Unit transformDto2Unit(AbstractUnitDto source, IdRefResolver idRefResolver) {
        var target = factory.createUnit(source.getName(), null);
        mapIdentifiableProperties(source, target);
        mapNameableProperties(source, target);

        target.setDomains(idRefResolver.resolve(source.getDomains()));
        if (source.getClient() != null) {
            target.setClient(idRefResolver.resolve(source.getClient()));
        }
        if (source.getParent() != null) {
            target.setParent(idRefResolver.resolve(source.getParent()));
        }

        return target;
    }

    // CustomLinkDto->CustomLink
    public CustomLink transformDto2CustomLink(CustomLinkDto source, String type,
            EntitySchema entitySchema, IdRefResolver idRefResolver) {
        EntityLayerSupertype linkTarget = null;
        if (source.getTarget() != null) {
            linkTarget = idRefResolver.resolve(source.getTarget());
        }

        var target = factory.createCustomLink(linkTarget, null);

        target.setAttributes(source.getAttributes());
        target.setType(type);
        entitySchema.validateCustomLink(target);
        return target;

    }

    // CustomPropertiesDto->CustomProperties
    public CustomAspect transformDto2CustomProperties(EntityFactory factory, CustomAspectDto source,
            String type, EntitySchema entitySchema) {
        var target = factory.createCustomAspect();
        target.setAttributes(source.getAttributes());
        target.setType(type);
        entitySchema.validateCustomAspect(target);
        return target;
    }

    public Catalogable transformDto2Catalogable(CatalogableDto catalogableDto,
            IdRefResolver idRefResolver) {
        if (catalogableDto instanceof AbstractAssetDto) {
            return transformDto2Asset((AbstractAssetDto) catalogableDto, idRefResolver);
        } else if (catalogableDto instanceof AbstractControlDto) {
            return transformDto2Control((AbstractControlDto) catalogableDto, idRefResolver);
        } else if (catalogableDto instanceof AbstractDocumentDto) {
            return transformDto2Document((AbstractDocumentDto) catalogableDto, idRefResolver);
        } else if (catalogableDto instanceof AbstractIncidentDto) {
            return transformDto2Incident((AbstractIncidentDto) catalogableDto, idRefResolver);
        } else if (catalogableDto instanceof AbstractPersonDto) {
            return transformDto2Person((AbstractPersonDto) catalogableDto, idRefResolver);
        } else if (catalogableDto instanceof AbstractProcessDto) {
            return transformDto2Process((AbstractProcessDto) catalogableDto, idRefResolver);
        } else if (catalogableDto instanceof AbstractScenarioDto) {
            return transformDto2Scenario((AbstractScenarioDto) catalogableDto, idRefResolver);
        }
        throw new IllegalArgumentException("unkown type: " + catalogableDto.getClass()
                                                                           .getName());
    }

    private void mapDomainTemplate(AbstractDomainTemplateDto source, IdRefResolver idRefResolver,
            DomainTemplate target) {
        mapNameableProperties(source, target);
        if (source.getCatalogs() != null) {
            target.setCatalogs(source.getCatalogs()
                                     .stream()
                                     .map(c -> transformDto2Catalog(c, idRefResolver))
                                     .collect(Collectors.toSet())

            );
        }
    }

    private <T extends EntityLayerSupertype> void mapCompositeEntity(CompositeEntityDto<T> source,
            CompositeEntity<T> target, IdRefResolver idRefResolver) {
        mapEntityLayerSupertype(source, target, idRefResolver);
        target.setParts(idRefResolver.resolve(source.getParts()));
    }

    private <TDto extends EntityLayerSupertypeDto, TEntity extends EntityLayerSupertype> void mapEntityLayerSupertype(
            TDto source, TEntity target, IdRefResolver idRefResolver) {
        mapIdentifiableProperties(source, target);
        mapNameableProperties(source, target);
        target.setDomains(idRefResolver.resolve(source.getDomains()));
        subTypeTransformer.mapSubTypesToEntity(source, target);
        var entitySchema = loadEntitySchema(target.getModelType());
        target.setLinks(mapLinks(target, source, entitySchema, idRefResolver));
        target.setCustomAspects(mapCustomAspects(source, factory, entitySchema));
        if (source.getOwner() != null) {
            target.setOwnerOrContainingCatalogItem(idRefResolver.resolve(source.getOwner()));
        }
    }

    private void mapIdentifiableProperties(VersionedDto source, Identifiable target) {
        if (source instanceof IdentifiableDto) {
            target.setId(Key.uuidFrom(((IdentifiableDto) source).getId()));
        }
    }

    private Set<CustomLink> mapLinks(EntityLayerSupertype entity, EntityLayerSupertypeDto dto,
            EntitySchema entitySchema, IdRefResolver idRefResolver) {
        return dto.getLinks()
                  .entrySet()
                  .stream()
                  .flatMap(entry -> entry.getValue()
                                         .stream()
                                         .map(linktDto -> {
                                             var customLink = transformDto2CustomLink(linktDto,
                                                                                      entry.getKey(),
                                                                                      entitySchema,
                                                                                      idRefResolver);
                                             customLink.setSource(entity);
                                             return customLink;
                                         }))
                  .collect(toSet());
    }

    private static void mapNameableProperties(NameableDto source, Nameable target) {
        target.setName(source.getName());
        target.setAbbreviation(source.getAbbreviation());
        target.setDescription(source.getDescription());
    }

    private static <TIn, TOut extends Identifiable> Set<TOut> convertSet(Set<TIn> source,
            Function<TIn, TOut> mapper) {
        if (mapper != null) {
            return source.stream()
                         .map(mapper)
                         .collect(Collectors.toSet());
        }
        return new HashSet<>();
    }

    private Set<CustomAspect> mapCustomAspects(EntityLayerSupertypeDto dto, EntityFactory factory,
            EntitySchema entitySchema) {
        return dto.getCustomAspects()
                  .entrySet()
                  .stream()
                  .map(entry -> transformDto2CustomProperties(factory, entry.getValue(),
                                                              entry.getKey(), entitySchema))
                  .collect(Collectors.toSet());
    }

    private EntitySchema loadEntitySchema(String entityType) {
        return entitySchemaLoader.load(entityType);
    }

    public CatalogItem transformDto2CatalogItem(AbstractCatalogItemDto source,
            IdRefResolver idRefResolver, Catalog catalog) {
        var target = factory.createCatalogItem(catalog, catalogItem -> {
            if (source instanceof CompositeCatalogItemDto) {
                CompositeCatalogItemDto catalogitem = (CompositeCatalogItemDto) source;
                CatalogableDto catalogableDto = catalogitem.getElement();
                if (catalogableDto instanceof AbstractAssetDto) {
                    return transformDto2Asset((AbstractAssetDto) catalogableDto, idRefResolver);
                } else if (catalogableDto instanceof AbstractControlDto) {
                    return transformDto2Control((AbstractControlDto) catalogableDto, idRefResolver);
                } else if (catalogableDto instanceof AbstractDocumentDto) {
                    return transformDto2Document((AbstractDocumentDto) catalogableDto,
                                                 idRefResolver);
                } else if (catalogableDto instanceof AbstractIncidentDto) {
                    return transformDto2Incident((AbstractIncidentDto) catalogableDto,
                                                 idRefResolver);
                } else if (catalogableDto instanceof AbstractPersonDto) {
                    return transformDto2Person((AbstractPersonDto) catalogableDto, idRefResolver);
                } else if (catalogableDto instanceof AbstractProcessDto) {
                    return transformDto2Process((AbstractProcessDto) catalogableDto, idRefResolver);
                } else if (catalogableDto instanceof AbstractScenarioDto) {
                    return transformDto2Scenario((AbstractScenarioDto) catalogableDto,
                                                 idRefResolver);
                }
            } else if (source instanceof ReferenceCatalogItemDto) {
                ReferenceCatalogItemDto catalogitem = (ReferenceCatalogItemDto) source;
                return idRefResolver.resolve(catalogitem.getElement());
            }
            throw new IllegalArgumentException("Cannot handle entity type " + source.getClass()
                                                                                    .getName());
        });
        mapIdentifiableProperties(source, target);
        target.setNamespace(source.getNamespace());

        target.getTailoringReferences()
              .clear();
        target.getTailoringReferences()
              .addAll(convertSet(source.getTailoringReferences(),
                                 tr -> transformDto2TailoringReference(tr, target, idRefResolver)));
        return target;
    }

}
