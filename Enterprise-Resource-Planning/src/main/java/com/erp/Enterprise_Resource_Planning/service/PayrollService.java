package com.erp.Enterprise_Resource_Planning.service;

import com.erp.Enterprise_Resource_Planning.dto.MessageResponse;
import com.erp.Enterprise_Resource_Planning.dto.PayslipResponse;
import com.erp.Enterprise_Resource_Planning.entity.*;
import com.erp.Enterprise_Resource_Planning.exception.BadRequestException;
import com.erp.Enterprise_Resource_Planning.exception.ResourceNotFoundException;
import com.erp.Enterprise_Resource_Planning.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles payroll generation (Task 4) and payroll approval with messaging (Task 5).
 *
 * Formulas:
 *   House         = baseSalary * houseRate / 100
 *   Transport     = baseSalary * transportRate / 100
 *   Gross         = baseSalary + House + Transport
 *   EmployeeTax   = baseSalary * taxRate / 100
 *   Pension       = baseSalary * pensionRate / 100
 *   Medical       = baseSalary * medicalRate / 100
 *   Others        = baseSalary * othersRate / 100
 *   Net           = baseSalary - (EmployeeTax + Pension + Medical + Others)
 */
@Service
public class PayrollService {

    // Well-known deduction keys (stored by name in the deductions table)
    private static final String KEY_TAX       = "EmployeeTax";
    private static final String KEY_PENSION   = "Pension";
    private static final String KEY_MEDICAL   = "MedicalInsurance";
    private static final String KEY_OTHERS    = "Others";
    private static final String KEY_HOUSE     = "House";
    private static final String KEY_TRANSPORT = "Transport";

    private final EmployeeRepository employeeRepository;
    private final EmploymentRepository employmentRepository;
    private final DeductionRepository deductionRepository;
    private final PayslipRepository payslipRepository;
    private final MessageRepository messageRepository;
    private final EmailService emailService;

    @Value("${app.institution.name}")
    private String institutionName;

    public PayrollService(EmployeeRepository employeeRepository,
                          EmploymentRepository employmentRepository,
                          DeductionRepository deductionRepository,
                          PayslipRepository payslipRepository,
                          MessageRepository messageRepository,
                          EmailService emailService) {
        this.employeeRepository = employeeRepository;
        this.employmentRepository = employmentRepository;
        this.deductionRepository = deductionRepository;
        this.payslipRepository = payslipRepository;
        this.messageRepository = messageRepository;
        this.emailService = emailService;
    }

    // ── Payroll generation ────────────────────────────────────────────────

    /**
     * ADMIN kicks off payroll for all ACTIVE employees for a given Month/Year.
     * Employees already having a payslip for that period are skipped (duplicate guard).
     */
    @Transactional
    public List<PayslipResponse> generatePayroll(Integer month, Integer year) {
        BigDecimal taxRate       = getRate(KEY_TAX);
        BigDecimal pensionRate   = getRate(KEY_PENSION);
        BigDecimal medicalRate   = getRate(KEY_MEDICAL);
        BigDecimal othersRate    = getRate(KEY_OTHERS);
        BigDecimal houseRate     = getRate(KEY_HOUSE);
        BigDecimal transportRate = getRate(KEY_TRANSPORT);

        List<Employment> activeEmployments = employmentRepository.findAllByStatus(EmployeeStatus.ACTIVE);

        if (activeEmployments.isEmpty()) {
            throw new BadRequestException("No active employees found to run payroll for.");
        }

        List<Payslip> generated = activeEmployments.stream()
                .filter(emp -> !payslipRepository.existsByEmployeeAndMonthAndYear(
                        emp.getEmployee(), month, year))
                .map(emp -> buildPayslip(emp.getEmployee(), emp.getSalary(),
                        month, year,
                        taxRate, pensionRate, medicalRate, othersRate,
                        houseRate, transportRate))
                .map(payslipRepository::save)
                .collect(Collectors.toList());

        return generated.stream().map(this::toPayslipResponse).collect(Collectors.toList());
    }

    /**
     * ADMIN approves payroll for a given Month/Year.
     * This Java layer mirrors the DB trigger by:
     *  1. Updating all PENDING payslips for that period to PAID.
     *  2. Writing a salary-credited message per employee.
     *
     * NOTE: The PostgreSQL trigger (see db/payslip_trigger.sql) fires on INSERT and
     *       handles the same logic at the DBMS level. The Java layer here provides
     *       an additional application-level approval gate that can be called explicitly.
     */
    @Transactional
    public List<PayslipResponse> approvePayroll(Integer month, Integer year) {
        List<Payslip> payslips = payslipRepository.findAllByMonthAndYear(month, year);

        if (payslips.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No payroll found for " + month + "/" + year + ". Run payroll generation first.");
        }

        payslips.forEach(ps -> {
            if (ps.getStatus() == PayslipStatus.PENDING) {
                ps.setStatus(PayslipStatus.PAID);
                payslipRepository.save(ps);

                // Persist message record in DB
                String content = buildMessage(ps);
                Message msg = Message.builder()
                        .employee(ps.getEmployee())
                        .month(month)
                        .year(year)
                        .content(content)
                        .sentAt(LocalDateTime.now())
                        .build();
                messageRepository.save(msg);

                // Send real email via Resend (best-effort, non-blocking on failure)
                emailService.sendSalaryCreditedEmail(
                        ps.getEmployee().getEmail(),
                        ps.getEmployee().getFirstName(),
                        content
                );
            }
        });

        return payslips.stream().map(this::toPayslipResponse).collect(Collectors.toList());
    }

