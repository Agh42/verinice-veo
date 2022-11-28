/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2020  Urs Zeidler.
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
package org.veo.rest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import org.veo.adapter.DbIdRefResolver;
import org.veo.adapter.IdRefResolver;
import org.veo.adapter.presenter.api.common.ReferenceAssembler;
import org.veo.adapter.presenter.api.dto.SearchQueryDto;
import org.veo.adapter.presenter.api.response.transformer.DtoToEntityTransformer;
import org.veo.adapter.presenter.api.response.transformer.EntityToDtoTransformer;
import org.veo.core.entity.Client;
import org.veo.core.entity.Identifiable;
import org.veo.core.entity.Key;
import org.veo.core.entity.Versioned;
import org.veo.core.repository.ClientRepository;
import org.veo.core.repository.RepositoryProvider;
import org.veo.core.usecase.UseCaseInteractor;
import org.veo.rest.common.ClientNotActiveException;
import org.veo.rest.common.SearchResponse;
import org.veo.rest.common.marshalling.ReferenceAssemblerImpl;
import org.veo.rest.security.ApplicationUser;
import org.veo.service.EtagService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;

@SecurityRequirement(name = RestApplication.SECURITY_SCHEME_OAUTH)
@Slf4j
public abstract class AbstractEntityController {

  @Autowired protected ClientRepository clientRepository;

  @Autowired private RepositoryProvider repositoryProvider;

  @Autowired ReferenceAssemblerImpl referenceAssembler;

  @Autowired EntityToDtoTransformer entityToDtoTransformer;

  @Autowired DtoToEntityTransformer dtoToEntityTransformer;

  @Autowired ReferenceAssembler urlAssembler;

  @Autowired protected UseCaseInteractor useCaseInteractor;

  @Autowired protected EtagService etagService;

  protected AbstractEntityController() {}

  protected CacheControl defaultCacheControl = CacheControl.noCache();

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
    log.error("Error validating request", ex);
    return ex.getBindingResult().getAllErrors().stream()
        .map(FieldError.class::cast)
        .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
  }

  protected Client getClient(String clientId) {
    Key<UUID> id = Key.uuidFrom(clientId);
    return clientRepository
        .findActiveById(id)
        .orElseThrow(() -> new ClientNotActiveException(clientId));
  }

  protected Client getAuthenticatedClient(Authentication auth) {
    ApplicationUser user = ApplicationUser.authenticatedUser(auth.getPrincipal());
    return getClient(user);
  }

  protected Client getClient(ApplicationUser user) {
    return getClient(user.getClientId());
  }

  protected IdRefResolver createIdRefResolver(Client client) {
    return new DbIdRefResolver(repositoryProvider, client);
  }

  protected abstract String buildSearchUri(String searchId);

  protected ResponseEntity<SearchResponse> createSearchResponseBody(SearchQueryDto search) {
    try {
      // Build search URI and remove optional request param placeholders.
      var searchUri = buildSearchUri(search.getSearchId()).replaceFirst("\\{[^}]*}", "");
      return ResponseEntity.created(new URI(searchUri)).body(new SearchResponse(searchUri));
    } catch (IOException | URISyntaxException e) {
      log.error("Could not create search.", e);
      throw new IllegalArgumentException(String.format("Could not create search %s", search));
    }
  }

  protected <T extends Identifiable & Versioned> Optional<String> getEtag(
      Class<T> entityClass, String id) {
    return etagService.getEtag(entityClass, id);
  }
}
