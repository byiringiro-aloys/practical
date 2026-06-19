package com.erp.Enterprise_Resource_Planning.controller;

import com.erp.Enterprise_Resource_Planning.dto.*;
import com.erp.Enterprise_Resource_Planning.service.AuthService;
import com.erp.Enterprise_Resource_Planning.service.EmployeeService;
import com.erp.Enterprise_Resource_Planning.service.PayrollService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Self-service endpoints for authenticated employees.
 * All routes are under /api/me and require only a valid JWT – no role restriction
 * beyond being authenticated, since each method scopes its query to the
 * currently logged-in user's email.
 *
 * ADMIN and MANAGER users who also have an employee record can use these too.
 */
@RestController
@RequestMapping("/api/me")
@Tag(name = "My Profile", description = "Employee self-service – view profile, payslips, messages, change password")
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

    // ── Profile ───────────────────────────────────────────────────────────

    @GetMapping("/profile")
    @Operation(summary = "View own employee profile")
    public ResponseEntity<ApiResponse<EmployeeResponse>> myProfile(
            @AuthenticationPrincipal UserDetails principal) {
        EmployeeResponse profile = employeeService.getMyProfile(principal.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Profile retrieved.", profile));
    }

    // ── Password ──────────────────────────────────────────────────────────

    @PutMapping("/change-password")
    @Operation(summary = "Change own password (current password required)")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(principal.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.ok("Password changed successfully."));
    }

    // ── Payslips ──────────────────────────────────────────────────────────

    @GetMapping("/payslips")
    @Operation(summary = "View all own payslips")
    public ResponseEntity<ApiResponse<List<PayslipResponse>>> myPayslips(
            @AuthenticationPrincipal UserDetails principal) {
        List<PayslipResponse> payslips = payrollService.getMyPayslips(principal.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Payslips retrieved.", payslips));
    }

    @GetMapping("/payslips/period")
    @Operation(summary = "View own payslip for a specific month/year  (?month=6&year=2026)")
    public ResponseEntity<ApiResponse<PayslipResponse>> myPayslipByPeriod(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam Integer month,
            @RequestParam Integer year) {
        PayslipResponse ps = payrollService.getMyPayslipByPeriod(
                principal.getUsername(), month, year);
        return ResponseEntity.ok(ApiResponse.ok("Payslip retrieved.", ps));
    }

    // ── Messages ──────────────────────────────────────────────────────────

    @GetMapping("/messages")
    @Operation(summary = "View own salary-credited notifications")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> myMessages(
            @AuthenticationPrincipal UserDetails principal) {
        List<MessageResponse> messages = payrollService.getMyMessages(principal.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Messages retrieved.", messages));
    }
}
