package com.example.zad1.service;

import com.example.zad1.dao.DepartmentDAO;
import com.example.zad1.model.Department;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;

@Service
public class DepartmentService {
    private final DepartmentDAO departmentDAO;

    public DepartmentService(DepartmentDAO departmentDAO) {
        this.departmentDAO = departmentDAO;
    }

    public Collection<Department> getAllDepartments(){
        return departmentDAO.findAll();
    }

    public Optional<Department> getDepartmentById(Long id){
        return departmentDAO.findById(id);
    }

    @Transactional
    public Department addDepartment(Department department) {
        return departmentDAO.save(department);
    }

    @Transactional
    public Optional<Department> updateDepartment(Long id, Department changes) {
        return departmentDAO.findById(id).map(existing -> {
            existing.setName(changes.getName());
            existing.setLocation(changes.getLocation());
            existing.setBudget(changes.getBudget());
            existing.setManagerEmail(changes.getManagerEmail());
            departmentDAO.save(existing);
            return existing;
        });
    }

    @Transactional
    public boolean deleteDepartment(Long id) {
        return departmentDAO.delete(id);
    }
}
