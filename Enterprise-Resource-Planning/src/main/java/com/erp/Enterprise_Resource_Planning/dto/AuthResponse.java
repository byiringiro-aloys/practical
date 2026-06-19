package com.erp.Enterprise_Resource_Planning.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "JWT token and basic account info returned after successful login or registration")
public class AuthResponse {

    @Schema(description = "JWT Bearer token – include this in the Authorization header", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "Email of the authenticated account", example = "mugabo.javis@erp.rw")
    private String email;

    @Schema(description = "Role granted to this account", example = "EMPLOYEE", allowableValues = {"ADMIN", "MANAGER", "EMPLOYEE"})
    private String role;
}
