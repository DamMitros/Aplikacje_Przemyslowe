package com.example.zad1.model;

public class CompanyStatistics {
    private final String companyName;
    private final long employeeCount;
    private final double averageSalary;
    private String topEarnerFullName;
    private final int highestSalary;

    public CompanyStatistics(String companyName, long employeeCount, double averageSalary, int highestSalary) {
        this.companyName = companyName;
        this.employeeCount = employeeCount;
        this.averageSalary = averageSalary;
        this.highestSalary = highestSalary;
        this.topEarnerFullName = "";
    }

    public CompanyStatistics(String companyName, long employeeCount, double averageSalary, String topEarnerFullName, int highestSalary) {
        this.companyName = companyName;
        this.employeeCount = employeeCount;
        this.averageSalary = averageSalary;
        this.topEarnerFullName = topEarnerFullName != null ? topEarnerFullName : "";
        this.highestSalary = highestSalary;
    }

    public String getCompanyName() { return companyName; }
    public long getEmployeeCount() { return employeeCount; }
    public double getAverageSalary() { return averageSalary; }
    public String getTopEarnerFullName() { return topEarnerFullName; }
    public int getHighestSalary() { return highestSalary; }

    public void setTopEarnerFullName(String topEarnerFullName) { this.topEarnerFullName = topEarnerFullName; }

    @Override
    public String toString(){
        return "CompanyStatistics{" +
                "companyName='" + companyName + '\'' +
                ", employeeCount=" + employeeCount +
                ", averageSalary=" + averageSalary +
                ", highestSalary=" + highestSalary +
                ", topEarnerFullName='" + topEarnerFullName + '\'' +
                '}';
    }
}
