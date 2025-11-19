package com.example.zad1.service;

import com.example.zad1.model.Department;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DepartmentServiceTest {

    private DepartmentService departmentService;

    @BeforeEach
    void setUp() {
        departmentService = new DepartmentService();
        departmentService.addDepartment(new Department(null, "IT", "TechCorp", "Gdańsk", 10000, "steczka@example.com"));
        departmentService.addDepartment(new Department(null, "Sales", "TechCorp", "Gdańsk", 100000, "manager@example.com"));
    }

    @Test
    void addDepartment_shouldAssignIdAndStore() {
        int initialSize = departmentService.getAllDepartments().size();
        Department newDept = new Department(null, "R&D", "TechCorp", "Gdynia", 250000, "rnd@example.com");
        Department savedDept = departmentService.addDepartment(newDept);

        assertAll(
                () -> assertNotNull(savedDept.getId()),
                () -> assertEquals("R&D", savedDept.getName()),
                () -> assertEquals(initialSize + 1, departmentService.getAllDepartments().size())
        );
    }

    @Test
    void getDepartmentById_shouldReturnStoredDepartment() {
        Optional<Department> found = departmentService.getDepartmentById(2L);
        assertAll(
                () -> assertTrue(found.isPresent()),
                () -> assertEquals("Sales", found.get().getName())
        );
    }

    @Test
    void getDepartmentById_shouldReturnCorrectDepartment() {
        Optional<Department> dept = departmentService.getDepartmentById(1L);
        assertTrue(dept.isPresent());
        assertEquals("IT", dept.get().getName());
    }

    @Test
    void getDepartmentById_shouldReturnEmptyForUnknownId() {
        Optional<Department> dept = departmentService.getDepartmentById(999L);
        assertTrue(dept.isEmpty());
    }

    @Test
    void getAllDepartments_shouldReturnAll() {
        Collection<Department> departments = departmentService.getAllDepartments();
        assertEquals(2, departments.size());
    }

    @Test
    void updateDepartment_shouldChangeData() {
        Department changes = new Department(null, "Information Technology","TechnoCorp", "Warszawa-Centrum", 550000, "new-manager@example.com");
        Optional<Department> updated = departmentService.updateDepartment(1L, changes);

        assertTrue(updated.isPresent());
        assertEquals("Information Technology", updated.get().getName());
        assertEquals("Warszawa-Centrum", updated.get().getLocation());

        Optional<Department> refetched = departmentService.getDepartmentById(1L);
        assertTrue(refetched.isPresent());
        assertEquals("Information Technology", refetched.get().getName());
    }

    @Test
    void updateDepartment_shouldReturnEmptyForUnknownId() {
        Department changes = new Department(null, "Test","Test", "Test", 1, null);
        Optional<Department> updated = departmentService.updateDepartment(999L, changes);
        assertTrue(updated.isEmpty());
    }

    @Test
    void deleteDepartment_shouldRemoveDepartment() {
        assertTrue(departmentService.deleteDepartment(1L));
        assertEquals(1, departmentService.getAllDepartments().size());

        Optional<Department> deleted = departmentService.getDepartmentById(1L);
        assertTrue(deleted.isEmpty());
    }

    @Test
    void deleteDepartment_shouldReturnFalseForUnknownId() {
        assertFalse(departmentService.deleteDepartment(999L));
        assertEquals(2, departmentService.getAllDepartments().size());
    }
}