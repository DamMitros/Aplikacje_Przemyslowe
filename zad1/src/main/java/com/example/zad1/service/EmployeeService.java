package com.example.zad1.service;

import com.example.zad1.dao.EmployeeDAO;
import com.example.zad1.model.CompanyStatistics;
import com.example.zad1.model.Employee;
import com.example.zad1.model.Position;
import com.example.zad1.model.EmploymentStatus;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService {
    private final EmployeeDAO employeeDAO;

    public EmployeeService(EmployeeDAO employeeDAO) {
        this.employeeDAO = employeeDAO;
    }

    @Transactional
    public boolean addEmployee(Employee employee){
        if (employee==null || employee.getEmail()==null || employee.getEmail().isBlank())
            return false;
        if (employeeDAO.findByEmail(employee.getEmail()).isPresent()) {
            return false;
        }
        employeeDAO.save(employee);
        return true;
    }

    public List<Employee> getAllEmployees(){
        return employeeDAO.findAll();
    }

    public Optional<Employee> getEmployeeByEmail(String email) {
        if (email == null || email.isBlank()) return Optional.empty();
        return employeeDAO.findByEmail(email);
    }

    public Optional<Employee> UpdateEmployeeByEmail(String email, Employee changes) {
        Optional<Employee> existing = employeeDAO.findByEmail(email);
        if (existing.isPresent()) {
            Employee emp = existing.get();
            emp.setFullName(changes.getFullName());
            emp.setCompanyName(changes.getCompanyName());
            emp.setPosition(changes.getPosition());
            emp.setSalary(changes.getSalary());
            emp.setStatus(changes.getStatus());
            emp.setDepartmentId(changes.getDepartmentId());

            employeeDAO.save(emp);
            return Optional.of(emp);
        }
        return Optional.empty();
    }

    @Transactional
    public Optional<Employee> updateStatusByEmail(String email, EmploymentStatus status){
        Optional<Employee> existing = employeeDAO.findByEmail(email);
        if (existing.isPresent()) {
            Employee emp = existing.get();
            emp.setStatus(status);
            employeeDAO.save(emp);
            return Optional.of(emp);
        }
        return Optional.empty();
    }

    @Transactional
    public boolean deleteEmployeeByEmail(String email) {
        return employeeDAO.delete(email);
    }

    @Transactional
    public Map<String, CompanyStatistics> getCompanyStatistics() {
        List<CompanyStatistics> statsList = employeeDAO.getCompanyStatistics();
        Map<String, CompanyStatistics> statsMap = new HashMap<>();

        return getAllEmployees().stream().filter(Objects::nonNull)
                .filter(emp -> emp.getCompanyName() != null && !emp.getCompanyName().isBlank())
                .collect(Collectors.groupingBy(
                        Employee::getCompanyName,
                        Collectors.collectingAndThen(Collectors.toList(), list -> {
                            int count=list.size();
                            double avg=list.stream().mapToInt(Employee::getSalary).average().orElse(0.0);
                            Employee topEarner=list.stream()
                                    .max(Comparator.comparingInt(Employee::getSalary))
                                    .orElse(null);
                            String topEarnerName=topEarner!=null ? topEarner.getFullName() : "";
                            int highestSalary=0;
                            if (topEarnerName != null) {
                                assert topEarner != null;
                                highestSalary = topEarner.getSalary();
                            }
                            return new CompanyStatistics(count, avg, topEarnerName, highestSalary);
                        })
                ));
    }

    public List<Employee> getEmployeeByCompany(String companyName){
        if (companyName==null) return Collections.emptyList();
        return getAllEmployees().stream()
                .filter(emp -> companyName.equalsIgnoreCase(emp.getCompanyName()))
                .collect(Collectors.toList());
    }

    public double getAverageSalary() {
        return getAllEmployees().stream()
                .mapToInt(Employee::getSalary)
                .average()
                .orElse(0.0);
    }

    public double getAverageSalaryByCompany(String companyName) {
        return getEmployeeByCompany(companyName).stream()
                .mapToInt(Employee::getSalary)
                .average().orElse(0.0);
    }

    public Optional<Employee> getHighestSalary() {
        return getAllEmployees().stream()
                .max(Comparator.comparingInt(Employee::getSalary));
    }

    public List<Employee> validateSalaryConsistency(){
        return getAllEmployees().stream()
                .filter(Objects::nonNull)
                .filter(emp -> emp.getPosition() != null)
                .filter(emp -> emp.getSalary() < emp.getPosition().getSalary())
                .collect(Collectors.toList());
    }

    public Map<Position, List<Employee>> groupByPosition(){
        return getAllEmployees().stream()
                .collect(Collectors.groupingBy(Employee::getPosition));
    }

    public Map<Position, Integer> countByPosition(){
        return getAllEmployees().stream()
                .collect(Collectors.groupingBy(
                        Employee::getPosition,
                        Collectors.summingInt(e -> 1)
                ));
    }

    public Map<EmploymentStatus, Integer> countByStatus() {
        return getAllEmployees().stream()
                .collect(Collectors.groupingBy(
                        Employee::getStatus,
                        Collectors.summingInt(e -> 1)
                ));
    }

    public List<Employee> getByStatus(EmploymentStatus status) {
        if (status == null) return Collections.emptyList();
        return getAllEmployees().stream()
                .filter(e -> e.getStatus() == status)
                .collect(Collectors.toList());
    }

    public List<Employee> getAllAlphabetically(){
        return getAllEmployees().stream()
                .sorted(Comparator
                        .comparing(Employee::getLastName)
                        .thenComparing(Employee::getFirstName))
                .collect(Collectors.toList());
    }

    public void displayAll(){
        if (getAllEmployees().isEmpty()){
            System.out.println("No employees to display.");
            return;
        }
        getAllEmployees().forEach(System.out::println);
    }

    @Transactional
    public void deleteAll() {
        employeeDAO.deleteAll();
    }
//    private final Set<Employee> employees = new HashSet<>();
//
//    public boolean addEmployee(Employee employee) {
//        if (employee==null || employee.getEmail()==null || employee.getEmail().isBlank())
//            return false;
//        return employees.add(employee);
//    }
//    public List<Employee> getAllEmployees() {
//        return new ArrayList<>(employees);
//    }

//    public Optional<Employee> UpdateEmployeeByEmail(String email, Employee changes) {
//        if (email == null || email.isBlank() || changes == null) return Optional.empty();
//        Optional<Employee> empOpt = GetEmployeeByEmail(email);
//        empOpt.ifPresent(emp -> {
//            emp.setFullName(changes.getFullName());
//            emp.setCompanyName(changes.getCompanyName());
//            emp.setPosition(changes.getPosition());
//            emp.setSalary(changes.getSalary());
//            emp.setStatus(changes.getStatus());
//        });
//        return empOpt;
//    }
//    public Optional<Employee> updateStatusByEmail(String email, EmploymentStatus status){
//        if (email==null || email.isBlank() || status==null) return Optional.empty();
//        Optional<Employee> empOpt = GetEmployeeByEmail(email);
//        empOpt.ifPresent(emp -> emp.setStatus(status));
//        return empOpt;
//    }
//    public boolean deleteEmployeeByEmail(String email) {
//        if (email==null || email.isBlank()) return false;
//        return employees.removeIf(emp -> email.equalsIgnoreCase(emp.getEmail()));
//    }
//    public List<Employee> getEmployeeByCompany(String companyName){
//        if (companyName==null || companyName.isBlank())
//            return Collections.emptyList();
//        return employees.stream()
//                .filter(emp ->
//                        emp.getCompanyName() != null &&
//                        emp.getCompanyName().equalsIgnoreCase(companyName))
//                .collect(Collectors.toList());
//    }
//    public Map<String, CompanyStatistics> getCompanyStatistics() {
//        return employees.stream()
//                .filter(Objects::nonNull)
//                .filter(emp -> emp.getCompanyName() != null && !emp.getCompanyName().isBlank())
//                .collect(Collectors.groupingBy(
//                        Employee::getCompanyName,
//                        Collectors.collectingAndThen(Collectors.toList(), list -> {
//                            int count = list.size();
//                            double avg = list.stream().mapToInt(Employee::getSalary).average().orElse(0.0);
//                            Employee topEarner = list.stream()
//                                    .max(Comparator.comparingInt(Employee::getSalary))
//                                    .orElse(null);
//                            String topEarnerName = topEarner != null ? topEarner.getFullName() : "";
//                            int highestSalary = 0;
//                            if (topEarnerName != null) {
//                                assert topEarner != null;
//                                highestSalary = topEarner.getSalary();
//                            }
//
//                            return new CompanyStatistics(count, avg, topEarnerName, highestSalary);
//                        })
//                ));
//    }
}
