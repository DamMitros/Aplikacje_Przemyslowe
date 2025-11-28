package com.example.zad1.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message="Imię i nazwisko jest wymagane")
    private String fullName;

    @Email(message="Nieprawidłowy format adresu email")
    @NotEmpty(message="Adres email jest wymagany")
    @Column(unique = true)
    private String email;

    private String companyName;

    @Enumerated(EnumType.STRING)
    private Position position;

    @Min(value = 0, message = "Wynagrodzenie nie może być ujemne")
    private int salary;

    @Enumerated(EnumType.STRING)
    private EmploymentStatus status = EmploymentStatus.ACTIVE;

    private String photoFileName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    public Employee() {}

    public Employee(String fullName, String email, String companyName, Position position, int salary) {
        this.fullName = fullName;
        this.email = email;
        this.companyName = companyName;
        this.position = position;
        this.salary = salary;
    }

    public Long getId() {
        return id;
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
    public String getPhotoFileName() {
        return photoFileName;
    }
    public Department getDepartment() {
        return department;
    }
    public Long getDepartmentId() {
        return department != null ? department.getId() : null;
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
    public void setPhotoFileName(String photoFileName) {
        this.photoFileName = photoFileName;
    }
    public void setDepartment(Department department) {
        this.department = department;
    }
    public void setDepartmentId(Long departmentId) {
        if (departmentId == null) {
            this.department = null;
        } else {
            if (this.department == null || !departmentId.equals(this.department.getId())) {
                Department dept = new Department();
                dept.setId(departmentId);
                this.department = dept;
            }
        }
    }

    @Override
    public String toString() {
        return "Employee [fullName=" + fullName + ", email=" + email + ", companyName=" + companyName + ", position="
                + position + ", salary=" + salary + "]";
    }
}
