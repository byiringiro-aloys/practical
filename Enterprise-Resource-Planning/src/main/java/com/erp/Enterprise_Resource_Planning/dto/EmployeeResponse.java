package com.erp.Enterprise_Resource_Planning.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class EmployeeResponse {

    // Personal
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String district;
    private String mobile;
    private LocalDate dateOfBirth;

    // Employment
    private String employeeId;
    private String department;
    private String position;
    private BigDecimal salary;
    private String status;
    private LocalDate joiningDate;
}
