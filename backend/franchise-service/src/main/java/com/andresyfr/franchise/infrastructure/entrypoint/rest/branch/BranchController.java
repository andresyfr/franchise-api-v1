package com.andresyfr.franchise.infrastructure.entrypoint.rest.branch;

import com.andresyfr.franchise.application.usecase.AddBranchUseCase;
import com.andresyfr.franchise.application.usecase.RenameBranchUseCase;
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
 * HTTP adapter for branch commands.
 *
 * <p>The controller validates transport data and delegates every business
 * decision to the use cases.</p>
 */
@Tag(name = "Branches", description = "Branch management")
@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class BranchController {

    private final AddBranchUseCase addBranchUseCase;
    private final RenameBranchUseCase renameBranchUseCase;

    /**
     * Creates the controller.
     *
     * @param addBranchUseCase    use case that adds branches
     * @param renameBranchUseCase use case that renames branches
     */
    public BranchController(AddBranchUseCase addBranchUseCase, RenameBranchUseCase renameBranchUseCase) {
        this.addBranchUseCase = addBranchUseCase;
        this.renameBranchUseCase = renameBranchUseCase;
    }

    /**
     * Adds a branch to a franchise.
     *
     * @param franchiseId identifier of the owning franchise
     * @param request     validated request body
     * @return the created branch with HTTP status 201
     */
    @Operation(summary = "Add a branch to a franchise")
    @PostMapping(value = "/franchises/{franchiseId}/branches", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<BranchResponse> add(@PathVariable String franchiseId,
                                    @Valid @RequestBody AddBranchRequest request) {
        return addBranchUseCase.execute(franchiseId, request.name())
                .map(BranchResponse::from);
    }

    /**
     * Renames an existing branch.
     *
     * @param branchId identifier of the branch
     * @param request  validated request body
     * @return the updated branch
     */
    @Operation(summary = "Rename a branch")
    @PatchMapping(value = "/branches/{branchId}/name", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<BranchResponse> rename(@PathVariable String branchId,
                                       @Valid @RequestBody RenameBranchRequest request) {
        return renameBranchUseCase.execute(branchId, request.name())
                .map(BranchResponse::from);
    }
}
