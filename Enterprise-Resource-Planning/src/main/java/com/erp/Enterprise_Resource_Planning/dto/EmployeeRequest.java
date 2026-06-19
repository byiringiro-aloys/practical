package com.erp.Enterprise_Resource_Planning.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * Combined request to create an employee together with their employment details.
 */
@Data
public class EmployeeRequest {

    // ── Personal info ────────────────────────────────────────────────────
    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String district;

    @NotBlank
    @Size(min = 10, max = 15)
    private String mobile;

    @NotNull
    @Past
    private LocalDate dateOfBirth;

    // ── Employment info ──────────────────────────────────────────────────
    @NotBlank
    private String employeeId;     // e.g. EMP-0001

    @NotBlank
    private String department;

    @NotBlank
    private String position;

    @NotNull
    @DecimalMin("0.01")
    private java.math.BigDecimal salary;

    @NotBlank
    private String status;         // "ACTIVE" | "INACTIVE"

    @NotNull
    private LocalDate joiningDate;
}
