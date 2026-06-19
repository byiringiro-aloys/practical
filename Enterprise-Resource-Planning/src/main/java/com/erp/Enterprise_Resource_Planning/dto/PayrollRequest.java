package com.erp.Enterprise_Resource_Planning.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(description = "Month and year identifying a payroll period")
public class PayrollRequest {

    @NotNull
    @Min(1) @Max(12)
    @Schema(description = "Month number (1 = January … 12 = December)", example = "6")
    private Integer month;

    @NotNull
    @Min(2000)
    @Schema(description = "Four-digit year", example = "2026")
    private Integer year;
}
