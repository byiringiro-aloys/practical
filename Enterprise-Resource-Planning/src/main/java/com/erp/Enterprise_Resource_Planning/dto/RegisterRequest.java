package com.erp.Enterprise_Resource_Planning.dto;

import com.erp.Enterprise_Resource_Planning.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Used by ADMIN to create ADMIN or MANAGER accounts only.
 * EMPLOYEE accounts are auto-created when ADMIN registers an employee
 * via POST /api/employees.
 */
@Data
public class RegisterRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    /**
     * Only ADMIN or MANAGER are valid here.
     * Sending EMPLOYEE will be rejected by the service layer.
     */
    @NotNull
    private Role role;
}
