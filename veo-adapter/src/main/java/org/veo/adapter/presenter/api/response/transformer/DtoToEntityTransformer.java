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

import static java.util.Map.copyOf;
import static java.util.stream.Collectors.toSet;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.NotImplementedException;

import org.veo.adapter.presenter.api.dto.AbstractAssetDto;
import org.veo.adapter.presenter.api.dto.AbstractAssetInDomainDto;
import org.veo.adapter.presenter.api.dto.AbstractCatalogDto;
import org.veo.adapter.presenter.api.dto.AbstractCatalogItemDto;
import org.veo.adapter.presenter.api.dto.AbstractCompositeElementInDomainDto;
import org.veo.adapter.presenter.api.dto.AbstractControlDto;
import org.veo.adapter.presenter.api.dto.AbstractControlInDomainDto;
import org.veo.adapter.presenter.api.dto.AbstractDocumentDto;
import org.veo.adapter.presenter.api.dto.AbstractDocumentInDomainDto;
import org.veo.adapter.presenter.api.dto.AbstractElementDto;
import org.veo.adapter.presenter.api.dto.AbstractElementInDomainDto;
import org.veo.adapter.presenter.api.dto.AbstractIncidentDto;
import org.veo.adapter.presenter.api.dto.AbstractIncidentInDomainDto;
import org.veo.adapter.presenter.api.dto.AbstractPersonDto;
import org.veo.adapter.presenter.api.dto.AbstractPersonInDomainDto;
import org.veo.adapter.presenter.api.dto.AbstractProcessDto;
import org.veo.adapter.presenter.api.dto.AbstractProcessInDomainDto;
import org.veo.adapter.presenter.api.dto.AbstractRiskDto;
import org.veo.adapter.presenter.api.dto.AbstractScenarioDto;
import org.veo.adapter.presenter.api.dto.AbstractScenarioInDomainDto;
import org.veo.adapter.presenter.api.dto.AbstractScopeDto;
import org.veo.adapter.presenter.api.dto.AbstractScopeInDomainDto;
import org.veo.adapter.presenter.api.dto.AbstractTailoringReferenceDto;
import org.veo.adapter.presenter.api.dto.AbstractUnitDto;
import org.veo.adapter.presenter.api.dto.CustomAspectDto;
import org.veo.adapter.presenter.api.dto.ElementTypeDefinitionDto;
import org.veo.adapter.presenter.api.dto.NameableDto;
import org.veo.adapter.presenter.api.dto.composite.CompositeCatalogDto;
import org.veo.adapter.presenter.api.dto.composite.CompositeCatalogItemDto;
import org.veo.adapter.presenter.api.dto.full.AssetRiskDto;
import org.veo.adapter.presenter.api.dto.full.ProcessRiskDto;
import org.veo.adapter.presenter.api.dto.full.ScopeRiskDto;
import org.veo.adapter.presenter.api.dto.reference.ReferenceCatalogDto;
import org.veo.adapter.presenter.api.dto.reference.ReferenceCatalogItemDto;
import org.veo.adapter.presenter.api.io.mapper.CategorizedRiskValueMapper;
import org.veo.adapter.presenter.api.response.IdentifiableDto;
import org.veo.adapter.service.domaintemplate.dto.TransformDomainTemplateDto;
import org.veo.adapter.service.domaintemplate.dto.TransformLinkTailoringReference;
import org.veo.core.entity.AbstractRisk;
import org.veo.core.entity.Asset;
import org.veo.core.entity.AssetRisk;
import org.veo.core.entity.Catalog;
import org.veo.core.entity.CatalogItem;
import org.veo.core.entity.CompositeElement;
import org.veo.core.entity.Control;
import org.veo.core.entity.CustomAspect;
import org.veo.core.entity.Document;
import org.veo.core.entity.Domain;
import org.veo.core.entity.DomainBase;
import org.veo.core.entity.DomainTemplate;
import org.veo.core.entity.Element;
import org.veo.core.entity.Identifiable;
import org.veo.core.entity.Incident;
import org.veo.core.entity.Key;
import org.veo.core.entity.LinkTailoringReference;
import org.veo.core.entity.Nameable;
import org.veo.core.entity.Person;
import org.veo.core.entity.Process;
import org.veo.core.entity.ProcessRisk;
import org.veo.core.entity.RiskAffected;
import org.veo.core.entity.Scenario;
import org.veo.core.entity.Scope;
import org.veo.core.entity.ScopeRisk;
import org.veo.core.entity.TailoringReference;
import org.veo.core.entity.Unit;
import org.veo.core.entity.definitions.ElementTypeDefinition;
import org.veo.core.entity.transform.EntityFactory;
import org.veo.core.entity.transform.IdentifiableFactory;
import org.veo.core.usecase.service.EntityStateMapper;
import org.veo.core.usecase.service.IdRefResolver;

