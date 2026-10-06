package com.andresyfr.franchise.infrastructure.entrypoint.rest.franchise;

import com.andresyfr.franchise.application.usecase.CreateFranchiseUseCase;
import com.andresyfr.franchise.application.usecase.RenameFranchiseUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * HTTP adapter for franchise commands.
 * It validates transport data and delegates business decisions to use cases.
 */
@Tag(name = "Franchises", description = "Franchise management")
@RestController
@RequestMapping(value = "/api/v1/franchises", produces = MediaType.APPLICATION_JSON_VALUE)
public class FranchiseController {

    private final CreateFranchiseUseCase createFranchiseUseCase;
    private final RenameFranchiseUseCase renameFranchiseUseCase;

    public FranchiseController(CreateFranchiseUseCase createFranchiseUseCase,
                               RenameFranchiseUseCase renameFranchiseUseCase) {
        this.createFranchiseUseCase = createFranchiseUseCase;
        this.renameFranchiseUseCase = renameFranchiseUseCase;
    }

    /**
     * Creates a franchise.
     *
     * @param request validated body
     * @return created franchise with HTTP status 201
     */
    @Operation(summary = "Create a franchise")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<FranchiseResponse> create(@Valid @RequestBody CreateFranchiseRequest request) {
        return createFranchiseUseCase.execute(request.name())
                .map(FranchiseResponse::from);
    }

    /**
     * Renames an existing franchise.
     *
     * @param franchiseId identifier from the URL
     * @param request     validated body
     * @return updated franchise
     */
    @Operation(summary = "Rename a franchise")
    @PatchMapping(value = "/{franchiseId}/name", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<FranchiseResponse> rename(@PathVariable String franchiseId,
                                          @Valid @RequestBody RenameFranchiseRequest request) {
        return renameFranchiseUseCase.execute(franchiseId, request.name())
                .map(FranchiseResponse::from);
    }
}
