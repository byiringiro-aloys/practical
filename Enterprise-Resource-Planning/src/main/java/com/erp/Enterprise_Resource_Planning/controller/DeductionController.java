package com.erp.Enterprise_Resource_Planning.controller;

import com.erp.Enterprise_Resource_Planning.dto.ApiResponse;
import com.erp.Enterprise_Resource_Planning.dto.DeductionRequest;
import com.erp.Enterprise_Resource_Planning.dto.DeductionResponse;
import com.erp.Enterprise_Resource_Planning.service.DeductionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deductions")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Deductions & Taxes")
public class DeductionController {

    private final DeductionService deductionService;

    public DeductionController(DeductionService deductionService) {
        this.deductionService = deductionService;
    }

    // ─────────────────────────────────────────────────────────────────────
    // POST /api/deductions
    // ─────────────────────────────────────────────────────────────────────

    @PostMapping
    @Operation(
        summary     = "Create deduction type",
        description = """
            Creates a named deduction with a percentage rate applied against base salary during payroll.

            **Default deductions seeded on startup:**
            | Name | Rate |
            |------|------|
            | EmployeeTax | 30% |
            | Pension | 6% |
            | MedicalInsurance | 5% |
            | Others | 5% |
            | House | 14% (allowance – added to gross) |
            | Transport | 14% (allowance – added to gross) |

            **Requires:** ADMIN role.
            """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201", description = "Deduction created",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                               schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "Validation error"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Not authenticated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Insufficient role – ADMIN required"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409", description = "Deduction name already exists")
    })
    public ResponseEntity<ApiResponse<DeductionResponse>> create(
            @Valid @RequestBody DeductionRequest request) {
        DeductionResponse response = deductionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Deduction created.", response));
    }

    // ─────────────────────────────────────────────────────────────────────
    // GET /api/deductions
    // ─────────────────────────────────────────────────────────────────────

    @GetMapping
    @Operation(
        summary     = "List all deduction types",
        description = "Returns all configured deduction and tax rates. Requires ADMIN role."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Deductions retrieved",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                               schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Not authenticated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Insufficient role – ADMIN required")
    })
    public ResponseEntity<ApiResponse<List<DeductionResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok("Deductions retrieved.", deductionService.getAll()));
    }

    // ─────────────────────────────────────────────────────────────────────
    // GET /api/deductions/{id}
    // ─────────────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @Operation(summary = "Get deduction by ID", description = "Returns a single deduction type by its database ID. Requires ADMIN role.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Deduction retrieved",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                               schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Not authenticated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Insufficient role – ADMIN required"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "Deduction not found")
    })
    public ResponseEntity<ApiResponse<DeductionResponse>> getOne(
            @Parameter(description = "Deduction database ID", example = "1", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Deduction retrieved.", deductionService.getById(id)));
    }

    // ─────────────────────────────────────────────────────────────────────
    // PUT /api/deductions/{id}
    // ─────────────────────────────────────────────────────────────────────

    @PutMapping("/{id}")
    @Operation(summary = "Update deduction type", description = "Updates the name or percentage of a deduction. Requires ADMIN role.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Deduction updated",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                               schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "Validation error"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Not authenticated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Insufficient role – ADMIN required"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "Deduction not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409", description = "Deduction name already in use by another record")
    })
    public ResponseEntity<ApiResponse<DeductionResponse>> update(
            @Parameter(description = "Deduction database ID", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody DeductionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Deduction updated.", deductionService.update(id, request)));
    }

    // ─────────────────────────────────────────────────────────────────────
    // DELETE /api/deductions/{id}
    // ─────────────────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete deduction type", description = "Permanently removes a deduction configuration. Requires ADMIN role.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Deduction deleted"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Not authenticated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Insufficient role – ADMIN required"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "Deduction not found")
    })
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "Deduction database ID", example = "1", required = true)
            @PathVariable Long id) {
        deductionService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Deduction deleted."));
    }
}