import lombok.RequiredArgsConstructor;

/** A collection of transform functions to transform entities to Dto back and forth. */
@RequiredArgsConstructor
public final class DtoToEntityTransformer {

  private final EntityFactory factory;
  private final IdentifiableFactory identifiableFactory;
  private final DomainAssociationTransformer domainAssociationTransformer;
  private final EntityStateMapper entityStateMapper;

  public Person transformDto2Person(AbstractPersonDto source, IdRefResolver idRefResolver) {
    var target = createIdentifiable(Person.class, source);
    entityStateMapper.mapState(source, target, idRefResolver);
    return target;
  }

  public Asset transformDto2Asset(AbstractAssetDto source, IdRefResolver idRefResolver) {
    var target = createIdentifiable(Asset.class, source);
    entityStateMapper.mapState(source, target, idRefResolver);
    return target;
  }

  public Process transformDto2Process(AbstractProcessDto source, IdRefResolver idRefResolver) {
    var target = createIdentifiable(Process.class, source);
    entityStateMapper.mapState(source, target, idRefResolver);
    return target;
  }

  public Document transformDto2Document(AbstractDocumentDto source, IdRefResolver idRefResolver) {
    var target = createIdentifiable(Document.class, source);
    entityStateMapper.mapState(source, target, idRefResolver);
    return target;
  }

  public Control transformDto2Control(AbstractControlDto source, IdRefResolver idRefResolver) {
    var target = createIdentifiable(Control.class, source);
    entityStateMapper.mapState(source, target, idRefResolver);
    return target;
  }

  public Incident transformDto2Incident(AbstractIncidentDto source, IdRefResolver idRefResolver) {
    var target = createIdentifiable(Incident.class, source);
    entityStateMapper.mapState(source, target, idRefResolver);
    return target;
  }

  public Scenario transformDto2Scenario(AbstractScenarioDto source, IdRefResolver idRefResolver) {
    var target = createIdentifiable(Scenario.class, source);
    entityStateMapper.mapState(source, target, idRefResolver);
    return target;
  }

  public Scope transformDto2Scope(AbstractScopeDto source, IdRefResolver idRefResolver) {
    var target = createIdentifiable(Scope.class, source);
    entityStateMapper.mapState(source, target, idRefResolver);
    return target;
  }

  public AbstractRisk<?, ?> transformDto2Risk(AbstractRiskDto source, IdRefResolver idRefResolver) {
    if (source instanceof AssetRiskDto ar) {
      return transformDto2AssetRisk(ar, idRefResolver);
    }
    if (source instanceof ProcessRiskDto pr) {
      return transformDto2ProcessRisk(pr, idRefResolver);
    }
    if (source instanceof ScopeRiskDto sr) {
      return transformDto2ScopeRisk(sr, idRefResolver);
    }
    throw new NotImplementedException(
        "Unsupported risk DTO type %s".formatted(source.getClass().getSimpleName()));
  }

  public AssetRisk transformDto2AssetRisk(AssetRiskDto source, IdRefResolver idRefResolver) {
    var asset = idRefResolver.resolve(source.getAsset());
    var risk = mapRisk(source, idRefResolver, asset);
    // TODO-2150 map risk values
    return risk;
  }

  public ProcessRisk transformDto2ProcessRisk(ProcessRiskDto source, IdRefResolver idRefResolver) {
    var process = idRefResolver.resolve(source.getProcess());
    var risk = mapRisk(source, idRefResolver, process);
    risk.defineRiskValues(
        source.getDomainsWithRiskValues().values().stream()
            .flatMap(
                domainAssociation ->
                    CategorizedRiskValueMapper.toRiskValues(
                        // domain ID used by DTO may differ from resolved domain's ID
                        idRefResolver.resolve(domainAssociation.getReference()).getIdAsString(),
                        domainAssociation.getRiskDefinitions()))
            .collect(toSet()));
    return risk;
  }

  public ScopeRisk transformDto2ScopeRisk(ScopeRiskDto source, IdRefResolver idRefResolver) {
    var scope = idRefResolver.resolve(source.getScope());
    var risk = mapRisk(source, idRefResolver, scope);
    // TODO-2150 map risk values
    return risk;
  }

