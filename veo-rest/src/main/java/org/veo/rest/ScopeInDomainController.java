/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2023  Jonas Jordan
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

import static org.veo.rest.ControllerConstants.CHILD_ELEMENT_IDS_PARAM;
import static org.veo.rest.ControllerConstants.DESCRIPTION_PARAM;
import static org.veo.rest.ControllerConstants.DESIGNATOR_PARAM;
import static org.veo.rest.ControllerConstants.DISPLAY_NAME_PARAM;
import static org.veo.rest.ControllerConstants.HAS_CHILD_ELEMENTS_PARAM;
import static org.veo.rest.ControllerConstants.HAS_PARENT_ELEMENTS_PARAM;
import static org.veo.rest.ControllerConstants.NAME_PARAM;
import static org.veo.rest.ControllerConstants.PAGE_NUMBER_DEFAULT_VALUE;
import static org.veo.rest.ControllerConstants.PAGE_NUMBER_PARAM;
import static org.veo.rest.ControllerConstants.PAGE_SIZE_DEFAULT_VALUE;
import static org.veo.rest.ControllerConstants.PAGE_SIZE_PARAM;
import static org.veo.rest.ControllerConstants.SCOPE_IDS_DESCRIPTION;
import static org.veo.rest.ControllerConstants.SCOPE_IDS_PARAM;
import static org.veo.rest.ControllerConstants.SORT_COLUMN_DEFAULT_VALUE;
import static org.veo.rest.ControllerConstants.SORT_COLUMN_PARAM;
import static org.veo.rest.ControllerConstants.SORT_ORDER_DEFAULT_VALUE;
import static org.veo.rest.ControllerConstants.SORT_ORDER_PARAM;
import static org.veo.rest.ControllerConstants.SORT_ORDER_PATTERN;
import static org.veo.rest.ControllerConstants.STATUS_PARAM;
import static org.veo.rest.ControllerConstants.SUB_TYPE_PARAM;
import static org.veo.rest.ControllerConstants.UNIT_PARAM;
import static org.veo.rest.ControllerConstants.UPDATED_BY_PARAM;
import static org.veo.rest.ControllerConstants.UUID_DESCRIPTION;
import static org.veo.rest.ControllerConstants.UUID_EXAMPLE;
import static org.veo.rest.ControllerConstants.UUID_PARAM;
import static org.veo.rest.ControllerConstants.UUID_PARAM_SPEC;
import static org.veo.rest.ControllerConstants.UUID_REGEX;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import org.veo.adapter.presenter.api.common.ApiResponseBody;
import org.veo.adapter.presenter.api.dto.AbstractElementInDomainDto;
import org.veo.adapter.presenter.api.dto.PageDto;
import org.veo.adapter.presenter.api.dto.create.CreateDomainAssociationDto;
import org.veo.adapter.presenter.api.dto.create.CreateScopeInDomainDto;
import org.veo.adapter.presenter.api.dto.full.FullControlInDomainDto;
import org.veo.adapter.presenter.api.dto.full.FullScopeInDomainDto;
import org.veo.adapter.presenter.api.io.mapper.GetElementsInputMapper;
import org.veo.adapter.presenter.api.io.mapper.PagingMapper;
import org.veo.adapter.presenter.api.response.transformer.DtoToEntityTransformer;
import org.veo.adapter.presenter.api.response.transformer.EntityToDtoTransformer;
import org.veo.core.entity.Domain;
import org.veo.core.entity.Element;
import org.veo.core.entity.Scope;
import org.veo.core.usecase.base.GenericGetElementsUseCase;
import org.veo.core.usecase.base.UpdateScopeInDomainUseCase;
import org.veo.core.usecase.decision.EvaluateElementUseCase;
import org.veo.core.usecase.scope.CreateScopeUseCase;
import org.veo.core.usecase.scope.GetScopeUseCase;
import org.veo.core.usecase.scope.GetScopesUseCase;
import org.veo.rest.annotations.UnitUuidParam;
import org.veo.rest.common.ClientLookup;
import org.veo.rest.common.ElementInDomainService;
import org.veo.rest.schemas.EvaluateElementOutputSchema;
import org.veo.rest.security.ApplicationUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** REST service which provides methods to manage scopes from the viewpoint of a domain. */
@RequiredArgsConstructor
@RestController
@RequestMapping(ScopeInDomainController.URL_BASE_PATH)
@Slf4j
public class ScopeInDomainController {
  public static final String URL_BASE_PATH =
      "/" + Domain.PLURAL_TERM + "/{domainId}/" + Scope.PLURAL_TERM;
  private final ClientLookup clientLookup;
  private final GetScopeUseCase getScopeUseCase;
  private final GetScopesUseCase getScopesUseCase;
  private final GenericGetElementsUseCase getElementsUseCase;
  private final CreateScopeUseCase createUseCase;
  private final UpdateScopeInDomainUseCase updateUseCase;
  private final ElementInDomainService elementService;
  private final DtoToEntityTransformer dtoToEntityTransformer;
  private final EntityToDtoTransformer entityToDtoTransformer;

