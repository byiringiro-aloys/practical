package com.erp.Enterprise_Resource_Planning.controller;

import com.erp.Enterprise_Resource_Planning.dto.ApiResponse;
import com.erp.Enterprise_Resource_Planning.dto.DeductionRequest;
import com.erp.Enterprise_Resource_Planning.dto.DeductionResponse;
import com.erp.Enterprise_Resource_Planning.service.DeductionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deductions")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Deductions & Taxes", description = "Manage configurable deduction rates (ADMIN only)")
public class DeductionController {

    private final DeductionService deductionService;

    public DeductionController(DeductionService deductionService) {
        this.deductionService = deductionService;
    }

    @PostMapping
    @Operation(summary = "Create a deduction type with its percentage")
    public ResponseEntity<ApiResponse<DeductionResponse>> create(@Valid @RequestBody DeductionRequest request) {
        DeductionResponse response = deductionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Deduction created.", response));
    }

    @GetMapping
    @Operation(summary = "List all deduction types")
    public ResponseEntity<ApiResponse<List<DeductionResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok("Deductions retrieved.", deductionService.getAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a deduction by ID")
    public ResponseEntity<ApiResponse<DeductionResponse>> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Deduction retrieved.", deductionService.getById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a deduction type")
    public ResponseEntity<ApiResponse<DeductionResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody DeductionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Deduction updated.", deductionService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a deduction type")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        deductionService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Deduction deleted."));
    }
}
