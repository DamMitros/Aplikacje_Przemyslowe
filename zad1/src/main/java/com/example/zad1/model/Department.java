package com.example.zad1.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

@Entity
@Table(name = "departments")
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "Nazwa departamentu jest wymagana")
    @Size(min = 2, message = "Nazwa musi mieć co najmniej 2 znaki")
    private String name;

    @NotEmpty(message="Nazwa firmy jest wymagana")
    private String companyName;

    @NotEmpty(message = "Lokalizacja departamentu jest wymagana")
    private String location;

    @Min(value = 0, message="Budżet departamentu nie może być ujemny")
    private double budget;

    private String managerEmail;

    @OneToMany(mappedBy = "department")
    private List<Employee> employees;

    public Department() {}

    public Department(String name, String companyName, String location, double budget, String managerEmail) {
        this.name = name;
        this.companyName = companyName;
        this.location = location;
        this.budget = budget;
        this.managerEmail = managerEmail;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public String getManagerEmail() {
        return managerEmail;
    }

    public void setManagerEmail(String managerEmail) {
        this.managerEmail = managerEmail;
    }
}