  @Operation(summary = "Loads a scope from the viewpoint of a domain")
  @ApiResponse(
      responseCode = "200",
      description = "Scope loaded",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = FullScopeInDomainDto.class)))
  @ApiResponse(responseCode = "404", description = "Scope not found")
  @ApiResponse(responseCode = "404", description = "Domain not found")
  @ApiResponse(responseCode = "404", description = "Scope not associated with domain")
  @GetMapping(UUID_PARAM_SPEC)
  public @Valid Future<ResponseEntity<FullScopeInDomainDto>> getElement(
      @Parameter(required = true, hidden = true) Authentication auth,
      @Parameter(required = true, example = UUID_EXAMPLE, description = UUID_DESCRIPTION)
          @PathVariable
          String domainId,
      @Parameter(required = true, example = UUID_EXAMPLE, description = UUID_DESCRIPTION)
          @PathVariable
          String uuid,
      WebRequest request) {
    return elementService.getElement(
        auth,
        domainId,
        uuid,
        request,
        Scope.class,
        getScopeUseCase,
        entityToDtoTransformer::transformScope2Dto);
  }

  @GetMapping
  @Operation(summary = "Loads all scopes in a domain")
  public @Valid Future<PageDto<FullScopeInDomainDto>> getControls(
      @Parameter(hidden = true) Authentication auth,
      @Parameter(required = true, example = UUID_EXAMPLE, description = UUID_DESCRIPTION)
          @PathVariable
          String domainId,
      @UnitUuidParam @RequestParam(value = UNIT_PARAM, required = false) String unitUuid,
      @RequestParam(value = DISPLAY_NAME_PARAM, required = false) String displayName,
      @RequestParam(value = SUB_TYPE_PARAM, required = false) String subType,
      @RequestParam(value = STATUS_PARAM, required = false) String status,
      @RequestParam(value = CHILD_ELEMENT_IDS_PARAM, required = false) List<String> childElementIds,
      @RequestParam(value = HAS_PARENT_ELEMENTS_PARAM, required = false) Boolean hasParentElements,
      @RequestParam(value = HAS_CHILD_ELEMENTS_PARAM, required = false) Boolean hasChildElements,
      @RequestParam(value = DESCRIPTION_PARAM, required = false) String description,
      @RequestParam(value = DESIGNATOR_PARAM, required = false) String designator,
      @RequestParam(value = NAME_PARAM, required = false) String name,
      @RequestParam(value = UPDATED_BY_PARAM, required = false) String updatedBy,
      @RequestParam(
              value = PAGE_SIZE_PARAM,
              required = false,
              defaultValue = PAGE_SIZE_DEFAULT_VALUE)
          Integer pageSize,
      @RequestParam(
              value = PAGE_NUMBER_PARAM,
              required = false,
              defaultValue = PAGE_NUMBER_DEFAULT_VALUE)
          Integer pageNumber,
      @RequestParam(
              value = SORT_COLUMN_PARAM,
              required = false,
              defaultValue = SORT_COLUMN_DEFAULT_VALUE)
          String sortColumn,
      @RequestParam(
              value = SORT_ORDER_PARAM,
              required = false,
              defaultValue = SORT_ORDER_DEFAULT_VALUE)
          @Pattern(regexp = SORT_ORDER_PATTERN)
          String sortOrder) {
    return elementService.getElements(
        domainId,
        getScopesUseCase,
        GetElementsInputMapper.map(
            clientLookup.getClient(auth),
            unitUuid,
            domainId,
            displayName,
            subType,
            status,
            childElementIds,
            hasChildElements,
            hasParentElements,
            null,
            null,
            description,
            designator,
            name,
            updatedBy,
            PagingMapper.toConfig(
                pageSize, pageNumber,
                sortColumn, sortOrder)),
        entityToDtoTransformer::transformScope2Dto);
  }

  @Operation(summary = "Creates a scope, assigning it to the domain")
  @PostMapping
  @ApiResponse(
      responseCode = "201",
      description = "Scope created",
      headers = @Header(name = "Location"))
  @ApiResponse(responseCode = "404", description = "Domain not found")
  public CompletableFuture<ResponseEntity<ApiResponseBody>> createElement(
      @Parameter(required = true, hidden = true) ApplicationUser user,
      @Parameter(required = true, example = UUID_EXAMPLE, description = UUID_DESCRIPTION)
          @PathVariable
          String domainId,
      @Valid @NotNull @RequestBody CreateScopeInDomainDto dto,
      @Parameter(description = SCOPE_IDS_DESCRIPTION)
          @RequestParam(name = SCOPE_IDS_PARAM, required = false)
          List<String> scopeIds) {
    return elementService.createElement(
        user, domainId, dto, scopeIds, createUseCase, dtoToEntityTransformer::transformDto2Scope);
  }

  @Operation(summary = "Associates an existing scope with a domain")
  @PostMapping(UUID_PARAM_SPEC)
  @ApiResponse(responseCode = "200", description = "Scope associated with domain")
  @ApiResponse(responseCode = "404", description = "Scope not found")
  @ApiResponse(responseCode = "404", description = "Domain not found")
  @ApiResponse(responseCode = "409", description = "Scope already associated with domain")
  public CompletableFuture<ResponseEntity<FullScopeInDomainDto>> associateElementWithDomain(
      @Parameter(hidden = true) Authentication auth,
      @Parameter(required = true, example = UUID_EXAMPLE, description = UUID_DESCRIPTION)
          @PathVariable
          String domainId,
      @Parameter(required = true, example = UUID_EXAMPLE, description = UUID_DESCRIPTION)
          @PathVariable
          String uuid,
      @Valid @NotNull @RequestBody CreateDomainAssociationDto dto) {
    return elementService.associateElementWithDomain(
        auth, domainId, uuid, dto, Scope.class, entityToDtoTransformer::transformScope2Dto);
  }

  @Operation(summary = "Updates a scope from the viewpoint of a domain")
  @PutMapping(UUID_PARAM_SPEC)
  @ApiResponse(responseCode = "200", description = "Scope updated")
  @ApiResponse(responseCode = "404", description = "Scope not found")
  @ApiResponse(responseCode = "404", description = "Scope not associated with domain")
  public CompletableFuture<ResponseEntity<FullScopeInDomainDto>> updateElement(
      @Parameter(hidden = true) Authentication auth,
      @Parameter(required = true, example = UUID_EXAMPLE, description = UUID_DESCRIPTION)
          @PathVariable
          String domainId,
      @RequestHeader(ControllerConstants.IF_MATCH_HEADER) @NotBlank String eTag,
      @Parameter(required = true, example = UUID_EXAMPLE, description = UUID_DESCRIPTION)
          @PathVariable
          String uuid,
      @Valid @NotNull @RequestBody FullScopeInDomainDto dto) {
    return elementService.update(
        auth,
        domainId,
        eTag,
        uuid,
        dto,
        updateUseCase,
        dtoToEntityTransformer::transformDto2Scope,
        entityToDtoTransformer::transformScope2Dto);
  }

  @Operation(
      summary =
          "Evaluates decisions and inspections on a transient scope without persisting anything")
  @ApiResponse(
      responseCode = "200",
      description = "Element evaluated",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = EvaluateElementOutputSchema.class)))
  @ApiResponse(responseCode = "404", description = "Domain not found")
  @PostMapping(value = "/evaluation")
  public @Valid CompletableFuture<ResponseEntity<EvaluateElementUseCase.OutputData>> evaluate(
      @Parameter(required = true, hidden = true) Authentication auth,
      @Parameter(required = true, example = UUID_EXAMPLE, description = UUID_DESCRIPTION)
          @PathVariable
          String domainId,
      @Valid @RequestBody FullScopeInDomainDto dto) {
    return elementService.evaluate(auth, dto, domainId, dtoToEntityTransformer::transformDto2Scope);
  }

  @Operation(summary = "Loads the members of a scope in a domain")
  @ApiResponse(
      responseCode = "200",
      description = "Members loaded",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              array =
                  @ArraySchema(schema = @Schema(implementation = FullControlInDomainDto.class))))
  @ApiResponse(responseCode = "404", description = "Scope not found")
  @ApiResponse(responseCode = "404", description = "Domain not found")
  @GetMapping(value = "/{" + UUID_PARAM + ":" + UUID_REGEX + "}/members")
  public @Valid Future<PageDto<AbstractElementInDomainDto<Element>>> getElementParts(
      @Parameter(hidden = true) Authentication auth,
      @Parameter(required = true, example = UUID_EXAMPLE, description = UUID_DESCRIPTION)
          @PathVariable
          String domainId,
      @Parameter(required = true, example = UUID_EXAMPLE, description = UUID_DESCRIPTION)
          @PathVariable
          String uuid,
      @RequestParam(
              value = PAGE_SIZE_PARAM,
              required = false,
              defaultValue = PAGE_SIZE_DEFAULT_VALUE)
          Integer pageSize,
      @RequestParam(
              value = PAGE_NUMBER_PARAM,
              required = false,
              defaultValue = PAGE_NUMBER_DEFAULT_VALUE)
          Integer pageNumber,
      @RequestParam(
              value = SORT_COLUMN_PARAM,
              required = false,
              defaultValue = SORT_COLUMN_DEFAULT_VALUE)
          String sortColumn,
      @RequestParam(
              value = SORT_ORDER_PARAM,
              required = false,
              defaultValue = SORT_ORDER_DEFAULT_VALUE)
          @Pattern(regexp = SORT_ORDER_PATTERN)
          String sortOrder) {
    var client = clientLookup.getClient(auth);
    elementService.ensureElementExists(client, domainId, uuid, getScopeUseCase);
    return elementService.getElements(
        domainId,
        getElementsUseCase,
        GetElementsInputMapper.map(
            client,
            null,
            domainId,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            uuid,
            null,
            null,
            null,
            null,
            PagingMapper.toConfig(
                pageSize, pageNumber,
                sortColumn, sortOrder)),
        entityToDtoTransformer::transformElement2Dto);
  }
}
