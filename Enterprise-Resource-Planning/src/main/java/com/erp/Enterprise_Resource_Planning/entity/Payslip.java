package com.erp.Enterprise_Resource_Planning.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
    name = "payslips",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_payslip_employee_month_year",
        columnNames = {"employee_id", "month", "year"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payslip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private Integer month;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal baseSalary;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal houseAmount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal transportAmount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal grossSalary;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal employeeTax;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal pension;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal medicalInsurance;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal otherDeductions;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal netSalary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayslipStatus status;
}
