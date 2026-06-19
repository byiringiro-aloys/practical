package com.erp.Enterprise_Resource_Planning.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "Request body to create or update an employee (personal + employment details combined)")
public class EmployeeRequest {

    // ── Personal info ─────────────────────────────────────────────────────

    @NotBlank
    @Schema(description = "Employee's first name", example = "Peter")
    private String firstName;

    @NotBlank
    @Schema(description = "Employee's last name", example = "Mugisha")
    private String lastName;

    @NotBlank
    @Email
    @Schema(description = "Unique email – also used as login username", example = "peter.mugisha@erp.rw")
    private String email;

    @NotBlank
    @Schema(description = "Rwandan district of residence", example = "Kigali")
    private String district;

    @NotBlank
    @Size(min = 10, max = 15)
    @Schema(description = "Mobile number (10-15 chars)", example = "+250788000010")
    private String mobile;

    @NotNull
    @Past
    @Schema(description = "Date of birth (must be in the past)", example = "1990-03-15")
    private LocalDate dateOfBirth;

    // ── Employment info ───────────────────────────────────────────────────

    @NotBlank
    @Schema(description = "Human-readable employee code – also used as default login password", example = "EMP-0010")
    private String employeeId;

    @NotBlank
    @Schema(description = "Department name", example = "Finance")
    private String department;

    @NotBlank
    @Schema(description = "Job title / position", example = "Finance Officer")
    private String position;

    @NotNull
    @DecimalMin("0.01")
    @Schema(description = "Base salary in RWF – used for all payroll computations", example = "700000.00")
    private BigDecimal salary;

    @NotBlank
    @Schema(description = "Employment status", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE"})
    private String status;

    @NotNull
    @Schema(description = "Date the employee joined", example = "2020-01-10")
    private LocalDate joiningDate;
}
