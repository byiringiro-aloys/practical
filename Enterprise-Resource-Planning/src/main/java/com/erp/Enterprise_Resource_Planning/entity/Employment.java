package com.erp.Enterprise_Resource_Planning.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Professional / employment details tied to an Employee.
 * The {@code salary} here is the BASE salary used for all payroll computations.
 *
 * Column naming note:
 *   - emp_code  → the human-readable employee code (e.g. EMP-0001)
 *   - employee_fk → FK back to employees.id
 */
@Entity
@Table(name = "employment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Human-readable employee code, e.g. EMP-0001. Stored as emp_code to avoid FK name collision. */
    @Column(name = "emp_code", nullable = false, unique = true)
    private String employeeId;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private String position;

    /** Base salary (RWF). */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal salary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmployeeStatus status;

    @Column(nullable = false)
    private LocalDate joiningDate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_fk", nullable = false)
    private Employee employee;
}
