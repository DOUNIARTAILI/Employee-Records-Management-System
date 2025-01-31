package com.drtaili.security.employee;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue
    @Column(unique = true)
    private Long employeeId;

    @NotBlank(message = "Full name is required")
    @Size(max = 30, message = "Full name must be less than 30 characters")
    private String fullName;

    @NotBlank(message = "Job title is required")
    @Size(max = 30, message = "Job title must be less than 30 characters")
    private String jobTitle;

    @NotBlank(message = "Department is required")
    @Size(max = 30, message = "Department must be less than 30 characters")
    private String department;

    @NotNull(message = "Hire date is required")
    private Date hireDate;

    @NotBlank(message = "Employment status is required")
    @Size(max = 20, message = "Employment status must be less than 20 characters")
    private String employmentStatus;

    @NotBlank(message = "Contact information is required")
    @Size(max = 30, message = "Contact information must be less than 30 characters")
    private String contactInformation;

    @NotBlank(message = "Address is required")
    @Size(max = 100, message = "Address must be less than 100 characters")
    private String address;
}