  private static <
          TElement extends RiskAffected<TElement, TRisk>,
          TRisk extends AbstractRisk<TElement, TRisk>>
      TRisk mapRisk(AbstractRiskDto source, IdRefResolver idRefResolver, TElement process) {
    var domains =
        source.getDomainReferences().stream().map(idRefResolver::resolve).collect(toSet());
    var risk = process.obtainRisk(idRefResolver.resolve(source.getScenario()), domains);
    risk.mitigate(
        source.getMitigation() != null ? idRefResolver.resolve(source.getMitigation()) : null);
    risk.appoint(
        source.getRiskOwner() != null ? idRefResolver.resolve(source.getRiskOwner()) : null);
    return risk;
  }

  public DomainTemplate transformTransformDomainTemplateDto2DomainTemplate(
      TransformDomainTemplateDto source, IdRefResolver idRefResolver) {
    var target = createIdentifiable(DomainTemplate.class, source);
    mapTransformDomainTemplate(source, idRefResolver, target);
    return target;
  }

  public Domain transformTransformDomainTemplateDto2Domain(
      TransformDomainTemplateDto source, IdRefResolver idRefResolver) {
    // DO NOT use domain template ID as domain ID.
    var target = identifiableFactory.create(Domain.class, Key.newUuid());
    mapTransformDomainTemplate(source, idRefResolver, target);
    target.setActive(true);
    return target;
  }

  // TODO VEO-839 remove when unit import no longer relies on it.
  public Unit transformDto2Unit(AbstractUnitDto source, IdRefResolver idRefResolver) {
    var target = createIdentifiable(Unit.class, source);
    entityStateMapper.mapState(source, target, idRefResolver);
    return target;
  }

  private void mapTransformDomainTemplate(
      TransformDomainTemplateDto source, IdRefResolver idRefResolver, DomainBase target) {
    target.setAuthority(source.getAuthority());
    target.setTemplateVersion(source.getTemplateVersion());
    mapNameableProperties(source, target);
    target.setElementTypeDefinitions(
        source.getElementTypeDefinitions().entrySet().stream()
            .map(entry -> mapElementTypeDefinition(entry.getKey(), entry.getValue(), target))
            .collect(Collectors.toSet()));
    target.setDecisions(source.getDecisions());
    target.setRiskDefinitions(copyOf(source.getRiskDefinitions()));
    if (source.getCatalogs() != null) {
      target.setCatalogs(
          source.getCatalogs().stream()
              .map(c -> transformDto2Catalog(c, idRefResolver))
              .collect(Collectors.toSet()));
    }
    target.setProfiles(copyOf(source.getProfiles()));
  }

  public ElementTypeDefinition mapElementTypeDefinition(
      String type, ElementTypeDefinitionDto source, DomainBase owner) {
    var target = factory.createElementTypeDefinition(type, owner);
    target.setSubTypes(source.getSubTypes());
    target.setCustomAspects(source.getCustomAspects());
    target.setLinks(source.getLinks());
    target.setTranslations(source.getTranslations());
    return target;
  }

  public Catalog transformDto2Catalog(AbstractCatalogDto source, IdRefResolver idRefResolver) {
    var target = createIdentifiable(Catalog.class, source);
    idRefResolver.resolve(source.getDomainTemplate()).addToCatalogs(target);
    mapNameableProperties(source, target);
    if (source instanceof ReferenceCatalogDto catalogDto) {
      target.setCatalogItems(idRefResolver.resolve(catalogDto.getCatalogItems()));
    } else if (source instanceof CompositeCatalogDto catalogDto) {
      target.setCatalogItems(
          convertSet(
              catalogDto.getCatalogItems(),
              ci -> transformDto2CatalogItem(ci, idRefResolver, target)));
    }

    return target;
  }

  public TailoringReference transformDto2TailoringReference(
      AbstractTailoringReferenceDto source, CatalogItem owner, IdRefResolver idRefResolver) {

    var target =
        source.isLinkTailoringReferences()
            ? createIdentifiable(LinkTailoringReference.class, source)
            : createIdentifiable(TailoringReference.class, source);
    target.setOwner(owner);
    target.setReferenceType(source.getReferenceType());
    if (source.getCatalogItem() != null) {
      CatalogItem resolve = idRefResolver.resolve(source.getCatalogItem());
      target.setCatalogItem(resolve);
    }

    if (source.isLinkTailoringReferences()) {
      TransformLinkTailoringReference tailoringReferenceDto =
          (TransformLinkTailoringReference) source;
      LinkTailoringReference tailoringReference = (LinkTailoringReference) target;
      tailoringReference.setAttributes(tailoringReferenceDto.getAttributes());
      tailoringReference.setLinkType(tailoringReferenceDto.getLinkType());
    }

    return target;
  }

