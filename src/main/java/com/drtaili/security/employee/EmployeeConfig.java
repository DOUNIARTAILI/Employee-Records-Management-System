package com.drtaili.security.employee;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Date;
import java.util.List;

@Configuration
public class EmployeeConfig {
    @Bean
    CommandLineRunner commandLineRunner(EmployeeRepository repository) {
    return args -> {
        Employee employee1 = Employee.builder()
                .fullName("John Doe")
                .jobTitle("Software Engineer")
                .department("Engineering")
                .hireDate(new Date())
                .employmentStatus("Full-Time")
                .contactInformation("john.doe@example.com")
                .address("123 Main St, Cityville")
                .build();

        Employee employee2 = Employee.builder()
                .fullName("Jane Smith")
                .jobTitle("HR Manager")
                .department("Human Resources")
                .hireDate(new Date())
                .employmentStatus("Full-Time")
                .contactInformation("jane.smith@example.com")
                .address("456 Elm St, Townsville")
                .build();

        Employee employee3 = Employee.builder()
                .fullName("Alice Johnson")
                .jobTitle("Product Manager")
                .department("Product Management")
                .hireDate(new Date())
                .employmentStatus("Full-Time")
                .contactInformation("alice.johnson@example.com")
                .address("789 Oak St, Villagetown")
                .build();

        repository.saveAll(List.of(employee1,employee2, employee3));
    };
    }
}
