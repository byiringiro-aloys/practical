package com.erp.Enterprise_Resource_Planning.repository;

import com.erp.Enterprise_Resource_Planning.entity.Employee;
import com.erp.Enterprise_Resource_Planning.entity.Employment;
import com.erp.Enterprise_Resource_Planning.entity.EmployeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmploymentRepository extends JpaRepository<Employment, Long> {
    Optional<Employment> findByEmployee(Employee employee);
    Optional<Employment> findByEmployeeId(String employeeId);
    boolean existsByEmployeeId(String employeeId);
    List<Employment> findAllByStatus(EmployeeStatus status);
}
