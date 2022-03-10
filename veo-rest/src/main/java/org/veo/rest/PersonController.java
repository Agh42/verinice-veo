/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2020  Jochen Kemnade.
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
import static org.veo.rest.ControllerConstants.UUID_DESCRIPTION;
import static org.veo.rest.ControllerConstants.UUID_EXAMPLE;
import static org.veo.rest.ControllerConstants.UUID_PARAM;
import static org.veo.rest.ControllerConstants.UUID_REGEX;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

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
import org.springframework.web.context.request.WebRequest;

import com.github.JanLoebel.jsonschemavalidation.JsonSchemaValidation;

import org.veo.adapter.IdRefResolver;
import org.veo.adapter.presenter.api.common.ApiResponseBody;
import org.veo.adapter.presenter.api.dto.PageDto;
import org.veo.adapter.presenter.api.dto.SearchQueryDto;
import org.veo.adapter.presenter.api.dto.create.CreatePersonDto;
import org.veo.adapter.presenter.api.dto.full.FullPersonDto;
import org.veo.adapter.presenter.api.io.mapper.CreateOutputMapper;
import org.veo.adapter.presenter.api.io.mapper.GetElementsInputMapper;
import org.veo.adapter.presenter.api.io.mapper.PagingMapper;
import org.veo.core.entity.Client;
import org.veo.core.entity.Key;
import org.veo.core.entity.Person;
import org.veo.core.usecase.base.CreateElementUseCase;
import org.veo.core.usecase.base.DeleteElementUseCase;
import org.veo.core.usecase.base.GetElementsUseCase;
import org.veo.core.usecase.base.ModifyElementUseCase;
import org.veo.core.usecase.base.ModifyElementUseCase.InputData;
import org.veo.core.usecase.person.CreatePersonUseCase;
import org.veo.core.usecase.person.GetPersonUseCase;
import org.veo.core.usecase.person.GetPersonsUseCase;
import org.veo.core.usecase.person.UpdatePersonUseCase;
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
 * REST service which provides methods to manage persons.
 */
@RestController
@RequestMapping(PersonController.URL_BASE_PATH)
@Slf4j
public class PersonController extends AbstractElementController<Person, FullPersonDto> {

    public static final String URL_BASE_PATH = "/" + Person.PLURAL_TERM;

    private final CreatePersonUseCase createPersonUseCase;
    private final GetPersonsUseCase getPersonsUseCase;
    private final UpdatePersonUseCase updatePersonUseCase;
    private final DeleteElementUseCase deleteElementUseCase;

    public PersonController(CreatePersonUseCase createPersonUseCase,
            GetPersonUseCase getPersonUseCase, GetPersonsUseCase getPersonsUseCase,
            UpdatePersonUseCase updatePersonUseCase, DeleteElementUseCase deleteElementUseCase) {
        super(Person.class, getPersonUseCase);
        this.createPersonUseCase = createPersonUseCase;
        this.getPersonsUseCase = getPersonsUseCase;
        this.updatePersonUseCase = updatePersonUseCase;
        this.deleteElementUseCase = deleteElementUseCase;
    }

    @GetMapping
    @Operation(summary = "Loads all persons")
    public @Valid CompletableFuture<PageDto<FullPersonDto>> getPersons(
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

        return getPersons(GetElementsInputMapper.map(client, unitUuid, displayName, subType, status,
                                                     description, designator, name, updatedBy,
                                                     PagingMapper.toConfig(pageSize, pageNumber,
                                                                           sortColumn, sortOrder)));
    }

    private CompletableFuture<PageDto<FullPersonDto>> getPersons(
            GetElementsUseCase.InputData inputData) {
        return useCaseInteractor.execute(getPersonsUseCase, inputData,
                                         output -> PagingMapper.toPage(output.getElements(),
                                                                       entityToDtoTransformer::transformPerson2Dto));
    }

