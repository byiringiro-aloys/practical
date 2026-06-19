package com.erp.Enterprise_Resource_Planning.controller;

import com.erp.Enterprise_Resource_Planning.dto.ApiResponse;
import com.erp.Enterprise_Resource_Planning.dto.MessageResponse;
import com.erp.Enterprise_Resource_Planning.dto.PayrollRequest;
import com.erp.Enterprise_Resource_Planning.dto.PayslipResponse;
import com.erp.Enterprise_Resource_Planning.service.PayrollService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payroll")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Payroll Management")
public class PayrollController {

    private final PayrollService payrollService;

    public PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    // ─────────────────────────────────────────────────────────────────────
    // POST /api/payroll/generate
    // ─────────────────────────────────────────────────────────────────────

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
        summary     = "Generate payroll",
        description = """
            Computes and creates payslips for **all ACTIVE employees** for the given month/year.
            Inactive employees are automatically excluded.
            Employees who already have a payslip for this period are skipped (duplicate guard).

            **Payroll formulas:**
            ```
            House         = baseSalary × 14%
            Transport     = baseSalary × 14%
            Gross         = baseSalary + House + Transport

            EmployeeTax   = baseSalary × 30%
            Pension       = baseSalary × 6%
            MedicalIns.   = baseSalary × 5%
            Others        = baseSalary × 5%
            Net           = baseSalary − (Tax + Pension + Medical + Others)
            ```

            Generated payslips have status **PENDING** until approved.

            **Requires:** MANAGER or ADMIN role.
            """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Payslips generated",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                               schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "No active employees found, or validation error",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                               schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Not authenticated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Insufficient role – MANAGER or ADMIN required")
    })
    public ResponseEntity<ApiResponse<List<PayslipResponse>>> generate(
            @Valid @RequestBody PayrollRequest request) {
        List<PayslipResponse> payslips =
                payrollService.generatePayroll(request.getMonth(), request.getYear());
        return ResponseEntity.ok(ApiResponse.ok(
                "Payroll generated for " + request.getMonth() + "/" + request.getYear()
                        + ". Total: " + payslips.size() + " payslips.", payslips));
    }

    // ─────────────────────────────────────────────────────────────────────
    // POST /api/payroll/approve
    // ─────────────────────────────────────────────────────────────────────

    @PostMapping("/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary     = "Approve payroll",
        description = """
            Approves all **PENDING** payslips for the given month/year.

            For each employee this action:
            1. Sets the payslip status to **PAID**
            2. Persists a salary-credited message in the `messages` table
            3. Sends a real email notification via **Resend** to the employee's address

            The PostgreSQL trigger `trg_payslip_after_insert` also handles steps 2–3 at the
            database level when payslips are inserted.

            **Run `POST /api/payroll/generate` first for the same period.**

            **Requires:** ADMIN role only.
            """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Payroll approved, payslips marked PAID, notifications sent",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                               schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Not authenticated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Insufficient role – ADMIN required"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "No payroll found for this period – run generate first",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                               schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<List<PayslipResponse>>> approve(
            @Valid @RequestBody PayrollRequest request) {
        List<PayslipResponse> payslips =
                payrollService.approvePayroll(request.getMonth(), request.getYear());
        return ResponseEntity.ok(ApiResponse.ok(
                "Payroll approved for " + request.getMonth() + "/" + request.getYear()
                        + ". Notifications sent.", payslips));
    }

    // ─────────────────────────────────────────────────────────────────────
    // GET /api/payroll?month=6&year=2026
    // ─────────────────────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
        summary     = "Get payslips by period",
        description = "Returns all employee payslips for the given month and year. Requires ADMIN or MANAGER role."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Payslips retrieved (empty list if payroll not yet generated)",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                               schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Not authenticated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Insufficient role – ADMIN or MANAGER required")
    })
    public ResponseEntity<ApiResponse<List<PayslipResponse>>> getByPeriod(
            @Parameter(description = "Month number (1–12)", example = "6", required = true)
            @RequestParam Integer month,
            @Parameter(description = "Four-digit year", example = "2026", required = true)
            @RequestParam Integer year) {
        return ResponseEntity.ok(ApiResponse.ok("Payslips retrieved.",
                payrollService.getPayslipsByPeriod(month, year)));
    }

    // ─────────────────────────────────────────────────────────────────────
    // GET /api/payroll/messages?month=6&year=2026
    // ─────────────────────────────────────────────────────────────────────

    @GetMapping("/messages")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
        summary     = "Get salary messages by period",
        description = "Returns all salary-credited notification messages generated when payroll was approved for the given period."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Messages retrieved (empty list if payroll not yet approved)",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                               schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Not authenticated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Insufficient role – ADMIN or MANAGER required")
    })
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getMessagesByPeriod(
            @Parameter(description = "Month number (1–12)", example = "6", required = true)
            @RequestParam Integer month,
            @Parameter(description = "Four-digit year", example = "2026", required = true)
            @RequestParam Integer year) {
        return ResponseEntity.ok(ApiResponse.ok("Messages retrieved.",
                payrollService.getMessagesByPeriod(month, year)));
    }
}
