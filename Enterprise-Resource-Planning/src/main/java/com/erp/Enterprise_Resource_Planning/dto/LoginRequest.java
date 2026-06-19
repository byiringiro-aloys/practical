package com.erp.Enterprise_Resource_Planning.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Credentials for login")
public class LoginRequest {

    @NotBlank
    @Email
    @Schema(description = "Registered email address", example = "mugabo.javis@erp.rw")
    private String email;

    @NotBlank
    @Schema(description = "Password (employees: default is their emp_code, e.g. EMP-0001)", example = "EMP-0001")
    private String password;
}
