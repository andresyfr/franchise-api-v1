package com.andresyfr.franchise.infrastructure.entrypoint.rest.branch;

import com.andresyfr.franchise.domain.model.Branch;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body to rename a branch.
 *
 * @param name proposed new branch name
 */
public record RenameBranchRequest(
        @Schema(example = "Sucursal El Poblado")
        @NotBlank
        @Size(max = Branch.MAX_NAME_LENGTH)
        String name) {
}
