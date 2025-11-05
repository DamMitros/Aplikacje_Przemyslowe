package com.example.zad1.model;

public class CompanyStatistics {
    private final int employeeCount;
    private final double averageSalary;
    private final String topEarnerFullName;
    private final int highestSalary;

    public CompanyStatistics(int employeeCount, double averageSalary, String topEarnerFullName, int highestSalary) {
        this.employeeCount = employeeCount;
        this.averageSalary = averageSalary;
        this.topEarnerFullName = topEarnerFullName;
        this.highestSalary = highestSalary;
    }

    public int getEmployeeCount() {
        return employeeCount;
    }

    public double getAverageSalary() {
        return averageSalary;
    }

    public String getTopEarnerFullName() {
        return topEarnerFullName;
    }
    public int getHighestSalary() {
        return highestSalary;
    }

    @Override
    public String toString(){
        return "CompanyStatistics{employeeCount=" + employeeCount +
                ", averageSalary=" + averageSalary +
                ", topEarnerFullName='" + topEarnerFullName + "'}";
    }
}
