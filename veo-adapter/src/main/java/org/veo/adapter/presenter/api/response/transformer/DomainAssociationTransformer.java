/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2020  Jonas Jordan.
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

import static org.veo.core.entity.risk.DomainRiskReferenceProvider.referencesForDomain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.veo.adapter.presenter.api.dto.AbstractAssetDto;
import org.veo.adapter.presenter.api.dto.AbstractControlDto;
import org.veo.adapter.presenter.api.dto.AbstractDocumentDto;
import org.veo.adapter.presenter.api.dto.AbstractIncidentDto;
import org.veo.adapter.presenter.api.dto.AbstractPersonDto;
import org.veo.adapter.presenter.api.dto.AbstractProcessDto;
import org.veo.adapter.presenter.api.dto.AbstractScenarioDto;
import org.veo.adapter.presenter.api.dto.AbstractScopeDto;
import org.veo.adapter.presenter.api.dto.AssetDomainAssociationDto;
import org.veo.adapter.presenter.api.dto.ControlDomainAssociationDto;
import org.veo.adapter.presenter.api.dto.ControlRiskValuesDto;
import org.veo.adapter.presenter.api.dto.DomainAssociationDto;
import org.veo.adapter.presenter.api.dto.ImpactRiskValuesDto;
import org.veo.adapter.presenter.api.dto.ProcessDomainAssociationDto;
import org.veo.adapter.presenter.api.dto.ScenarioDomainAssociationDto;
import org.veo.adapter.presenter.api.dto.ScenarioRiskValuesDto;
import org.veo.adapter.presenter.api.dto.ScopeDomainAssociationDto;
import org.veo.core.entity.Asset;
import org.veo.core.entity.Control;
import org.veo.core.entity.Document;
import org.veo.core.entity.Domain;
import org.veo.core.entity.DomainBase;
import org.veo.core.entity.Element;
import org.veo.core.entity.Incident;
import org.veo.core.entity.Person;
import org.veo.core.entity.Process;
import org.veo.core.entity.Scenario;
import org.veo.core.entity.Scope;
import org.veo.core.entity.aspects.Aspect;
import org.veo.core.entity.risk.CategoryRef;
import org.veo.core.entity.risk.ControlRiskValues;
import org.veo.core.entity.risk.DomainRiskReferenceProvider;
import org.veo.core.entity.risk.ImpactRef;
import org.veo.core.entity.risk.ImpactValues;
import org.veo.core.entity.risk.ImplementationStatusRef;
import org.veo.core.entity.risk.PotentialProbabilityImpl;
import org.veo.core.entity.risk.ProbabilityRef;
import org.veo.core.entity.risk.RiskDefinitionRef;
import org.veo.core.entity.risk.RiskImpactValues;
import org.veo.core.entity.risk.ScenarioRiskValues;
import org.veo.core.entity.state.ControlRiskValuesState;

/**
 * Maps {@link Domain} associations of {@link Element}s between entities and DTOs. See {@link
 * DomainAssociationDto}.
 */
public class DomainAssociationTransformer {

  public Map<RiskDefinitionRef, ControlRiskValues> mapRiskValues(
      Map<String, ? extends ControlRiskValuesState> riskValues, DomainBase domain) {
    var referenceProvider = referencesForDomain(domain);
    return riskValues.entrySet().stream()
        .collect(
            Collectors.toMap(
                kv -> referenceProvider.getRiskDefinitionRef(kv.getKey()),
                kv ->
                    mapControlRiskValuesDto2Entity(kv.getKey(), kv.getValue(), referenceProvider)));
  }

  private ControlRiskValues mapControlRiskValuesDto2Entity(
      String riskDefinitionId,
      ControlRiskValuesState riskValuesState,
      DomainRiskReferenceProvider referenceProvider) {
    var riskValues = new ControlRiskValues();
    riskValues.setImplementationStatus(
        Optional.ofNullable(riskValuesState.getImplementationStatus())
            .map(status -> referenceProvider.getImplementationStatus(riskDefinitionId, status))
            .orElse(null));
    return riskValues;
  }

  public Map<RiskDefinitionRef, ImpactValues> mapImpactValues(
      Map<String, ? extends RiskImpactValues> riskValues, DomainBase domain) {
    var referenceProvider = referencesForDomain(domain);
    return riskValues.entrySet().stream()
        .collect(
            Collectors.toMap(
                e -> referenceProvider.getRiskDefinitionRef(e.getKey()),
                e -> mapImpactValues(e.getKey(), e.getValue(), referenceProvider)));
  }

  private ImpactValues mapImpactValues(
      String riskDefinitionId,
      RiskImpactValues value,
      DomainRiskReferenceProvider referenceProvider) {
    var riskValues = new ImpactValues();

    Map<CategoryRef, ImpactRef> potentialImpacts =
        value.getPotentialImpacts().entrySet().stream()
            .collect(
                Collectors.toMap(
                    e -> toCategoryRef(riskDefinitionId, referenceProvider, e), Entry::getValue));
    riskValues.setPotentialImpacts(potentialImpacts);
    return riskValues;
  }

