package com.example.zad1.dao;

import com.example.zad1.model.CompanyStatistics;
import com.example.zad1.model.Employee;
import java.util.List;
import java.util.Optional;

public interface EmployeeDAO {
    List<Employee> findAll();
    Optional<Employee> findByEmail(String email);
    void save(Employee employee);
    boolean delete(String email);
    void deleteAll();
    List<CompanyStatistics> getCompanyStatistics();
}