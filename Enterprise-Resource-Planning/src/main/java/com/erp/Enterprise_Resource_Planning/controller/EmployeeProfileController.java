package com.erp.Enterprise_Resource_Planning.controller;

import com.erp.Enterprise_Resource_Planning.dto.*;
import com.erp.Enterprise_Resource_Planning.service.AuthService;
import com.erp.Enterprise_Resource_Planning.service.EmployeeService;
import com.erp.Enterprise_Resource_Planning.service.PayrollService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/me")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "My Profile")
public class EmployeeProfileController {

    private final EmployeeService employeeService;
    private final PayrollService  payrollService;
    private final AuthService     authService;

    public EmployeeProfileController(EmployeeService employeeService,
                                     PayrollService payrollService,
                                     AuthService authService) {
        this.employeeService = employeeService;
        this.payrollService  = payrollService;
        this.authService     = authService;
    }

    // ─────────────────────────────────────────────────────────────────────
    // GET /api/me/profile
    // ─────────────────────────────────────────────────────────────────────

    @GetMapping("/profile")
    @Operation(
        summary     = "Get own profile",
        description = "Returns the personal and employment details for the currently authenticated employee."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Profile retrieved",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Not authenticated – provide Bearer token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "No employee record linked to this account (ADMIN/MANAGER without an employee record)")
    })
    public ResponseEntity<ApiResponse<EmployeeResponse>> myProfile(
            @AuthenticationPrincipal UserDetails principal) {
        EmployeeResponse profile = employeeService.getMyProfile(principal.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Profile retrieved.", profile));
    }

    // ─────────────────────────────────────────────────────────────────────
    // PUT /api/me/change-password
    // ─────────────────────────────────────────────────────────────────────

    @PutMapping("/change-password")
    @Operation(
        summary     = "Change own password",
        description = """
            Change the currently authenticated user's password.

            Employees should do this on first login to replace the default emp_code password.
            Requires the current password as confirmation.
            """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Password changed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "Current password incorrect, or new password is same as current",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(principal.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.ok("Password changed successfully."));
    }

    // ─────────────────────────────────────────────────────────────────────
    // GET /api/me/payslips
    // ─────────────────────────────────────────────────────────────────────

    @GetMapping("/payslips")
    @Operation(
        summary     = "Get all own payslips",
        description = "Returns every payslip generated for the authenticated employee across all periods."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Payslips retrieved (empty list if none generated yet)",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Not authenticated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "No employee record linked to this account")
    })
    public ResponseEntity<ApiResponse<List<PayslipResponse>>> myPayslips(
            @AuthenticationPrincipal UserDetails principal) {
        List<PayslipResponse> payslips = payrollService.getMyPayslips(principal.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Payslips retrieved.", payslips));
    }

    // ─────────────────────────────────────────────────────────────────────
    // GET /api/me/payslips/period?month=6&year=2026
    // ─────────────────────────────────────────────────────────────────────

    @GetMapping("/payslips/period")
    @Operation(
        summary     = "Get own payslip for a specific period",
        description = "Returns the payslip for the authenticated employee for the given month and year."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Payslip retrieved",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Not authenticated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "No payslip found for the requested period")
    })
    public ResponseEntity<ApiResponse<PayslipResponse>> myPayslipByPeriod(
            @AuthenticationPrincipal UserDetails principal,
            @Parameter(description = "Month number (1–12)", example = "6", required = true)
            @RequestParam Integer month,
            @Parameter(description = "Four-digit year", example = "2026", required = true)
            @RequestParam Integer year) {
        PayslipResponse ps = payrollService.getMyPayslipByPeriod(
                principal.getUsername(), month, year);
        return ResponseEntity.ok(ApiResponse.ok("Payslip retrieved.", ps));
    }

    // ─────────────────────────────────────────────────────────────────────
    // GET /api/me/messages
    // ─────────────────────────────────────────────────────────────────────

    @GetMapping("/messages")
    @Operation(
        summary     = "Get own salary notifications",
        description = "Returns all salary-credited notification messages for the authenticated employee. " +
                      "Messages are generated when ADMIN approves payroll."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Messages retrieved (empty list if payroll not yet approved)",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Not authenticated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "No employee record linked to this account")
    })
    public ResponseEntity<ApiResponse<List<MessageResponse>>> myMessages(
            @AuthenticationPrincipal UserDetails principal) {
        List<MessageResponse> messages = payrollService.getMyMessages(principal.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Messages retrieved.", messages));
    }
}
