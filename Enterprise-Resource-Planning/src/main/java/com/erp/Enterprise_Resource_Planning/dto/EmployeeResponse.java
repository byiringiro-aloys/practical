package com.erp.Enterprise_Resource_Planning.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@Schema(description = "Employee personal and employment details")
public class EmployeeResponse {

    @Schema(description = "Internal database ID", example = "1")
    private Long id;

    // ── Personal ──────────────────────────────────────────────────────────
    @Schema(example = "Peter")
    private String firstName;

    @Schema(example = "Mugisha")
    private String lastName;

    @Schema(example = "peter.mugisha@erp.rw")
    private String email;

    @Schema(example = "Kigali")
    private String district;

    @Schema(example = "+250788000010")
    private String mobile;

    @Schema(example = "1990-03-15")
    private LocalDate dateOfBirth;

    // ── Employment ────────────────────────────────────────────────────────
    @Schema(description = "Human-readable employee code", example = "EMP-0010")
    private String employeeId;

    @Schema(example = "Finance")
    private String department;

    @Schema(example = "Finance Officer")
    private String position;

    @Schema(description = "Base salary in RWF", example = "700000.00")
    private BigDecimal salary;

    @Schema(example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE"})
    private String status;

    @Schema(example = "2020-01-10")
    private LocalDate joiningDate;
}
