package com.erp.Enterprise_Resource_Planning.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Request to create or update a deduction type")
public class DeductionRequest {

    @NotBlank
    @Schema(description = "Unique deduction name", example = "EmployeeTax",
            allowableValues = {"EmployeeTax", "Pension", "MedicalInsurance", "Others", "House", "Transport"})
    private String name;

    @NotNull
    @DecimalMin("0.01")
    @DecimalMax("100.00")
    @Schema(description = "Deduction rate as a percentage of base salary (0.01 – 100.00)", example = "30.00")
    private BigDecimal percentage;
}
