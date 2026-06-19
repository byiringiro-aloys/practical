package com.erp.Enterprise_Resource_Planning.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MessageResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private Integer month;
    private Integer year;
    private String content;
    private LocalDateTime sentAt;
}
