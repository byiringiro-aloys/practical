package com.erp.Enterprise_Resource_Planning.controller;

import com.erp.Enterprise_Resource_Planning.dto.ApiResponse;
import com.erp.Enterprise_Resource_Planning.dto.AuthResponse;
import com.erp.Enterprise_Resource_Planning.dto.ChangePasswordRequest;
import com.erp.Enterprise_Resource_Planning.dto.LoginRequest;
import com.erp.Enterprise_Resource_Planning.dto.RegisterRequest;
import com.erp.Enterprise_Resource_Planning.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Login for all users; account management")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Public entry point for every role (ADMIN, MANAGER, EMPLOYEE).
     * Employees log in with their email and default password (their emp_code).
     */
    @PostMapping("/login")
    @Operation(summary = "Login – returns a JWT token (all roles)")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful.", response));
    }

    /**
     * ADMIN-only endpoint to create new ADMIN or MANAGER accounts.
     */
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create an ADMIN or MANAGER account (ADMIN only)")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Account created successfully.", response));
    }

    /**
     * Any authenticated user can change their own password.
     * Employees should use this to replace the default emp_code password on first login.
     */
    @PutMapping("/change-password")
    @Operation(summary = "Change own password (all authenticated users)")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(principal.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.ok("Password changed successfully."));
    }
}
