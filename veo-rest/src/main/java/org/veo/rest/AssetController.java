/*******************************************************************************
 * Copyright (c) 2019 Daniel Murygin.
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
package org.veo.rest;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.veo.rest.ControllerConstants.ANY_AUTH;
import static org.veo.rest.ControllerConstants.DISPLAY_NAME_PARAM;
import static org.veo.rest.ControllerConstants.SUB_TYPE_PARAM;
import static org.veo.rest.ControllerConstants.UNIT_PARAM;
import static org.veo.rest.ControllerConstants.UUID_PARAM;
import static org.veo.rest.ControllerConstants.UUID_REGEX;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
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

import org.veo.adapter.ModelObjectReferenceResolver;
import org.veo.adapter.presenter.api.common.ApiResponseBody;
import org.veo.adapter.presenter.api.common.ReferenceAssembler;
import org.veo.adapter.presenter.api.dto.EntityLayerSupertypeDto;
import org.veo.adapter.presenter.api.dto.SearchQueryDto;
import org.veo.adapter.presenter.api.dto.create.CreateAssetDto;
import org.veo.adapter.presenter.api.dto.full.AssetRiskDto;
import org.veo.adapter.presenter.api.dto.full.FullAssetDto;
import org.veo.adapter.presenter.api.io.mapper.CreateOutputMapper;
import org.veo.adapter.presenter.api.io.mapper.GetEntitiesInputMapper;
import org.veo.core.entity.Asset;
import org.veo.core.entity.Client;
import org.veo.core.entity.EntityTypeNames;
import org.veo.core.entity.Key;
import org.veo.core.usecase.UseCaseInteractor;
import org.veo.core.usecase.asset.CreateAssetRiskUseCase;
import org.veo.core.usecase.asset.CreateAssetUseCase;
import org.veo.core.usecase.asset.DeleteAssetRiskUseCase;
import org.veo.core.usecase.asset.GetAssetRiskUseCase;
import org.veo.core.usecase.asset.GetAssetRisksUseCase;
import org.veo.core.usecase.asset.GetAssetUseCase;
import org.veo.core.usecase.asset.GetAssetsUseCase;
import org.veo.core.usecase.asset.UpdateAssetRiskUseCase;
import org.veo.core.usecase.asset.UpdateAssetUseCase;
import org.veo.core.usecase.base.CreateEntityUseCase;
import org.veo.core.usecase.base.DeleteEntityUseCase;
import org.veo.core.usecase.base.GetEntitiesUseCase;
import org.veo.core.usecase.base.ModifyEntityUseCase;
import org.veo.core.usecase.base.ModifyEntityUseCase.InputData;
import org.veo.core.usecase.common.ETag;
import org.veo.rest.annotations.ParameterUuid;
import org.veo.rest.annotations.UnitUuidParam;
import org.veo.rest.common.RestApiResponse;
import org.veo.rest.security.ApplicationUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;

/**
 * REST service which provides methods to manage assets.
 */
@RestController
@RequestMapping(AssetController.URL_BASE_PATH)
@Slf4j
public class AssetController extends AbstractEntityController implements AssetRiskResource {

    private final DeleteAssetRiskUseCase deleteAssetRiskUseCase;
    private final UpdateAssetRiskUseCase updateAssetRiskUseCase;
    @Autowired
    ReferenceAssembler urlAssembler;
    private final GetAssetRisksUseCase getAssetRisksUseCase;

    public AssetController(UseCaseInteractor useCaseInteractor, GetAssetUseCase getAssetUseCase,
            GetAssetsUseCase getAssetsUseCase, CreateAssetUseCase createAssetUseCase,
            UpdateAssetUseCase updateAssetUseCase, DeleteEntityUseCase deleteEntityUseCase,
            CreateAssetRiskUseCase createAssetRiskUseCase, GetAssetRiskUseCase getAssetRiskUseCase,
            DeleteAssetRiskUseCase deleteAssetRiskUseCase,
            UpdateAssetRiskUseCase updateAssetRiskUseCase,
            GetAssetRisksUseCase getAssetRisksUseCase) {
        this.useCaseInteractor = useCaseInteractor;
        this.getAssetUseCase = getAssetUseCase;
        this.getAssetsUseCase = getAssetsUseCase;
        this.createAssetUseCase = createAssetUseCase;
        this.updateAssetUseCase = updateAssetUseCase;
        this.deleteEntityUseCase = deleteEntityUseCase;
        this.createAssetRiskUseCase = createAssetRiskUseCase;
        this.getAssetRiskUseCase = getAssetRiskUseCase;
        this.deleteAssetRiskUseCase = deleteAssetRiskUseCase;
        this.updateAssetRiskUseCase = updateAssetRiskUseCase;
        this.getAssetRisksUseCase = getAssetRisksUseCase;
    }

