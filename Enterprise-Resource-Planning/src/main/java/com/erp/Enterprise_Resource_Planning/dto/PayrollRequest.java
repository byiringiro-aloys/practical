package com.erp.Enterprise_Resource_Planning.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PayrollRequest {

    @NotNull
    @Min(1) @Max(12)
    private Integer month;

    @NotNull
    @Min(2000)
    private Integer year;
}
