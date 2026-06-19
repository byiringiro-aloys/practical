package com.erp.Enterprise_Resource_Planning.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DeductionRequest {

    @NotBlank
    private String name;

    @NotNull
    @DecimalMin("0.01")
    @DecimalMax("100.00")
    private BigDecimal percentage;
}
