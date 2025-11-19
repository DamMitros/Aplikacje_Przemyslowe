package com.example.zad1.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;

public class Department {
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
    public Department() {}

    public Department(Long id, String name, String companyName, String location, double budget, String managerEmail) {
        this.id = id;
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
