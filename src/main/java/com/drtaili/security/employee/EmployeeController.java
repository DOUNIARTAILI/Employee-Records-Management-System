package com.drtaili.security.employee;

import com.drtaili.security.auth.AuthenticationService;
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
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        return employeeService.getEmployeeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyAuthority('admin:create', 'hr:create')")
    @PostMapping
    public Employee createEmployee(@RequestBody Employee employee) {
        return employeeService.createEmployee(employee);
    }
    @PreAuthorize("hasAnyAuthority('admin:update', 'manager:update', 'hr:update')")
    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id, @RequestBody Employee employeeDetails) {
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
