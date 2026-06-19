package com.erp.Enterprise_Resource_Planning.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "Computed payslip for one employee for a given month/year. " +
        "Columns: EmpId | Name | Base | House | Transport | Gross | Tax | Pension | Medical | Other | NetSalary | Status | Month | Year")
public class PayslipResponse {

    @Schema(description = "Internal payslip ID", example = "1")
    private Long id;

    @Schema(description = "Employee code", example = "EMP-0001")
    private String empId;

    @Schema(description = "Full name", example = "Peter Mugisha")
    private String name;

    @Schema(description = "Base salary in RWF", example = "700000.00")
    private BigDecimal base;

    @Schema(description = "House allowance (base × 14%)", example = "98000.00")
    private BigDecimal house;

    @Schema(description = "Transport allowance (base × 14%)", example = "98000.00")
    private BigDecimal transport;

    @Schema(description = "Gross salary = base + house + transport", example = "896000.00")
    private BigDecimal gross;

    @Schema(description = "Employee tax deduction (base × 30%)", example = "210000.00")
    private BigDecimal tax;

    @Schema(description = "Pension deduction (base × 6%)", example = "42000.00")
    private BigDecimal pension;

    @Schema(description = "Medical insurance deduction (base × 5%)", example = "35000.00")
    private BigDecimal medical;

    @Schema(description = "Other deductions (base × 5%)", example = "35000.00")
    private BigDecimal other;

    @Schema(description = "Net salary = baseSalary − (employeeTax + pension + medicalInsurance + otherDeductions)", example = "378000.00")
    private BigDecimal netSalary;

    @Schema(description = "Payslip status", example = "PAID", allowableValues = {"PENDING", "PAID"})
    private String status;

    @Schema(description = "Payroll month", example = "6")
    private Integer month;

    @Schema(description = "Payroll year", example = "2026")
    private Integer year;
}
