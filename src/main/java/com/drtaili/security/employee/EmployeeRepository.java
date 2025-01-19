package com.drtaili.security.employee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByFullNameContainingIgnoreCase(String name);
    List<Employee> findByDepartmentContainingIgnoreCase(String department);
    List<Employee> findByJobTitleContainingIgnoreCase(String jobTitle);
    List<Employee> findByEmployeeId(Long id);
}
