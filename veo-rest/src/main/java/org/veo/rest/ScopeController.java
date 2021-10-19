/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2019  Daniel Murygin.
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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.veo.rest.ControllerConstants.ANY_AUTH;
import static org.veo.rest.ControllerConstants.ANY_INT;
import static org.veo.rest.ControllerConstants.ANY_STRING;
import static org.veo.rest.ControllerConstants.DESCRIPTION_PARAM;
import static org.veo.rest.ControllerConstants.DESIGNATOR_PARAM;
import static org.veo.rest.ControllerConstants.DISPLAY_NAME_PARAM;
import static org.veo.rest.ControllerConstants.NAME_PARAM;
import static org.veo.rest.ControllerConstants.PAGE_NUMBER_DEFAULT_VALUE;
import static org.veo.rest.ControllerConstants.PAGE_NUMBER_PARAM;
import static org.veo.rest.ControllerConstants.PAGE_SIZE_DEFAULT_VALUE;
import static org.veo.rest.ControllerConstants.PAGE_SIZE_PARAM;
import static org.veo.rest.ControllerConstants.SORT_COLUMN_DEFAULT_VALUE;
import static org.veo.rest.ControllerConstants.SORT_COLUMN_PARAM;
import static org.veo.rest.ControllerConstants.SORT_ORDER_DEFAULT_VALUE;
import static org.veo.rest.ControllerConstants.SORT_ORDER_PARAM;
import static org.veo.rest.ControllerConstants.SORT_ORDER_PATTERN;
import static org.veo.rest.ControllerConstants.STATUS_PARAM;
import static org.veo.rest.ControllerConstants.SUB_TYPE_PARAM;
import static org.veo.rest.ControllerConstants.UNIT_PARAM;
import static org.veo.rest.ControllerConstants.UPDATED_BY_PARAM;
import static org.veo.rest.ControllerConstants.UUID_PARAM;
import static org.veo.rest.ControllerConstants.UUID_REGEX;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.JanLoebel.jsonschemavalidation.JsonSchemaValidation;

import org.veo.adapter.IdRefResolver;
import org.veo.adapter.presenter.api.common.ApiResponseBody;
import org.veo.adapter.presenter.api.dto.AbstractElementDto;
import org.veo.adapter.presenter.api.dto.PageDto;
import org.veo.adapter.presenter.api.dto.SearchQueryDto;
import org.veo.adapter.presenter.api.dto.create.CreateScopeDto;
import org.veo.adapter.presenter.api.dto.full.FullScopeDto;
import org.veo.adapter.presenter.api.io.mapper.GetElementsInputMapper;
import org.veo.adapter.presenter.api.io.mapper.PagingMapper;
import org.veo.core.entity.Client;
import org.veo.core.entity.Key;
import org.veo.core.entity.Scope;
import org.veo.core.usecase.UseCase;
import org.veo.core.usecase.UseCaseInteractor;
import org.veo.core.usecase.base.CreateElementUseCase;
import org.veo.core.usecase.base.DeleteElementUseCase;
import org.veo.core.usecase.base.GetElementsUseCase;
import org.veo.core.usecase.base.ModifyElementUseCase;
import org.veo.core.usecase.common.ETag;
import org.veo.core.usecase.scope.CreateScopeUseCase;
import org.veo.core.usecase.scope.GetScopeUseCase;
import org.veo.core.usecase.scope.GetScopesUseCase;
import org.veo.core.usecase.scope.UpdateScopeUseCase;
import org.veo.rest.annotations.ParameterUuid;
import org.veo.rest.annotations.UnitUuidParam;
import org.veo.rest.common.RestApiResponse;
import org.veo.rest.security.ApplicationUser;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;

/**
 * REST service which provides methods to manage scopes.
 */
@RestController
@RequestMapping(ScopeController.URL_BASE_PATH)
@Slf4j
public class ScopeController extends AbstractEntityControllerWithDefaultSearch {

