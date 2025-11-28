package com.example.zad1.service;

import com.example.zad1.model.Department;
import com.example.zad1.repository.DepartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;

@Service
public class DepartmentService {
    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public Collection<Department> getAllDepartments(){
        return departmentRepository.findAll();
    }

    public Optional<Department> getDepartmentById(Long id){
        return departmentRepository.findById(id);
    }

    @Transactional
    public Department addDepartment(Department department) {
        return departmentRepository.save(department);
    }

    @Transactional
    public Optional<Department> updateDepartment(Long id, Department changes) {
        return departmentRepository.findById(id).map(existing -> {
            existing.setName(changes.getName());
            existing.setLocation(changes.getLocation());
            existing.setBudget(changes.getBudget());
            existing.setManagerEmail(changes.getManagerEmail());
            departmentRepository.save(existing);
            return existing;
        });
    }

    @Transactional
    public boolean deleteDepartment(Long id) {
        if (departmentRepository.existsById(id)) {
            departmentRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