    @Override
    @Operation(summary = "Loads a person")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                         description = "Person loaded",
                         content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = FullPersonDto.class))),
            @ApiResponse(responseCode = "404", description = "Person not found") })
    @GetMapping(ControllerConstants.UUID_PARAM_SPEC)
    public @Valid CompletableFuture<ResponseEntity<FullPersonDto>> getElement(
            @Parameter(required = false, hidden = true) Authentication auth,
            @Parameter(required = true,
                       example = UUID_EXAMPLE,
                       description = UUID_DESCRIPTION) @PathVariable String uuid,
            WebRequest request) {
        return super.getElement(auth, uuid, request);
    }

    @Override
    @Operation(summary = "Loads the parts of a person")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                         description = "Parts loaded",
                         content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            array = @ArraySchema(schema = @Schema(implementation = FullPersonDto.class)))),
            @ApiResponse(responseCode = "404", description = "Person not found") })
    @GetMapping(value = "/{" + UUID_PARAM + ":" + UUID_REGEX + "}/parts")
    public @Valid CompletableFuture<ResponseEntity<List<FullPersonDto>>> getElementParts(
            @Parameter(required = false, hidden = true) Authentication auth,
            @Parameter(required = true,
                       example = UUID_EXAMPLE,
                       description = UUID_DESCRIPTION) @PathVariable String uuid,
            WebRequest request) {
        return super.getElementParts(auth, uuid, request);
    }

    @PostMapping()
    @Operation(summary = "Creates a person")
    @ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Person created") })
    public CompletableFuture<ResponseEntity<ApiResponseBody>> createPerson(
            @Parameter(hidden = true) ApplicationUser user,
            @Valid @NotNull @RequestBody @JsonSchemaValidation(Person.SINGULAR_TERM) CreatePersonDto dto) {
        return useCaseInteractor.execute(createPersonUseCase,
                                         (Supplier<CreateElementUseCase.InputData<Person>>) () -> {
                                             Client client = getClient(user);
                                             IdRefResolver idRefResolver = createIdRefResolver(client);
                                             return new CreateElementUseCase.InputData<>(
                                                     dtoToEntityTransformer.transformDto2Person(dto,
                                                                                                idRefResolver),
                                                     client);
                                         }, output -> {
                                             ApiResponseBody body = CreateOutputMapper.map(output.getEntity());
                                             return RestApiResponse.created(URL_BASE_PATH, body);
                                         });
    }

    @PutMapping(ControllerConstants.UUID_PARAM_SPEC)
    @Operation(summary = "Updates a person")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Person updated"),
            @ApiResponse(responseCode = "404", description = "Person not found") })
    public CompletableFuture<FullPersonDto> updatePerson(
            @Parameter(hidden = true) ApplicationUser user,
            @RequestHeader(ControllerConstants.IF_MATCH_HEADER) @NotBlank String eTag,
            @Parameter(required = true,
                       example = UUID_EXAMPLE,
                       description = UUID_DESCRIPTION) @PathVariable String uuid,
            @Valid @NotNull @RequestBody @JsonSchemaValidation(Person.SINGULAR_TERM) FullPersonDto personDto) {
        personDto.applyResourceId(uuid);
        return useCaseInteractor.execute(updatePersonUseCase, new Supplier<InputData<Person>>() {
            @Override
            public InputData<Person> get() {
                Client client = getClient(user);
                IdRefResolver idRefResolver = createIdRefResolver(client);
                return new ModifyElementUseCase.InputData<>(
                        dtoToEntityTransformer.transformDto2Person(personDto, idRefResolver),
                        client, eTag, user.getUsername());
            }
        },

                                         output -> entityToDtoTransformer.transformPerson2Dto(output.getEntity()));
    }

    @DeleteMapping(ControllerConstants.UUID_PARAM_SPEC)
    @Operation(summary = "Deletes a person")
    @ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Person deleted"),
            @ApiResponse(responseCode = "404", description = "Person not found") })
    public CompletableFuture<ResponseEntity<ApiResponseBody>> deletePerson(
            @Parameter(required = false, hidden = true) Authentication auth,
            @Parameter(required = true,
                       example = UUID_EXAMPLE,
                       description = UUID_DESCRIPTION) @PathVariable String uuid) {
        Client client = getAuthenticatedClient(auth);
        return useCaseInteractor.execute(deleteElementUseCase,
                                         new DeleteElementUseCase.InputData(Person.class,
                                                 Key.uuidFrom(uuid), client),
                                         output -> ResponseEntity.noContent()
                                                                 .build());
    }

    @Override
    @SuppressFBWarnings("NP_NULL_PARAM_DEREF_ALL_TARGETS_DANGEROUS")
    protected String buildSearchUri(String id) {
        return linkTo(methodOn(PersonController.class).runSearch(ANY_AUTH, id, ANY_INT, ANY_INT,
                                                                 ANY_STRING, ANY_STRING))
                                                                                         .withSelfRel()
                                                                                         .getHref();
    }

    @GetMapping(value = "/searches/{searchId}")
    @Operation(summary = "Finds persons for the search.")
    public @Valid CompletableFuture<PageDto<FullPersonDto>> runSearch(
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
            return getPersons(GetElementsInputMapper.map(getAuthenticatedClient(auth),
                                                         SearchQueryDto.decodeFromSearchId(searchId),
                                                         PagingMapper.toConfig(pageSize, pageNumber,
                                                                               sortColumn,
                                                                               sortOrder)));
        } catch (IOException e) {
            log.error("Could not decode search URL: {}", e.getLocalizedMessage());
            return null;
        }
    }

    @Override
    protected FullPersonDto entity2Dto(Person entity) {
        return entityToDtoTransformer.transformPerson2Dto(entity);
    }
}
