package com.erp.Enterprise_Resource_Planning.service;

import com.erp.Enterprise_Resource_Planning.dto.EmployeeRequest;
import com.erp.Enterprise_Resource_Planning.dto.EmployeeResponse;
import com.erp.Enterprise_Resource_Planning.entity.*;
import com.erp.Enterprise_Resource_Planning.exception.BadRequestException;
import com.erp.Enterprise_Resource_Planning.exception.DuplicateResourceException;
import com.erp.Enterprise_Resource_Planning.exception.ResourceNotFoundException;
import com.erp.Enterprise_Resource_Planning.repository.EmployeeRepository;
import com.erp.Enterprise_Resource_Planning.repository.EmploymentRepository;
import com.erp.Enterprise_Resource_Planning.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmploymentRepository employmentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(EmployeeRepository employeeRepository,
                           EmploymentRepository employmentRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.employmentRepository = employmentRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates Employee + Employment records, and also a User account
     * for the employee (so they can log in with their email).
     */
    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already in use: " + request.getEmail());
        }
        if (employmentRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new DuplicateResourceException("Employee ID already in use: " + request.getEmployeeId());
        }

        EmployeeStatus status;
        try {
            status = EmployeeStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status value: " + request.getStatus());
        }

        Employee employee = Employee.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .district(request.getDistrict())
                .mobile(request.getMobile())
                .dateOfBirth(request.getDateOfBirth())
                .build();
        employee = employeeRepository.save(employee);

        Employment employment = Employment.builder()
                .employeeId(request.getEmployeeId())
                .department(request.getDepartment())
                .position(request.getPosition())
                .salary(request.getSalary())
                .status(status)
                .joiningDate(request.getJoiningDate())
                .employee(employee)
                .build();
        employmentRepository.save(employment);

        // Create a login account for the employee (default password = employeeId)
        if (!userRepository.existsByEmail(request.getEmail())) {
            User user = User.builder()
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getEmployeeId()))
                    .role(Role.EMPLOYEE)
                    .employee(employee)
                    .build();
            userRepository.save(user);
        }

        return mapToResponse(employee, employment);
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(emp -> {
                    Employment employment = employmentRepository.findByEmployee(emp)
                            .orElse(null);
                    return mapToResponse(emp, employment);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        Employment employment = employmentRepository.findByEmployee(employee).orElse(null);
        return mapToResponse(employee, employment);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getMyProfile(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No employee record linked to account: " + email));
        Employment employment = employmentRepository.findByEmployee(employee).orElse(null);
        return mapToResponse(employee, employment);
    }

    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        // Check email uniqueness if changed
        if (!employee.getEmail().equals(request.getEmail())
                && employeeRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already in use: " + request.getEmail());
        }

        Employment employment = employmentRepository.findByEmployee(employee)
                .orElseThrow(() -> new ResourceNotFoundException("Employment record not found for employee: " + id));

        if (!employment.getEmployeeId().equals(request.getEmployeeId())
                && employmentRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new DuplicateResourceException("Employee ID already in use: " + request.getEmployeeId());
        }

        EmployeeStatus status;
        try {
            status = EmployeeStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status value: " + request.getStatus());
        }

        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setDistrict(request.getDistrict());
        employee.setMobile(request.getMobile());
        employee.setDateOfBirth(request.getDateOfBirth());
        employeeRepository.save(employee);

        // Update employment
        employment.setEmployeeId(request.getEmployeeId());
        employment.setDepartment(request.getDepartment());
        employment.setPosition(request.getPosition());
        employment.setSalary(request.getSalary());
        employment.setStatus(status);
        employment.setJoiningDate(request.getJoiningDate());
        employmentRepository.save(employment);

        return mapToResponse(employee, employment);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        employeeRepository.delete(employee);
    }

    // ── Mapping helper ──────────────────────────────────────────────────────

    public static EmployeeResponse mapToResponse(Employee emp, Employment employment) {
        EmployeeResponse.EmployeeResponseBuilder builder = EmployeeResponse.builder()
                .id(emp.getId())
                .firstName(emp.getFirstName())
                .lastName(emp.getLastName())
                .email(emp.getEmail())
                .district(emp.getDistrict())
                .mobile(emp.getMobile())
                .dateOfBirth(emp.getDateOfBirth());

        if (employment != null) {
            builder.employeeId(employment.getEmployeeId())
                   .department(employment.getDepartment())
                   .position(employment.getPosition())
                   .salary(employment.getSalary())
                   .status(employment.getStatus().name())
                   .joiningDate(employment.getJoiningDate());
        }

        return builder.build();
    }
}
