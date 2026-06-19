package com.erp.Enterprise_Resource_Planning.controller;

import com.erp.Enterprise_Resource_Planning.dto.ApiResponse;
import com.erp.Enterprise_Resource_Planning.dto.MessageResponse;
import com.erp.Enterprise_Resource_Planning.dto.PayrollRequest;
import com.erp.Enterprise_Resource_Planning.dto.PayslipResponse;
import com.erp.Enterprise_Resource_Planning.service.PayrollService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payroll")
@Tag(name = "Payroll Management", description = "Payroll generation, approval, and payslip queries")
public class PayrollController {

    private final PayrollService payrollService;

    public PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    // ── Admin: generate payroll ───────────────────────────────────────────

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Generate payroll for all ACTIVE employees for a given month/year (MANAGER or ADMIN)")
    public ResponseEntity<ApiResponse<List<PayslipResponse>>> generate(
            @Valid @RequestBody PayrollRequest request) {
        List<PayslipResponse> payslips =
                payrollService.generatePayroll(request.getMonth(), request.getYear());
        return ResponseEntity.ok(ApiResponse.ok(
                "Payroll generated for " + request.getMonth() + "/" + request.getYear()
                + ". Total: " + payslips.size() + " payslips.", payslips));
    }

    // ── Admin: approve payroll (fires messaging) ──────────────────────────

    @PostMapping("/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve payroll – marks payslips PAID and sends salary-credited messages (ADMIN)")
    public ResponseEntity<ApiResponse<List<PayslipResponse>>> approve(
            @Valid @RequestBody PayrollRequest request) {
        List<PayslipResponse> payslips =
                payrollService.approvePayroll(request.getMonth(), request.getYear());
        return ResponseEntity.ok(ApiResponse.ok(
                "Payroll approved for " + request.getMonth() + "/" + request.getYear()
                + ". Notifications sent.", payslips));
    }

    // ── Admin: view payroll for a period ─────────────────────────────────

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get all payslips for a given month/year (ADMIN or MANAGER)")
    public ResponseEntity<ApiResponse<List<PayslipResponse>>> getByPeriod(
            @RequestParam Integer month,
            @RequestParam Integer year) {
        return ResponseEntity.ok(ApiResponse.ok("Payslips retrieved.",
                payrollService.getPayslipsByPeriod(month, year)));
    }

    // ── Admin: view messages for a period ────────────────────────────────

    @GetMapping("/messages")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "View salary-credited messages for a given month/year (ADMIN or MANAGER)")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getMessagesByPeriod(
            @RequestParam Integer month,
            @RequestParam Integer year) {
        return ResponseEntity.ok(ApiResponse.ok("Messages retrieved.",
                payrollService.getMessagesByPeriod(month, year)));
    }

    // ── Employee: view own payslips ───────────────────────────────────────

    @GetMapping("/my-payslips")
    @Operation(summary = "View own payslips (authenticated employee)")
    public ResponseEntity<ApiResponse<List<PayslipResponse>>> myPayslips(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok("Your payslips retrieved.",
                payrollService.getMyPayslips(principal.getUsername())));
    }

    @GetMapping("/my-payslips/period")
    @Operation(summary = "View own payslip for a specific month/year")
    public ResponseEntity<ApiResponse<PayslipResponse>> myPayslipByPeriod(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam Integer month,
            @RequestParam Integer year) {
        return ResponseEntity.ok(ApiResponse.ok("Payslip retrieved.",
                payrollService.getMyPayslipByPeriod(principal.getUsername(), month, year)));
    }

    // ── Employee: view own messages ───────────────────────────────────────

    @GetMapping("/my-messages")
    @Operation(summary = "View own salary-credited messages (authenticated employee)")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> myMessages(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok("Your messages retrieved.",
                payrollService.getMyMessages(principal.getUsername())));
    }
}
