package com.example.zad1.dto;

import com.example.zad1.model.CompanyStatistics;

public class CompanyStatisticsDTO {
    private String companyName;
    private long employeeCount;
    private double averageSalary;
    private int highestSalary;
    private String topEarnerFullName;

    public CompanyStatisticsDTO(){}

    public CompanyStatisticsDTO(String companyName, CompanyStatistics stats){
        this.companyName = companyName;
        this.employeeCount = stats.getEmployeeCount();
        this.averageSalary = stats.getAverageSalary();
        this.highestSalary = stats.getHighestSalary();
        this.topEarnerFullName = stats.getTopEarnerFullName();
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public long getEmployeeCount() {
        return employeeCount;
    }

    public void setEmployeeCount(int employeeCount) {
        this.employeeCount = employeeCount;
    }

    public double getAverageSalary() {
        return averageSalary;
    }

    public void setAverageSalary(double averageSalary) {
        this.averageSalary = averageSalary;
    }

    public int getHighestSalary() {
        return highestSalary;
    }

    public void setHighestSalary(int highestSalary) {
        this.highestSalary = highestSalary;
    }

    public String getTopEarnerFullName() {
        return topEarnerFullName;
    }

    public void setTopEarnerFullName(String topEarnerFullName) {
        this.topEarnerFullName = topEarnerFullName;
    }
}
