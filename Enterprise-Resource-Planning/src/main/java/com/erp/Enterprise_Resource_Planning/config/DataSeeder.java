package com.erp.Enterprise_Resource_Planning.config;

import com.erp.Enterprise_Resource_Planning.entity.*;
import com.erp.Enterprise_Resource_Planning.repository.*;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final EmploymentRepository employmentRepository;
    private final DeductionRepository deductionRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      EmployeeRepository employeeRepository,
                      EmploymentRepository employmentRepository,
                      DeductionRepository deductionRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.employmentRepository = employmentRepository;
        this.deductionRepository = deductionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String @NonNull ... args) {
        seedAdmin();
        seedManager();
        seedDeductions();
        seedSampleEmployees();
    }

    // ── Admin user ────────────────────────────────────────────────────────

    private void seedAdmin() {
        String adminEmail = "admin@erp.rw";
        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(admin);
            log.info("Seeded ADMIN user: {} / admin123", adminEmail);
        }
    }

    // ── Manager user ──────────────────────────────────────────────────────

    private void seedManager() {
        String managerEmail = "manager@erp.rw";
        if (!userRepository.existsByEmail(managerEmail)) {
            User manager = User.builder()
                    .email(managerEmail)
                    .password(passwordEncoder.encode("manager123"))
                    .role(Role.MANAGER)
                    .build();
            userRepository.save(manager);
            log.info("Seeded MANAGER user: {} / manager123", managerEmail);
        }
    }

    // ── Deductions ────────────────────────────────────────────────────────

    private void seedDeductions() {
        Map<String, BigDecimal> defaults = Map.of(
                "EmployeeTax",       BigDecimal.valueOf(30),
                "Pension",           BigDecimal.valueOf(6),
                "MedicalInsurance",  BigDecimal.valueOf(5),
                "Others",            BigDecimal.valueOf(5),
                "House",             BigDecimal.valueOf(14),
                "Transport",         BigDecimal.valueOf(14)
        );

        defaults.forEach((name, pct) -> {
            if (!deductionRepository.existsByNameIgnoreCase(name)) {
                deductionRepository.save(
                        Deduction.builder().name(name).percentage(pct).build()
                );
                log.info("Seeded deduction: {} @ {}%", name, pct);
            }
        });
    }

    // ──  Employees ──────────────────────────────────────────────────

    private void seedSampleEmployees() {
        seedEmployee(
                "Mugabo", "Javis", "mugabo.javis@erp.rw",
                "Kigali", "+250788000001", LocalDate.of(1990, 3, 15),
                "EMP-0001", "Finance", "Finance Officer",
                BigDecimal.valueOf(700_000), EmployeeStatus.ACTIVE, LocalDate.of(2020, 1, 10)
        );

        seedEmployee(
                "Michou", "Michell", "michou.michell@erp.rw",
                "Musanze", "+250788000002", LocalDate.of(1993, 7, 22),
                "EMP-0002", "HR", "HR Manager",
                BigDecimal.valueOf(850_000), EmployeeStatus.ACTIVE, LocalDate.of(2019, 5, 1)
        );
    }

    private void seedEmployee(
            String firstName, String lastName, String email,
            String district, String mobile, LocalDate dob,
            String empId, String dept, String position,
            BigDecimal salary, EmployeeStatus status, LocalDate joiningDate) {

        if (employeeRepository.existsByEmail(email)) return;

        Employee employee = employeeRepository.save(
                Employee.builder()
                        .firstName(firstName).lastName(lastName).email(email)
                        .district(district).mobile(mobile).dateOfBirth(dob)
                        .build()
        );

        employmentRepository.save(
                Employment.builder()
                        .employeeId(empId).department(dept).position(position)
                        .salary(salary).status(status).joiningDate(joiningDate)
                        .employee(employee)
                        .build()
        );

        if (!userRepository.existsByEmail(email)) {
            userRepository.save(
                    User.builder()
                            .email(email)
                            .password(passwordEncoder.encode(empId)) // default password = employeeId
                            .role(Role.EMPLOYEE)
                            .employee(employee)
                            .build()
            );
        }

        log.info("Seeded employee: {} {} ({})", firstName, lastName, empId);
    }
}
