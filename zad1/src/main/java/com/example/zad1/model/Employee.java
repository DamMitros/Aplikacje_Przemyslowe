package com.example.zad1.model;

import java.util.Objects;

public class Employee {
    private String fullName;
    private String email;
    private String companyName;
    private Position position;
    private int salary;
    private EmploymentStatus status = EmploymentStatus.ACTIVE;

    public Employee(String fullName, String email, String companyName, Position position, int salary) {
        this.fullName = fullName;
        this.email = email;
        this.companyName = companyName;
        this.position = position;
        this.salary = salary;
    }

    public String getFullName() {
        return fullName;
    }
    public String getLastName() {
        if (fullName == null || fullName.isBlank())
            return "";
        String[] parts = fullName.trim().split("\\s+");
        return parts.length > 1 ? parts[parts.length - 1] : "";
    }
    public String getFirstName() {
        if (fullName == null || fullName.isBlank())
            return "";
        String[] parts = fullName.trim().split("\\s+");
        return parts[0];
    }
    public String getEmail() {
        return email;
    }
    public String getCompanyName() {
        return companyName;
    }
    public Position getPosition() {
        return position;
    }
    public int getSalary() {
        return salary;
    }
    public EmploymentStatus getStatus() {
        return status;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
    public void setPosition(Position position) {
        this.position = position;
    }
    public void setSalary(int salary) {
        this.salary = salary;
    }
    public void setStatus(EmploymentStatus status) {
        this.status = status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Employee that))
            return false;
        return this.email.equals(that.email);
    }

    @Override
    public String toString() {
        return "Employee [fullName=" + fullName + ", email=" + email + ", companyName=" + companyName + ", position="
                + position + ", salary=" + salary + "]";
    }
}
