package com.andresyfr.franchise.infrastructure.entrypoint.rest.franchise;

import com.andresyfr.franchise.domain.model.Franchise;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Validated request body for franchise renaming. @param name proposed new name
 */
public record RenameFranchiseRequest(
        @Schema(example = "Franquicia Andina")
        @NotBlank
        @Size(max = Franchise.MAX_NAME_LENGTH)
        String name) {
}
