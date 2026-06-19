package com.erp.Enterprise_Resource_Planning.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Matches the sample payslip columns:
 * EmpId | Name | Base | House | Transport | Gross | Tax | Pension | Medical | Other | NetSalary | Status | Month | Year
 */
@Data
@Builder
public class PayslipResponse {
    private Long id;
    private String empId;          // employeeId string
    private String name;           // firstName + lastName
    private BigDecimal base;
    private BigDecimal house;
    private BigDecimal transport;
    private BigDecimal gross;
    private BigDecimal tax;
    private BigDecimal pension;
    private BigDecimal medical;
    private BigDecimal other;
    private BigDecimal netSalary;
    private String status;
    private Integer month;
    private Integer year;
}
