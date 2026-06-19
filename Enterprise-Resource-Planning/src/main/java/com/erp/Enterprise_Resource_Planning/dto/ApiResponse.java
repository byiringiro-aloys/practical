package com.erp.Enterprise_Resource_Planning.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Standard API response envelope wrapping all responses")
public class ApiResponse<T> {

    @Schema(description = "true when the operation succeeded, false on error", example = "true")
    private boolean success;

    @Schema(description = "Human-readable result message", example = "Employee retrieved.")
    private String message;

    @Schema(description = "Response payload – null for void operations or on error")
    private T data;

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> ok(String message) {
        return new ApiResponse<>(true, message, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
