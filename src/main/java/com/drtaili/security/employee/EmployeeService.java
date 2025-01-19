package com.drtaili.security.employee;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Optional<Employee> getEmployeeById(Long employeeId) {
        return employeeRepository.findById(employeeId);
    }

    public Employee createEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    public Employee updateEmployee(Long employeeId, Employee employeeDetails) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + employeeId));

        employee.setFullName(employeeDetails.getFullName());
        employee.setJobTitle(employeeDetails.getJobTitle());
        employee.setDepartment(employeeDetails.getDepartment());
        employee.setHireDate(employeeDetails.getHireDate());
        employee.setEmploymentStatus(employeeDetails.getEmploymentStatus());
        employee.setContactInformation(employeeDetails.getContactInformation());
        employee.setAddress(employeeDetails.getAddress());

        return employeeRepository.save(employee);
    }

    public void deleteEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + employeeId));
        employeeRepository.delete(employee);
    }
}

