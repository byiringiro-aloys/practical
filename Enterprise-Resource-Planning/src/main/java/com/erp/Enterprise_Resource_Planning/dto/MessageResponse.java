package com.erp.Enterprise_Resource_Planning.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Salary-credited notification sent to an employee when payroll is approved")
public class MessageResponse {

    @Schema(description = "Internal message ID", example = "1")
    private Long id;

    @Schema(description = "Employee's database ID", example = "1")
    private Long employeeId;

    @Schema(description = "Employee full name", example = "Peter Mugisha")
    private String employeeName;

    @Schema(description = "Payroll month", example = "6")
    private Integer month;

    @Schema(description = "Payroll year", example = "2026")
    private Integer year;

    @Schema(description = "Full notification text",
            example = "Dear Peter, Your salary of 6/2026 from Government of Rwanda 378000.00 has been credited to your EMP-0001 account Successfully.")
    private String content;

    @Schema(description = "Timestamp the message was generated", example = "2026-06-19T10:30:00")
    private LocalDateTime sentAt;
}
