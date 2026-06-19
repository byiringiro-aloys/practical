package com.erp.Enterprise_Resource_Planning.dto;

import com.erp.Enterprise_Resource_Planning.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Request to create an ADMIN or MANAGER account (EMPLOYEE accounts are created via POST /api/employees)")
public class RegisterRequest {

    @NotBlank
    @Email
    @Schema(description = "Email address for the new account", example = "manager2@erp.rw")
    private String email;

    @NotBlank
    @Schema(description = "Account password (min 6 characters)", example = "securePass123")
    private String password;

    @NotNull
    @Schema(description = "Role to assign – only ADMIN or MANAGER allowed here", example = "MANAGER",
            allowableValues = {"ADMIN", "MANAGER"})
    private Role role;
}
