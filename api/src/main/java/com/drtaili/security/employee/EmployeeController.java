package com.drtaili.security.employee;

import com.drtaili.security.auth.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/employee")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'HR')")
public class EmployeeController {
    private final EmployeeService employeeService;
    @PreAuthorize("hasAnyAuthority('admin:read', 'manager:read', 'hr:read')")
    @GetMapping
    public List<Employee> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    @PreAuthorize("hasAnyAuthority('admin:read', 'manager:read', 'hr:read')")
    @GetMapping("/search")
    public ResponseEntity<List<Employee>> searchEmployees(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String jobTitle) {
        List<Employee> employees = employeeService.searchEmployees(name, id, department, jobTitle);
        return ResponseEntity.ok(employees);
    }
    @PreAuthorize("hasAnyAuthority('admin:create', 'hr:create')")
    @PostMapping
    public Employee createEmployee(@Valid @RequestBody Employee employee) {
        return employeeService.createEmployee(employee);
    }
    @PreAuthorize("hasAnyAuthority('admin:update', 'manager:update', 'hr:update')")
    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id, @Valid @RequestBody Employee employeeDetails) {
        Employee updatedEmployee = employeeService.updateEmployee(id, employeeDetails);
        return ResponseEntity.ok(updatedEmployee);
    }
    @PreAuthorize("hasAnyAuthority('admin:delete', 'hr:delete')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
