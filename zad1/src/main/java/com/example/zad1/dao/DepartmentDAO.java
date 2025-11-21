package com.example.zad1.dao;

import com.example.zad1.model.Department;
import java.util.List;
import java.util.Optional;

public interface DepartmentDAO {
    List<Department> findAll();
    Optional<Department> findById(Long id);
    Department save(Department department);
    boolean delete(Long id);
}