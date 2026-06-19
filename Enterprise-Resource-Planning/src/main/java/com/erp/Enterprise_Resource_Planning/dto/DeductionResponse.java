package com.erp.Enterprise_Resource_Planning.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DeductionResponse {
    private Long id;
    private String name;
    private BigDecimal percentage;
}
