package com.erp.Enterprise_Resource_Planning.controller;

import com.erp.Enterprise_Resource_Planning.dto.ApiResponse;
import com.erp.Enterprise_Resource_Planning.dto.AuthResponse;
import com.erp.Enterprise_Resource_Planning.dto.LoginRequest;
import com.erp.Enterprise_Resource_Planning.dto.RegisterRequest;
import com.erp.Enterprise_Resource_Planning.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
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

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ─────────────────────────────────────────────────────────────────────
    // POST /api/auth/login
    // ─────────────────────────────────────────────────────────────────────

    @PostMapping("/login")
    @Operation(
        summary     = "Login",
        description = """
            Authenticate with email and password to receive a JWT Bearer token.

            **Seeded accounts for testing:**
            | Email | Password | Role |
            |-------|----------|------|
            | admin@erp.rw | admin123 | ADMIN |
            | manager@erp.rw | manager123 | MANAGER |
            | mugabo.javis@erp.rw | EMP-0001 | EMPLOYEE |
            | michou.michell@erp.rw | EMP-0002 | EMPLOYEE |

            Copy the `token` from the response, click **Authorize** at the top of this page,
            and paste `Bearer <token>` to unlock all secured endpoints.
            """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Login successful – JWT token returned",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                               schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Invalid email or password",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                               schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful.", response));
    }

    // ─────────────────────────────────────────────────────────────────────
    // POST /api/auth/register
    // ─────────────────────────────────────────────────────────────────────

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary     = "Register admin or manager account",
        description = """
            Create a new **ADMIN** or **MANAGER** user account. Requires ADMIN role.

            > **Note:** EMPLOYEE accounts are created automatically when ADMIN adds an employee
            via `POST /api/employees`. Their default password is their employee code (e.g. `EMP-0001`).
            Sending `role: EMPLOYEE` here will be rejected.
            """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201", description = "Account created successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                               schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "Validation error or EMPLOYEE role submitted",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                               schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Not authenticated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Insufficient role – ADMIN required"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409", description = "Email already registered",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                               schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Account created successfully.", response));
    }
}
