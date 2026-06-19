package com.erp.Enterprise_Resource_Planning.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "Deduction type with its configured rate")
public class DeductionResponse {

    @Schema(description = "Internal ID", example = "1")
    private Long id;

    @Schema(description = "Deduction name", example = "EmployeeTax")
    private String name;

    @Schema(description = "Rate as percentage of base salary", example = "30.00")
    private BigDecimal percentage;
}
