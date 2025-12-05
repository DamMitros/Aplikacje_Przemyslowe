package com.example.zad1.dto;

import com.example.zad1.validation.TechCorpEmail;
import jakarta.validation.constraints.*;

public class EmployeeDTO {
    @NotEmpty(message = "Imię jest wymagane")
    @Size(min = 2, message="Imię musi mieć co najmniej 2 znaki")
    private String firstName;

    @NotEmpty(message="Nazwisko jest wymagane")
    @Size(min=2, message = "Nazwisko musi mieć co najmniej 2 znaki")
    private String lastName;

    @NotEmpty(message="Email jest wymagany")
    @Email(message = "Nieprawidłowy format adresu email")
    @TechCorpEmail(message = "Tylko adresy w domenie @techcorp.com sa akceptowane")
    private String email;

    @NotEmpty(message = "Nazwa firmy jest wymagana")
    private String company;

    @NotEmpty(message = "Stanowisko jest wymagane")
    private String position;

    @Positive(message = "Pensja musi być wartośćią dodatnią")
    @NotNull(message = "Pensja jest wymagana")
    private Integer salary;

    @NotEmpty(message = "Status jest wymagany")
    private String status;

    public EmployeeDTO() {}

    public EmployeeDTO(String firstName, String lastName, String email, String company, String position, Integer salary, String status) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.company = company;
        this.position = position;
        this.salary = salary;
        this.status = status;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Integer getSalary() {
        return salary;
    }

    public void setSalary(Integer salary) {
        this.salary = salary;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
