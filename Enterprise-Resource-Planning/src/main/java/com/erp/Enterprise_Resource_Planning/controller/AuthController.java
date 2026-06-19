package com.erp.Enterprise_Resource_Planning.controller;

import com.erp.Enterprise_Resource_Planning.dto.ApiResponse;
import com.erp.Enterprise_Resource_Planning.dto.AuthResponse;
import com.erp.Enterprise_Resource_Planning.dto.LoginRequest;
import com.erp.Enterprise_Resource_Planning.dto.RegisterRequest;
import com.erp.Enterprise_Resource_Planning.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication endpoints.
 *
 * POST /api/auth/login     – public, returns JWT for all roles
 * POST /api/auth/register  – ADMIN only, creates ADMIN or MANAGER accounts
 *
 * Self-service (profile, password, payslips, messages) → /api/me/*
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Login (all roles) and account registration (ADMIN only)")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login – returns a JWT token (ADMIN / MANAGER / EMPLOYEE)")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful.", response));
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create an ADMIN or MANAGER account (ADMIN only). "
            + "Employee accounts are auto-created via POST /api/employees.")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Account created successfully.", response));
    }
}