  private CategoryRef toCategoryRef(
      String riskDefinitionId,
      DomainRiskReferenceProvider referenceProvider,
      Entry<String, ImpactRef> e) {
    return referenceProvider.getCategoryRef(riskDefinitionId, e.getKey());
  }

  public Map<RiskDefinitionRef, PotentialProbabilityImpl> mapPotentialProbability(
      Map<String, ? extends ScenarioRiskValues> riskValues, DomainBase domain) {
    return riskValues.entrySet().stream().collect(groupScenarioRiskValuesByDomain(domain));
  }

  private Collector<
          Map.Entry<String, ? extends ScenarioRiskValues>,
          ?,
          Map<RiskDefinitionRef, PotentialProbabilityImpl>>
      groupScenarioRiskValuesByDomain(DomainBase domain) {
    var referenceProvider = referencesForDomain(domain);
    return Collectors.toMap(
        kv -> toRiskDefinitionRef(kv.getKey(), domain),
        kv -> mapScenarioRiskValues2Entity(kv.getKey(), kv.getValue(), referenceProvider));
  }

  private PotentialProbabilityImpl mapScenarioRiskValues2Entity(
      String riskDefinitionId,
      ScenarioRiskValues riskValues,
      DomainRiskReferenceProvider referenceProvider) {
    var result = new PotentialProbabilityImpl();

    result.setPotentialProbability(
        Optional.ofNullable(riskValues.getPotentialProbability())
            .map(pp -> referenceProvider.getProbabilityRef(riskDefinitionId, pp))
            .orElse(null));
    result.setPotentialProbabilityExplanation(riskValues.getPotentialProbabilityExplanation());
    return result;
  }

  public void mapDomainsToDto(Asset source, AbstractAssetDto target) {
    target.setDomains(
        extractDomainAssociations(
            source,
            domain -> {
              var associationDto = new AssetDomainAssociationDto();
              associationDto.setRiskValues(mapRiskValues(source, domain));
              return associationDto;
            }));
  }

  public void mapDomainsToDto(Control source, AbstractControlDto target) {
    Map<String, ControlDomainAssociationDto> extractDomainAssociations =
        extractDomainAssociations(
            source,
            domain -> {
              var associationDto = new ControlDomainAssociationDto();
              associationDto.setRiskValues(mapRiskValues(source, domain));
              return associationDto;
            });
    target.setDomains(extractDomainAssociations);
  }

  public Map<String, ControlRiskValuesDto> mapRiskValues(Control source, DomainBase domain) {
    return source
        .getRiskValues(domain)
        .map(
            riskValues ->
                riskValues.entrySet().stream()
                    .collect(
                        Collectors.toMap(
                            kv -> kv.getKey().getIdRef(), this::mapControlRiskValuesToDto)))
        .orElse(new HashMap<>());
  }

  private ControlRiskValuesDto mapControlRiskValuesToDto(
      Map.Entry<RiskDefinitionRef, ControlRiskValues> entry) {
    var riskValuesDto = new ControlRiskValuesDto();
    riskValuesDto.setImplementationStatus(
        Optional.ofNullable(entry.getValue().getImplementationStatus())
            .map(ImplementationStatusRef::getOrdinalValue)
            .orElse(null));
    return riskValuesDto;
  }

  private ImpactRiskValuesDto mapImpactRiskValuesToDto(
      Map.Entry<RiskDefinitionRef, ImpactValues> entry) {
    var riskValuesDto = new ImpactRiskValuesDto();
    riskValuesDto.setPotentialImpacts(
        entry.getValue().getPotentialImpacts().entrySet().stream()
            .collect(Collectors.toMap(e -> e.getKey().getIdRef(), Entry::getValue)));
    return riskValuesDto;
  }

  public void mapDomainsToDto(Document source, AbstractDocumentDto target) {
    target.setDomains(extractDomainAssociations(source, DomainAssociationDto::new));
  }

  public void mapDomainsToDto(Incident source, AbstractIncidentDto target) {
    target.setDomains(extractDomainAssociations(source, DomainAssociationDto::new));
  }

  public void mapDomainsToDto(Person source, AbstractPersonDto target) {
    target.setDomains(extractDomainAssociations(source, DomainAssociationDto::new));
  }

  public void mapDomainsToDto(Process source, AbstractProcessDto target) {
    target.setDomains(
        extractDomainAssociations(
            source,
            domain -> {
              var associationDto = new ProcessDomainAssociationDto();
              associationDto.setRiskValues(mapRiskValues(source, domain));
              return associationDto;
            }));
  }

