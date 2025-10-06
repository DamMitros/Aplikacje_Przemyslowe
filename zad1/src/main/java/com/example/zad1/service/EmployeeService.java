package com.example.zad1.service;

import com.example.zad1.model.Employee;
import com.example.zad1.model.Position;

import java.util.*;
import java.util.stream.Collectors;

public class EmployeeService {
    private final Set<Employee> employees = new HashSet<>();

    public boolean addEmployee(Employee employee) {
        if (employee==null || employee.getEmail()==null || employee.getEmail().isBlank())
            return false;
        return employees.add(employee);
    }

    public void displayAll(){
       if (employees.isEmpty()){
           System.out.println("No employees to display.");
           return;
       }
       employees.forEach(System.out::println);
    }

    public List<Employee> getEmployeeByCompany(String companyName){
        if (companyName==null || companyName.isBlank())
            return Collections.emptyList();
        return employees.stream()
                .filter(emp ->
                        emp.getCompanyName() != null &&
                        emp.getCompanyName().equalsIgnoreCase(companyName))
                .collect(Collectors.toList());
    }

    public List<Employee> getAlphabetically(){
        return employees.stream()
                .sorted(Comparator
                        .comparing(Employee::getLastName)
                        .thenComparing(Employee::getFirstName))
                .collect(Collectors.toList());
    }

    public Map<Position, List<Employee>> groupByPosition(){
        return employees.stream().collect(Collectors.groupingBy(Employee::getPosition));
    }

    public Map<Position, Integer> countByPosition(){
        return employees.stream().collect(Collectors.groupingBy(Employee::getPosition, Collectors.summingInt(e -> 1)));
    }

    public double getAverageSalary() {
        return employees.stream()
                .mapToInt(Employee::getSalary)
                .average()
                .orElse(0.0);
    }

    public Optional<Employee> getHighestSalary() {
        return employees.stream().max(Comparator.comparingInt(Employee::getSalary));
    }
}