    public static final String URL_BASE_PATH = "/" + EntityTypeNames.ASSETS;

    private final UseCaseInteractor useCaseInteractor;
    private final CreateAssetUseCase createAssetUseCase;
    private final UpdateAssetUseCase updateAssetUseCase;
    private final GetAssetUseCase getAssetUseCase;
    private final GetAssetsUseCase getAssetsUseCase;
    private final DeleteEntityUseCase deleteEntityUseCase;
    private final CreateAssetRiskUseCase createAssetRiskUseCase;
    private final GetAssetRiskUseCase getAssetRiskUseCase;

    @GetMapping
    @Operation(summary = "Loads all assets")
    public @Valid CompletableFuture<List<FullAssetDto>> getAssets(
            @Parameter(required = false, hidden = true) Authentication auth,
            @UnitUuidParam @RequestParam(value = UNIT_PARAM, required = false) String unitUuid,
            @UnitUuidParam @RequestParam(value = DISPLAY_NAME_PARAM,
                                         required = false) String displayName,
            @RequestParam(value = SUB_TYPE_PARAM, required = false) String subType) {
        Client client = null;
        try {
            client = getAuthenticatedClient(auth);
        } catch (NoSuchElementException e) {
            return CompletableFuture.supplyAsync(Collections::emptyList);
        }

        return getAssets(GetEntitiesInputMapper.map(client, unitUuid, displayName, subType));
    }

    private CompletableFuture<List<FullAssetDto>> getAssets(
            GetEntitiesUseCase.InputData inputData) {
        return useCaseInteractor.execute(getAssetsUseCase, inputData, output -> output.getEntities()
                                                                                      .stream()
                                                                                      .map(a -> entityToDtoTransformer.transformAsset2Dto(a))
                                                                                      .collect(Collectors.toList()));
    }

