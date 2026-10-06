package com.andresyfr.franchise.infrastructure.entrypoint.rest.branch;

import com.andresyfr.franchise.domain.model.Branch;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body to add a branch to a franchise.
 *
 * @param name proposed branch name
 */
public record AddBranchRequest(
        @Schema(example = "Sucursal Medellín")
        @NotBlank
        @Size(max = Branch.MAX_NAME_LENGTH)
        String name) {
}