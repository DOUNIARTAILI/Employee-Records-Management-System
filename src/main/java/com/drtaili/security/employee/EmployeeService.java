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

    public Employee createEmployee(Employee employee) {
        if (employee.getEmployeeId() != null && employeeRepository.existsById(employee.getEmployeeId())) {
            throw new RuntimeException("Employee with ID " + employee.getEmployeeId() + " already exists");
        }
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

    public List<Employee> searchEmployees(String name, Long id, String department, String jobTitle) {
        if (id != null) {
            return employeeRepository.findByEmployeeId(id);
        } else if (name != null && !name.isEmpty()) {
            return employeeRepository.findByFullNameContainingIgnoreCase(name);
        } else if (department != null && !department.isEmpty()) {
            return employeeRepository.findByDepartmentContainingIgnoreCase(department);
        } else if (jobTitle != null && !jobTitle.isEmpty()) {
            return employeeRepository.findByJobTitleContainingIgnoreCase(jobTitle);
        } else {
            return employeeRepository.findAll();
        }
    }
}