  public CustomAspect transformDto2CustomAspect(
      EntityFactory factory, CustomAspectDto source, String type, DomainBase domain) {
    var target = factory.createCustomAspect(type, domain);
    target.setAttributes(source.getAttributes());
    return target;
  }

  public Element transformDto2Element(AbstractElementDto elementDto, IdRefResolver idRefResolver) {
    if (elementDto instanceof AbstractAssetDto asset) {
      return transformDto2Asset(asset, idRefResolver);
    } else if (elementDto instanceof AbstractControlDto control) {
      return transformDto2Control(control, idRefResolver);
    } else if (elementDto instanceof AbstractDocumentDto document) {
      return transformDto2Document(document, idRefResolver);
    } else if (elementDto instanceof AbstractIncidentDto incdent) {
      return transformDto2Incident(incdent, idRefResolver);
    } else if (elementDto instanceof AbstractPersonDto person) {
      return transformDto2Person(person, idRefResolver);
    } else if (elementDto instanceof AbstractProcessDto process) {
      return transformDto2Process(process, idRefResolver);
    } else if (elementDto instanceof AbstractScenarioDto scenario) {
      return transformDto2Scenario(scenario, idRefResolver);
    } else if (elementDto instanceof AbstractScopeDto scope) {
      return transformDto2Scope(scope, idRefResolver);
    }
    throw new IllegalArgumentException("unkown type: " + elementDto.getClass().getName());
  }

  private static void mapNameableProperties(NameableDto source, Nameable target) {
    target.setName(source.getName());
    target.setAbbreviation(source.getAbbreviation());
    target.setDescription(source.getDescription());
  }

  private static <TIn, TOut extends Identifiable> Set<TOut> convertSet(
      Set<TIn> source, Function<TIn, TOut> mapper) {
    if (mapper != null) {
      return source.stream().map(mapper).collect(Collectors.toSet());
    }
    return new HashSet<>();
  }

  public CatalogItem transformDto2CatalogItem(
      AbstractCatalogItemDto source, IdRefResolver idRefResolver, Catalog catalog) {
    var target = createIdentifiable(CatalogItem.class, source);
    target.setCatalog(catalog);
    if (source instanceof CompositeCatalogItemDto catalogitem) {
      AbstractElementDto elementDto = catalogitem.getElement();
      if (elementDto instanceof AbstractAssetDto asset) {
        target.setElement(transformDto2Asset(asset, idRefResolver));
      } else if (elementDto instanceof AbstractControlDto control) {
        target.setElement(transformDto2Control(control, idRefResolver));
      } else if (elementDto instanceof AbstractDocumentDto document) {
        target.setElement(transformDto2Document(document, idRefResolver));
      } else if (elementDto instanceof AbstractIncidentDto incident) {
        target.setElement(transformDto2Incident(incident, idRefResolver));
      } else if (elementDto instanceof AbstractPersonDto person) {
        target.setElement(transformDto2Person(person, idRefResolver));
      } else if (elementDto instanceof AbstractProcessDto process) {
        target.setElement(transformDto2Process(process, idRefResolver));
      } else if (elementDto instanceof AbstractScenarioDto scenario) {
        target.setElement(transformDto2Scenario(scenario, idRefResolver));
      } else if (elementDto instanceof AbstractScopeDto scope) {
        target.setElement(transformDto2Scope(scope, idRefResolver));
      }
    } else if (source instanceof ReferenceCatalogItemDto catalogitem) {
      target.setElement(idRefResolver.resolve(catalogitem.getElement()));
    } else {
      throw new IllegalArgumentException(
          "Cannot handle entity type " + source.getClass().getName());
    }
    target.setNamespace(source.getNamespace());

    target.getTailoringReferences().clear();
    target
        .getTailoringReferences()
        .addAll(
            convertSet(
                source.getTailoringReferences(),
                tr -> transformDto2TailoringReference(tr, target, idRefResolver)));
    return target;
  }

  private <T extends Identifiable> T createIdentifiable(Class<T> type, Object source) {
    Key<UUID> key = null;
    if (source instanceof IdentifiableDto identifiable) {
      key = Key.uuidFrom(identifiable.getId());
    }
    return identifiableFactory.create(type, key);
  }

  public Asset transformDto2Asset(
      AbstractAssetInDomainDto source, String domainId, IdRefResolver idRefResolver) {
    var target = createIdentifiable(Asset.class, source);
    var domain = idRefResolver.resolve(domainId, Domain.class);
    mapCompositeElement(source, target, domain, idRefResolver);
    return target;
  }

