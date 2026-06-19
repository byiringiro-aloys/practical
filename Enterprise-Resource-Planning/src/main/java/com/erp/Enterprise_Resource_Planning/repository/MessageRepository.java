package com.erp.Enterprise_Resource_Planning.repository;

import com.erp.Enterprise_Resource_Planning.entity.Employee;
import com.erp.Enterprise_Resource_Planning.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findAllByEmployee(Employee employee);
    List<Message> findAllByMonthAndYear(Integer month, Integer year);
}
