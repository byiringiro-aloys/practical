package com.erp.Enterprise_Resource_Planning.repository;

import com.erp.Enterprise_Resource_Planning.entity.Employee;
import com.erp.Enterprise_Resource_Planning.entity.Payslip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PayslipRepository extends JpaRepository<Payslip, Long> {

    boolean existsByEmployeeAndMonthAndYear(Employee employee, Integer month, Integer year);

    List<Payslip> findAllByMonthAndYear(Integer month, Integer year);

    List<Payslip> findAllByEmployee(Employee employee);

    Optional<Payslip> findByEmployeeAndMonthAndYear(Employee employee, Integer month, Integer year);
}