  public Control transformDto2Control(
      AbstractControlInDomainDto source, String domainId, IdRefResolver idRefResolver) {
    var target = createIdentifiable(Control.class, source);
    var domain = idRefResolver.resolve(domainId, Domain.class);
    mapCompositeElement(source, target, domain, idRefResolver);
    target.setRiskValues(
        domain, domainAssociationTransformer.mapRiskValues(source.getRiskValues(), domain));
    return target;
  }

  public Document transformDto2Document(
      AbstractDocumentInDomainDto source, String domainId, IdRefResolver idRefResolver) {
    var target = createIdentifiable(Document.class, source);
    var domain = idRefResolver.resolve(domainId, Domain.class);
    mapCompositeElement(source, target, domain, idRefResolver);
    return target;
  }

  public Incident transformDto2Incident(
      AbstractIncidentInDomainDto source, String domainId, IdRefResolver idRefResolver) {
    var target = createIdentifiable(Incident.class, source);
    var domain = idRefResolver.resolve(domainId, Domain.class);
    mapCompositeElement(source, target, domain, idRefResolver);
    return target;
  }

  public Person transformDto2Person(
      AbstractPersonInDomainDto source, String domainId, IdRefResolver idRefResolver) {
    var target = createIdentifiable(Person.class, source);
    var domain = idRefResolver.resolve(domainId, Domain.class);
    mapCompositeElement(source, target, domain, idRefResolver);
    return target;
  }

  public Process transformDto2Process(
      AbstractProcessInDomainDto source, String domainId, IdRefResolver idRefResolver) {
    var target = createIdentifiable(Process.class, source);
    var domain = idRefResolver.resolve(domainId, Domain.class);
    mapCompositeElement(source, target, domain, idRefResolver);
    target.setImpactValues(
        domain, domainAssociationTransformer.mapImpactValues(source.getRiskValues(), domain));
    return target;
  }

  public Scenario transformDto2Scenario(
      AbstractScenarioInDomainDto source, String domainId, IdRefResolver idRefResolver) {
    var target = createIdentifiable(Scenario.class, source);
    var domain = idRefResolver.resolve(domainId, Domain.class);
    mapCompositeElement(source, target, domain, idRefResolver);
    target.setPotentialProbability(
        domain,
        domainAssociationTransformer.mapPotentialProbability(source.getRiskValues(), domain));
    return target;
  }

  public Scope transformDto2Scope(
      AbstractScopeInDomainDto source, String domainId, IdRefResolver idRefResolver) {
    var target = createIdentifiable(Scope.class, source);
    var domain = idRefResolver.resolve(domainId, Domain.class);
    mapElement(source, target, domain, idRefResolver);
    target.setMembers(convertSet(source.getMembers(), idRefResolver::resolve));
    target.setRiskDefinition(
        domain,
        domainAssociationTransformer.toRiskDefinitionRef(source.getRiskDefinition(), domain));
    return target;
  }

  private <TElement extends CompositeElement<TElement>> void mapCompositeElement(
      AbstractCompositeElementInDomainDto<TElement> source,
      TElement target,
      Domain domain,
      IdRefResolver idRefResolver) {
    mapElement(source, target, domain, idRefResolver);
    target.setParts(source.getParts().stream().map(idRefResolver::resolve).collect(toSet()));
  }

  private <TElement extends Element> void mapElement(
      AbstractElementInDomainDto<TElement> source,
      TElement target,
      Domain domain,
      IdRefResolver idRefResolver) {
    mapNameableProperties(source, target);
    target.setOwnerOrContainingCatalogItem(idRefResolver.resolve(source.getOwner()));
    target.associateWithDomain(domain, source.getSubType(), source.getStatus());
    mapCustomAspects(source, target, domain);
    mapLinks(source, target, domain, idRefResolver);
  }

  private <TElement extends Element> void mapCustomAspects(
      AbstractElementInDomainDto<TElement> source, TElement target, Domain domain) {
    source
        .getCustomAspects()
        .getValue()
        .forEach(
            (type, attributes) -> {
              var ca = factory.createCustomAspect(type, domain);
              ca.setAttributes(attributes.getValue());
              target.applyCustomAspect(ca);
            });
  }

  private <TElement extends Element> void mapLinks(
      AbstractElementInDomainDto<TElement> source,
      TElement target,
      Domain domain,
      IdRefResolver resolver) {
    source
        .getLinks()
        .getValue()
        .forEach(
            (type, linkDtos) -> {
              linkDtos.forEach(
                  linkDto -> {
                    var link =
                        factory.createCustomLink(
                            resolver.resolve(linkDto.getTarget()), target, type, domain);
                    link.setAttributes(linkDto.getAttributes().getValue());
                    target.applyLink(link);
                  });
            });
  }
}
