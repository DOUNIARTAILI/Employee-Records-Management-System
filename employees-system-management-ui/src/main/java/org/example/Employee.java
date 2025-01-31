package org.example;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
@JsonIgnoreProperties(ignoreUnknown = true)
public class Employee {
    private Long employeeId;
    private String fullName;
    private String jobTitle;
    private String department;
    private Date hireDate;
    private String employmentStatus;
    private String contactInformation;
    private String address;
    public Employee(){}
    public Employee(Long employeeId, String fullName, String jobTitle, String department,
                    Date hireDate, String employmentStatus, String contactInformation, String address) {
        this.employeeId = employeeId;
        this.fullName = fullName;
        this.jobTitle = jobTitle;
        this.department = department;
        this.hireDate = hireDate;
        this.employmentStatus = employmentStatus;
        this.contactInformation = contactInformation;
        this.address = address;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getDepartment() {
        return department;
    }

    public Date getHireDate() {
        return hireDate;
    }

    public String getEmploymentStatus() {
        return employmentStatus;
    }

    public String getContactInformation() {
        return contactInformation;
    }

    public String getAddress() {
        return address;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setHireDate(Date hireDate) {
        this.hireDate = hireDate;
    }

    public void setEmploymentStatus(String employmentStatus) {
        this.employmentStatus = employmentStatus;
    }

    public void setContactInformation(String contactInformation) {
        this.contactInformation = contactInformation;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}