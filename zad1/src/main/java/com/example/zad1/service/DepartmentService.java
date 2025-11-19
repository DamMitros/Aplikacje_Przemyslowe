package com.example.zad1.service;

import com.example.zad1.model.Department;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class DepartmentService {
    private final Map<Long, Department> departments = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong();

    public DepartmentService() {}

    public Collection<Department> getAllDepartments() {
        return departments.values();
    }

    public Optional<Department> getDepartmentById(Long id) {
        return Optional.ofNullable(departments.get(id));
    }

    public Department addDepartment(Department department) {
        Long id = idCounter.incrementAndGet();
        department.setId(id);
        departments.put(id, department);
        return department;
    }

    public Optional<Department> updateDepartment(Long id, Department changes) {
        return getDepartmentById(id).map(existing -> {
            existing.setName(changes.getName());
            existing.setLocation(changes.getLocation());
            existing.setBudget(changes.getBudget());
            existing.setManagerEmail(changes.getManagerEmail());
            departments.put(id, existing);
            return existing;
        });
    }

    public boolean deleteDepartment(Long id) {
        return departments.remove(id) != null;
    }
}
