package com.erp.Enterprise_Resource_Planning.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request to change the authenticated user's own password")
public class ChangePasswordRequest {

    @NotBlank
    @Schema(description = "Current password (employees: emp_code on first login)", example = "EMP-0001")
    private String currentPassword;

    @NotBlank
    @Size(min = 6, message = "New password must be at least 6 characters")
    @Schema(description = "New password – minimum 6 characters", example = "myNewPass123")
    private String newPassword;
}