    public static final String URL_BASE_PATH = "/" + Scope.PLURAL_TERM;

    protected static final String TYPE_PARAM = "type";

    private final UseCaseInteractor useCaseInteractor;

    private final CreateScopeUseCase createScopeUseCase;
    private final GetScopeUseCase getScopeUseCase;
    private final GetScopesUseCase getScopesUseCase;
    private final UpdateScopeUseCase updateScopeUseCase;
    private final DeleteElementUseCase deleteElementUseCase;

    public ScopeController(UseCaseInteractor useCaseInteractor,
            CreateScopeUseCase createScopeUseCase, GetScopeUseCase getScopeUseCase,
            GetScopesUseCase getScopesUseCase, UpdateScopeUseCase updateScopeUseCase,
            DeleteElementUseCase deleteElementUseCase) {
        this.useCaseInteractor = useCaseInteractor;
        this.createScopeUseCase = createScopeUseCase;
        this.getScopeUseCase = getScopeUseCase;
        this.getScopesUseCase = getScopesUseCase;
        this.updateScopeUseCase = updateScopeUseCase;
        this.deleteElementUseCase = deleteElementUseCase;
    }

    @GetMapping
    @Operation(summary = "Loads all scopes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                         description = "Members loaded",
                         content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            array = @ArraySchema(schema = @Schema(implementation = FullScopeDto.class)))),
            @ApiResponse(responseCode = "404", description = "Scope not found") })
    public @Valid CompletableFuture<PageDto<FullScopeDto>> getScopes(
            @Parameter(required = false, hidden = true) Authentication auth,
            @UnitUuidParam @RequestParam(value = UNIT_PARAM, required = false) String unitUuid,
            @RequestParam(value = DISPLAY_NAME_PARAM, required = false) String displayName,
            @RequestParam(value = SUB_TYPE_PARAM, required = false) String subType,
            @RequestParam(value = STATUS_PARAM, required = false) String status,
            @RequestParam(value = DESCRIPTION_PARAM, required = false) String description,
            @RequestParam(value = DESIGNATOR_PARAM, required = false) String designator,
            @RequestParam(value = NAME_PARAM, required = false) String name,
            @RequestParam(value = UPDATED_BY_PARAM, required = false) String updatedBy,
            @RequestParam(value = PAGE_SIZE_PARAM,
                          required = false,
                          defaultValue = PAGE_SIZE_DEFAULT_VALUE) Integer pageSize,
            @RequestParam(value = PAGE_NUMBER_PARAM,
                          required = false,
                          defaultValue = PAGE_NUMBER_DEFAULT_VALUE) Integer pageNumber,
            @RequestParam(value = SORT_COLUMN_PARAM,
                          required = false,
                          defaultValue = SORT_COLUMN_DEFAULT_VALUE) String sortColumn,
            @RequestParam(value = SORT_ORDER_PARAM,
                          required = false,
                          defaultValue = SORT_ORDER_DEFAULT_VALUE) @Pattern(regexp = SORT_ORDER_PATTERN) String sortOrder) {
        Client client = null;
        try {
            client = getAuthenticatedClient(auth);
        } catch (NoSuchElementException e) {
            return CompletableFuture.supplyAsync(PageDto::emptyPage);
        }

        final GetElementsUseCase.InputData inputData = GetElementsInputMapper.map(client, unitUuid,
                                                                                  displayName,
                                                                                  subType, status,
                                                                                  description,
                                                                                  designator, name,
                                                                                  updatedBy,
                                                                                  PagingMapper.toConfig(pageSize,
                                                                                                        pageNumber,
                                                                                                        sortColumn,
                                                                                                        sortOrder));
        return getScopes(inputData);
    }

    private CompletableFuture<PageDto<FullScopeDto>> getScopes(
            GetElementsUseCase.InputData inputData) {
        return useCaseInteractor.execute(getScopesUseCase, inputData,
                                         output -> PagingMapper.toPage(output.getElements(),
                                                                       entityToDtoTransformer::transformScope2Dto));
    }

    @GetMapping(value = "/{" + UUID_PARAM + ":" + UUID_REGEX + "}")
    @Operation(summary = "Loads a scope")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                         description = "Scope loaded",
                         content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = FullScopeDto.class))),
            @ApiResponse(responseCode = "404", description = "Scope not found") })
    public @Valid CompletableFuture<ResponseEntity<FullScopeDto>> getScope(
            @Parameter(required = false, hidden = true) Authentication auth,
            @ParameterUuid @PathVariable(UUID_PARAM) String uuid) {
        Client client = getAuthenticatedClient(auth);

        CompletableFuture<FullScopeDto> scopeFuture = useCaseInteractor.execute(getScopeUseCase,
                                                                                new UseCase.IdAndClient(
                                                                                        Key.uuidFrom(uuid),
                                                                                        client),
                                                                                output -> entityToDtoTransformer.transformScope2Dto(output.getScope()));
        return scopeFuture.thenApply(scopeDto -> ResponseEntity.ok()
                                                               .eTag(ETag.from(scopeDto.getId(),
                                                                               scopeDto.getVersion()))
                                                               .body(scopeDto));
    }

    @GetMapping(value = "/{" + UUID_PARAM + ":" + UUID_REGEX + "}/members")
    @Operation(summary = "Loads the members of a scope")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                         description = "Members loaded",
                         content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            array = @ArraySchema(schema = @Schema(implementation = FullScopeDto.class)))),
            @ApiResponse(responseCode = "404", description = "Scope not found") })
    public @Valid CompletableFuture<List<AbstractElementDto>> getMembers(
            @Parameter(required = false, hidden = true) Authentication auth,
            @ParameterUuid @PathVariable(UUID_PARAM) String uuid) {
        Client client = getAuthenticatedClient(auth);
        return useCaseInteractor.execute(getScopeUseCase,
                                         new UseCase.IdAndClient(Key.uuidFrom(uuid), client),
                                         output -> {
                                             Scope scope = output.getScope();
                                             return scope.getMembers()
                                                         .stream()
                                                         .map(member -> entityToDtoTransformer.transform2Dto(member))
                                                         .collect(Collectors.toList());
                                         });
    }

    @PostMapping()
    @Operation(summary = "Creates a scope")
    @ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Scope created") })
    public CompletableFuture<ResponseEntity<ApiResponseBody>> createScope(
            @Parameter(hidden = true) ApplicationUser user,
            @Valid @NotNull @RequestBody @JsonSchemaValidation(Scope.SINGULAR_TERM) CreateScopeDto createScopeDto) {
        return useCaseInteractor.execute(createScopeUseCase,
                                         (Supplier<CreateElementUseCase.InputData<Scope>>) () -> {
                                             Client client = getClient(user);
                                             IdRefResolver idRefResolver = createIdRefResolver(client);
                                             return new CreateElementUseCase.InputData<>(
                                                     dtoToEntityTransformer.transformDto2Scope(createScopeDto,
                                                                                               idRefResolver),
                                                     client);
                                         }, output -> {
                                             Scope scope = output.getEntity();
                                             Optional<String> scopeId = scope.getId() == null
                                                     ? Optional.empty()
                                                     : Optional.ofNullable(scope.getId()
                                                                                .uuidValue());
                                             ApiResponseBody apiResponseBody = new ApiResponseBody(
                                                     true, scopeId, "Scope created successfully.");
                                             return RestApiResponse.created(URL_BASE_PATH,
                                                                            apiResponseBody);
                                         });
    }

    @PutMapping(value = "/{" + UUID_PARAM + ":" + UUID_REGEX + "}")
    @Operation(summary = "Updates a scope")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Scope updated"),
            @ApiResponse(responseCode = "404", description = "Scope not found") })
    public CompletableFuture<FullScopeDto> updateScope(
            @Parameter(hidden = true) ApplicationUser user,
            @RequestHeader(ControllerConstants.IF_MATCH_HEADER) @NotBlank String eTag,
            @ParameterUuid @PathVariable(UUID_PARAM) String uuid,
            @Valid @NotNull @RequestBody @JsonSchemaValidation(Scope.SINGULAR_TERM) FullScopeDto scopeDto) {
        scopeDto.applyResourceId(uuid);
        return useCaseInteractor.execute(updateScopeUseCase,
                                         (Supplier<ModifyElementUseCase.InputData<Scope>>) () -> {
                                             Client client = getClient(user);
                                             IdRefResolver idRefResolver = createIdRefResolver(client);
                                             return new UpdateScopeUseCase.InputData<>(
                                                     dtoToEntityTransformer.transformDto2Scope(scopeDto,
                                                                                               idRefResolver),
                                                     client, eTag, user.getUsername());
                                         },
                                         output -> entityToDtoTransformer.transformScope2Dto(output.getEntity()));
    }

    @DeleteMapping(value = "/{" + UUID_PARAM + ":" + UUID_REGEX + "}")
    @Operation(summary = "Deletes a scope")
    @ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Scope deleted"),
            @ApiResponse(responseCode = "404", description = "Scope not found") })
    public CompletableFuture<ResponseEntity<ApiResponseBody>> deleteScope(
            @Parameter(required = false, hidden = true) Authentication auth,
            @ParameterUuid @PathVariable(UUID_PARAM) String uuid) {
        Client client = getAuthenticatedClient(auth);

        return useCaseInteractor.execute(deleteElementUseCase,
                                         new DeleteElementUseCase.InputData(Scope.class,
                                                 Key.uuidFrom(uuid), client),
                                         output -> ResponseEntity.noContent()
                                                                 .build());
    }

    @Override
    @SuppressFBWarnings("NP_NULL_PARAM_DEREF_ALL_TARGETS_DANGEROUS")
    protected String buildSearchUri(String id) {
        return linkTo(methodOn(ScopeController.class).runSearch(ANY_AUTH, id, ANY_INT, ANY_INT,
                                                                ANY_STRING, ANY_STRING))
                                                                                        .withSelfRel()
                                                                                        .getHref();
    }

    @GetMapping(value = "/searches/{searchId}")
    @Operation(summary = "Finds scopes for the search.")
    public @Valid CompletableFuture<PageDto<FullScopeDto>> runSearch(
            @Parameter(required = false, hidden = true) Authentication auth,
            @PathVariable String searchId,
            @RequestParam(value = PAGE_SIZE_PARAM,
                          required = false,
                          defaultValue = PAGE_SIZE_DEFAULT_VALUE) Integer pageSize,
            @RequestParam(value = PAGE_NUMBER_PARAM,
                          required = false,
                          defaultValue = PAGE_NUMBER_DEFAULT_VALUE) Integer pageNumber,
            @RequestParam(value = SORT_COLUMN_PARAM,
                          required = false,
                          defaultValue = SORT_COLUMN_DEFAULT_VALUE) String sortColumn,
            @RequestParam(value = SORT_ORDER_PARAM,
                          required = false,
                          defaultValue = SORT_ORDER_DEFAULT_VALUE) @Pattern(regexp = SORT_ORDER_PATTERN) String sortOrder) {
        try {
            return getScopes(GetElementsInputMapper.map(getAuthenticatedClient(auth),
                                                        SearchQueryDto.decodeFromSearchId(searchId),
                                                        PagingMapper.toConfig(pageSize, pageNumber,
                                                                              sortColumn,
                                                                              sortOrder)));
        } catch (IOException e) {
            log.error("Could not decode search URL: {}", e.getLocalizedMessage());
            return null;
        }
    }
}