    @GetMapping(value = "/{id}")
    @Operation(summary = "Loads an asset")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                         description = "Asset loaded",
                         content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = FullAssetDto.class))),
            @ApiResponse(responseCode = "404", description = "Asset not found") })
    public @Valid CompletableFuture<ResponseEntity<FullAssetDto>> getAsset(
            @Parameter(required = false, hidden = true) Authentication auth,
            @PathVariable String id) {
        ApplicationUser user = ApplicationUser.authenticatedUser(auth.getPrincipal());
        Client client = getClient(user.getClientId());

        CompletableFuture<FullAssetDto> assetFuture = useCaseInteractor.execute(getAssetUseCase,
                                                                                new GetAssetUseCase.InputData(
                                                                                        Key.uuidFrom(id),
                                                                                        client),
                                                                                output -> {
                                                                                    return entityToDtoTransformer.transformAsset2Dto(output.getAsset());
                                                                                });

        return assetFuture.thenApply(assetDto -> ResponseEntity.ok()
                                                               .eTag(ETag.from(assetDto.getId(),
                                                                               assetDto.getVersion()))
                                                               .body(assetDto));
    }

    @GetMapping(value = "/{" + UUID_PARAM + ":" + UUID_REGEX + "}/parts")
    @Operation(summary = "Loads the parts of an asset")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                         description = "Parts loaded",
                         content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            array = @ArraySchema(schema = @Schema(implementation = FullAssetDto.class)))),
            @ApiResponse(responseCode = "404", description = "Asset not found") })
    public @Valid CompletableFuture<List<EntityLayerSupertypeDto>> getParts(
            @Parameter(required = false, hidden = true) Authentication auth,
            @ParameterUuid @PathVariable(UUID_PARAM) String uuid) {
        Client client = getAuthenticatedClient(auth);
        return useCaseInteractor.execute(getAssetUseCase,
                                         new GetAssetUseCase.InputData(Key.uuidFrom(uuid), client),
                                         output -> {
                                             Asset scope = output.getAsset();
                                             return scope.getParts()
                                                         .stream()
                                                         .map(part -> entityToDtoTransformer.transform2Dto(part))
                                                         .collect(Collectors.toList());
                                         });
    }

    @PostMapping()
    @Operation(summary = "Creates an asset")
    @ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Asset created") })
    public CompletableFuture<ResponseEntity<ApiResponseBody>> createAsset(
            @Parameter(hidden = true) ApplicationUser user,
            @Valid @NotNull @RequestBody CreateAssetDto dto) {

        return useCaseInteractor.execute(createAssetUseCase,
                                         (Supplier<CreateEntityUseCase.InputData<Asset>>) () -> {
                                             Client client = getClient(user);
                                             ModelObjectReferenceResolver modelObjectReferenceResolver = createModelObjectReferenceResolver(client);
                                             return new CreateEntityUseCase.InputData<>(
                                                     dtoToEntityTransformer.transformDto2Asset(dto,
                                                                                               null,
                                                                                               modelObjectReferenceResolver),
                                                     client, user.getUsername());
                                         }, output -> {
                                             ApiResponseBody body = CreateOutputMapper.map(output.getEntity());
                                             return RestApiResponse.created(URL_BASE_PATH, body);
                                         });
    }

    @PutMapping(value = "/{id}")
    @Operation(summary = "Updates an asset")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Asset updated"),
            @ApiResponse(responseCode = "404", description = "Asset not found") })
    public CompletableFuture<FullAssetDto> updateAsset(
            @Parameter(hidden = true) ApplicationUser user,
            @RequestHeader(ControllerConstants.IF_MATCH_HEADER) @NotBlank String eTag,
            @PathVariable String id, @Valid @NotNull @RequestBody FullAssetDto assetDto) {
        applyId(id, assetDto);
        return useCaseInteractor.execute(updateAssetUseCase,
                                         new Supplier<ModifyEntityUseCase.InputData<Asset>>() {

                                             @Override
                                             public InputData<Asset> get() {
                                                 Client client = getClient(user);
                                                 ModelObjectReferenceResolver modelObjectReferenceResolver = createModelObjectReferenceResolver(client);
                                                 return new ModifyEntityUseCase.InputData<Asset>(
                                                         dtoToEntityTransformer.transformDto2Asset(assetDto,
                                                                                                   Key.uuidFrom(assetDto.getId()),
                                                                                                   modelObjectReferenceResolver),
                                                         client, eTag, user.getUsername());
                                             }
                                         }

                                         ,
                                         output -> entityToDtoTransformer.transformAsset2Dto(output.getEntity()));
    }

    @DeleteMapping(value = "/{" + UUID_PARAM + ":" + UUID_REGEX + "}")
    @Operation(summary = "Deletes an asset")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Asset deleted"),
            @ApiResponse(responseCode = "404", description = "Asset not found") })
    public CompletableFuture<ResponseEntity<ApiResponseBody>> deleteAsset(
            @Parameter(required = false, hidden = true) Authentication auth,
            @ParameterUuid @PathVariable(UUID_PARAM) String uuid) {
        ApplicationUser user = ApplicationUser.authenticatedUser(auth.getPrincipal());
        Client client = getClient(user.getClientId());
        return useCaseInteractor.execute(deleteEntityUseCase,
                                         new DeleteEntityUseCase.InputData(Asset.class,
                                                 Key.uuidFrom(uuid), client),
                                         output -> ResponseEntity.ok()
                                                                 .build());
    }

    @Override
    protected String buildSearchUri(String id) {
        return linkTo(methodOn(AssetController.class).runSearch(ANY_AUTH, id)).withSelfRel()
                                                                              .getHref();
    }

    @GetMapping(value = "/searches/{searchId}")
    @Operation(summary = "Finds assets for the search.")
    public @Valid CompletableFuture<List<FullAssetDto>> runSearch(
            @Parameter(required = false, hidden = true) Authentication auth,
            @PathVariable String searchId) {
        try {
            return getAssets(GetEntitiesInputMapper.map(getAuthenticatedClient(auth),
                                                        SearchQueryDto.decodeFromSearchId(searchId)));
        } catch (IOException e) {
            log.error(String.format("Could not decode search URL: %s", e.getLocalizedMessage()));
            return null;
        }
    }

    @Override
    public @Valid CompletableFuture<List<AssetRiskDto>> getRisks(
            @Parameter(hidden = true) ApplicationUser user, String assetId) {

        Client client = getClient(user.getClientId());
        var input = new GetAssetRisksUseCase.InputData(client, Key.uuidFrom(assetId));

        return useCaseInteractor.execute(getAssetRisksUseCase, input, output -> {
            return output.getAssetRisks()
                         .stream()
                         .map(risk -> AssetRiskDto.from(risk, referenceAssembler))
                         .collect(Collectors.toList());
        });
    }

    @Override
    public @Valid CompletableFuture<ResponseEntity<AssetRiskDto>> getRisk(
            @Parameter(hidden = true) ApplicationUser user, String assetId, String scenarioId) {

        Client client = getClient(user.getClientId());
        var input = new GetAssetRiskUseCase.InputData(client, Key.uuidFrom(assetId),
                Key.uuidFrom(scenarioId));

        var riskFuture = useCaseInteractor.execute(getAssetRiskUseCase, input, output -> {
            return AssetRiskDto.from(output.getAssetRisk(), referenceAssembler);
        });

        return riskFuture.thenApply(riskDto -> ResponseEntity.ok()
                                                             .eTag(ETag.from(riskDto.getAsset()
                                                                                    .getId(),
                                                                             riskDto.getScenario()
                                                                                    .getId(),
                                                                             riskDto.getVersion()))
                                                             .body(riskDto));
    }

    @Override
    public CompletableFuture<ResponseEntity<ApiResponseBody>> createRisk(ApplicationUser user,
            @Valid @NotNull AssetRiskDto dto, String assetId) {

        var input = new CreateAssetRiskUseCase.InputData(getClient(user.getClientId()),
                Key.uuidFrom(assetId), urlAssembler.toKey(dto.getScenario()),
                urlAssembler.toKeys(dto.getDomains()), urlAssembler.toKey(dto.getMitigation()),
                urlAssembler.toKey(dto.getRiskOwner()));

        return useCaseInteractor.execute(createAssetRiskUseCase, input, output -> {
            var url = String.format("%s/%s/%s", URL_BASE_PATH, output.getAssetRisk()
                                                                     .getAsset()
                                                                     .getId()
                                                                     .uuidValue(),
                                    AssetRiskResource.RESOURCE_NAME);
            var body = new ApiResponseBody(true, Optional.of(output.getAssetRisk()
                                                                   .getScenario()
                                                                   .getId()
                                                                   .uuidValue()),
                    "Asset risk created successfully.", "");
            return RestApiResponse.created(url, body);
        });
    }

    @Override
    public CompletableFuture<ResponseEntity<ApiResponseBody>> deleteRisk(ApplicationUser user,
            String assetId, String scenarioId) {

        Client client = getClient(user.getClientId());
        var input = new DeleteAssetRiskUseCase.InputData(client, Key.uuidFrom(assetId),
                Key.uuidFrom(scenarioId));

        return useCaseInteractor.execute(deleteAssetRiskUseCase, input,
                                         output -> ResponseEntity.ok()
                                                                 .build());
    }

    @Override
    public @Valid CompletableFuture<ResponseEntity<AssetRiskDto>> updateRisk(ApplicationUser user,
            String assetId, String scenarioId, @Valid @NotNull AssetRiskDto dto, String eTag) {

        var input = new UpdateAssetRiskUseCase.InputData(getClient(user.getClientId()),
                Key.uuidFrom(assetId), urlAssembler.toKey(dto.getScenario()),
                urlAssembler.toKeys(dto.getDomains()), urlAssembler.toKey(dto.getMitigation()),
                urlAssembler.toKey(dto.getRiskOwner()), eTag);

        // update risk and return saved risk with updated ETag, timestamps etc.:
        return useCaseInteractor.execute(updateAssetRiskUseCase, input, output -> null)
                                .thenCompose(o -> this.getRisk(user, assetId, scenarioId));
    }
}