    // ── Payslip queries ───────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<PayslipResponse> getPayslipsByPeriod(Integer month, Integer year) {
        return payslipRepository.findAllByMonthAndYear(month, year).stream()
                .map(this::toPayslipResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PayslipResponse> getMyPayslips(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + email));
        return payslipRepository.findAllByEmployee(employee).stream()
                .map(this::toPayslipResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PayslipResponse getMyPayslipByPeriod(String email, Integer month, Integer year) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + email));
        Payslip ps = payslipRepository.findByEmployeeAndMonthAndYear(employee, month, year)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payslip not found for " + month + "/" + year));
        return toPayslipResponse(ps);
    }

    // ── Message queries ───────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<MessageResponse> getMyMessages(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + email));
        return messageRepository.findAllByEmployee(employee).stream()
                .map(this::toMessageResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getMessagesByPeriod(Integer month, Integer year) {
        return messageRepository.findAllByMonthAndYear(month, year).stream()
                .map(this::toMessageResponse)
                .collect(Collectors.toList());
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private Payslip buildPayslip(Employee employee, BigDecimal base,
                                  Integer month, Integer year,
                                  BigDecimal taxRate, BigDecimal pensionRate,
                                  BigDecimal medicalRate, BigDecimal othersRate,
                                  BigDecimal houseRate, BigDecimal transportRate) {

        BigDecimal house     = pct(base, houseRate);
        BigDecimal transport = pct(base, transportRate);
        BigDecimal gross     = base.add(house).add(transport);

        BigDecimal tax     = pct(base, taxRate);
        BigDecimal pension = pct(base, pensionRate);
        BigDecimal medical = pct(base, medicalRate);
        BigDecimal others  = pct(base, othersRate);

        BigDecimal net = base
                .subtract(tax)
                .subtract(pension)
                .subtract(medical)
                .subtract(others);

        return Payslip.builder()
                .employee(employee)
                .month(month)
                .year(year)
                .baseSalary(base)
                .houseAmount(house)
                .transportAmount(transport)
                .grossSalary(gross)
                .employeeTax(tax)
                .pension(pension)
                .medicalInsurance(medical)
                .otherDeductions(others)
                .netSalary(net)
                .status(PayslipStatus.PENDING)
                .build();
    }

    private BigDecimal pct(BigDecimal base, BigDecimal rate) {
        return base.multiply(rate)
                   .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal getRate(String deductionName) {
        return deductionRepository.findByNameIgnoreCase(deductionName)
                .map(Deduction::getPercentage)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Deduction configuration not found: " + deductionName +
                        ". Please seed the deductions table before running payroll."));
    }

    private String buildMessage(Payslip ps) {
        Employment employment = employmentRepository.findByEmployee(ps.getEmployee())
                .orElse(null);
        String empId = employment != null ? employment.getEmployeeId() : "N/A";
        String monthYear = ps.getMonth() + "/" + ps.getYear();

        return String.format(
                "Dear %s, Your salary of %s from %s %s has been credited to your %s account Successfully.",
                ps.getEmployee().getFirstName(),
                monthYear,
                institutionName,
                ps.getNetSalary().toPlainString(),
                empId
        );
    }

    // ── Response mappers ──────────────────────────────────────────────────

    private PayslipResponse toPayslipResponse(Payslip ps) {
        Employment employment = employmentRepository.findByEmployee(ps.getEmployee()).orElse(null);
        String empId = employment != null ? employment.getEmployeeId() : "N/A";
        String name  = ps.getEmployee().getFirstName() + " " + ps.getEmployee().getLastName();

        return PayslipResponse.builder()
                .id(ps.getId())
                .empId(empId)
                .name(name)
                .base(ps.getBaseSalary())
                .house(ps.getHouseAmount())
                .transport(ps.getTransportAmount())
                .gross(ps.getGrossSalary())
                .tax(ps.getEmployeeTax())
                .pension(ps.getPension())
                .medical(ps.getMedicalInsurance())
                .other(ps.getOtherDeductions())
                .netSalary(ps.getNetSalary())
                .status(ps.getStatus().name())
                .month(ps.getMonth())
                .year(ps.getYear())
                .build();
    }

    private MessageResponse toMessageResponse(Message msg) {
        return MessageResponse.builder()
                .id(msg.getId())
                .employeeId(msg.getEmployee().getId())
                .employeeName(msg.getEmployee().getFirstName() + " " + msg.getEmployee().getLastName())
                .month(msg.getMonth())
                .year(msg.getYear())
                .content(msg.getContent())
                .sentAt(msg.getSentAt())
                .build();
    }
}