  public Map<String, ImpactRiskValuesDto> mapRiskValues(Process source, DomainBase domain) {
    return source
        .getImpactValues(domain)
        .map(
            riskValues ->
                riskValues.entrySet().stream()
                    .collect(
                        Collectors.toMap(
                            kv -> kv.getKey().getIdRef(), this::mapImpactRiskValuesToDto)))
        .orElse(new HashMap<>());
  }

  public Map<String, ImpactRiskValuesDto> mapRiskValues(Asset source, DomainBase domain) {
    return source
        .getImpactValues(domain)
        .map(
            riskValues ->
                riskValues.entrySet().stream()
                    .collect(
                        Collectors.toMap(
                            kv -> kv.getKey().getIdRef(), this::mapImpactRiskValuesToDto)))
        .orElse(new HashMap<>());
  }

  public Map<String, ImpactRiskValuesDto> mapRiskValues(Scope source, DomainBase domain) {
    return source
        .getImpactValues(domain)
        .map(
            riskValues ->
                riskValues.entrySet().stream()
                    .collect(
                        Collectors.toMap(
                            kv -> kv.getKey().getIdRef(), this::mapImpactRiskValuesToDto)))
        .orElse(new HashMap<>());
  }

  public Map<String, ImpactRiskValuesDto> mapRiskValues(Asset source, Domain domain) {
    return source
        .getImpactValues(domain)
        .map(
            riskValues ->
                riskValues.entrySet().stream()
                    .collect(
                        Collectors.toMap(
                            kv -> kv.getKey().getIdRef(), this::mapImpactRiskValuesToDto)))
        .orElse(new HashMap<>());
  }

  public void mapDomainsToDto(Scenario source, AbstractScenarioDto target) {
    target.setDomains(
        extractDomainAssociations(
            source,
            domain -> {
              var associationDto = new ScenarioDomainAssociationDto();
              associationDto.setRiskValues(mapRiskValues(source, domain));
              return associationDto;
            }));
  }

  public Map<String, ScenarioRiskValuesDto> mapRiskValues(Scenario source, DomainBase domain) {
    return source
        .getPotentialProbability(domain)
        .map(
            riskValues ->
                riskValues.entrySet().stream()
                    .collect(
                        Collectors.toMap(
                            kv -> kv.getKey().getIdRef(), this::mapScenarioRiskValuesToDto)))
        .orElse(new HashMap<>());
  }

  private ScenarioRiskValuesDto mapScenarioRiskValuesToDto(
      Map.Entry<RiskDefinitionRef, PotentialProbabilityImpl> entry) {
    var riskValuesDto = new ScenarioRiskValuesDto();
    riskValuesDto.setPotentialProbability(
        Optional.ofNullable(entry.getValue().getPotentialProbability())
            .map(ProbabilityRef::getIdRef)
            .orElse(null));
    riskValuesDto.setPotentialProbabilityExplanation(
        entry.getValue().getPotentialProbabilityExplanation());
    return riskValuesDto;
  }

  public void mapDomainsToDto(Scope source, AbstractScopeDto target) {
    target.setDomains(
        extractDomainAssociations(
            source,
            domain -> {
              var associationDto = new ScopeDomainAssociationDto();
              associationDto.setRiskDefinition(mapRiskDefinition(source, domain));
              associationDto.setRiskValues(mapRiskValues(source, domain));
              return associationDto;
            }));
  }

  public String mapRiskDefinition(Scope source, DomainBase domain) {
    return source.getRiskDefinition(domain).map(RiskDefinitionRef::getIdRef).orElse(null);
  }

  private <T extends DomainAssociationDto> Map<String, T> extractDomainAssociations(
      Element source, Supplier<T> supplier) {
    return extractDomainAssociations(source, domain -> supplier.get());
  }

  private <T extends DomainAssociationDto> Map<String, T> extractDomainAssociations(
      Element source, Function<DomainBase, T> supplier) {
    // Catalog item elements in a domain template may have aspects for
    // domains that
    // the element is not assigned to. Seems invalid, but that's the
    // situation we
    // have to deal with here.
    var domains = new HashSet<DomainBase>();
    domains.addAll(source.getDomains());
    domains.addAll(
        source.getSubTypeAspects().stream().map(Aspect::getDomain).collect(Collectors.toSet()));

    return domains.stream()
        .collect(
            Collectors.toMap(
                domain -> domain.getId().uuidValue(),
                domain -> {
                  var association = supplier.apply(domain);
                  association.setSubType(source.findSubType(domain).orElse(null));
                  association.setStatus(source.findStatus(domain).orElse(null));
                  association.setDecisionResults(source.getDecisionResults(domain));
                  return association;
                }));
  }

  RiskDefinitionRef toRiskDefinitionRef(String riskDefId, DomainBase domain) {
    if (riskDefId == null) {
      return null;
    }
    return referencesForDomain(domain).getRiskDefinitionRef(riskDefId);
  }
}
