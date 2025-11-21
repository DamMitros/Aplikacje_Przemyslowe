package com.example.zad1.service;

import com.example.zad1.dao.DepartmentDAO;
import com.example.zad1.model.Department;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {
    @Mock
    private DepartmentDAO departmentDAO;

    @InjectMocks
    private DepartmentService departmentService;

    @BeforeEach
    void setUp() {
        departmentService.addDepartment(new Department(null, "IT", "TechCorp", "Gdańsk", 10000, "steczka@example.com"));
        departmentService.addDepartment(new Department(null, "Sales", "TechCorp", "Gdańsk", 100000, "manager@example.com"));
    }

    @Test
    void addDepartment_shouldAssignIdAndStore() {
        Department newDept = new Department(null, "R&D", "TechCorp", "Gdynia", 250000, "rnd@example.com");
        when(departmentDAO.save(any(Department.class))).thenReturn(newDept);
        Department saved = departmentService.addDepartment(newDept);

        assertNotNull(saved);
    }

    @Test
    void getDepartmentById_shouldReturnStoredDepartment() {
         Department dept = new Department(2L, "Sales", "TechCorp", "Gdańsk", 100000, "manager@example.com");
         when(departmentDAO.findById(2L)).thenReturn(Optional.of(dept));
         Optional<Department> found = departmentService.getDepartmentById(2L);
         assertAll(
                () -> assertTrue(found.isPresent()),
                () -> assertEquals("Sales", found.get().getName())
        );
    }

    @Test
    void getDepartmentById_shouldReturnEmptyForUnknownId() {
        when(departmentDAO.findById(999L)).thenReturn(Optional.empty());

        Optional<Department> dept = departmentService.getDepartmentById(999L);
        assertTrue(dept.isEmpty());
    }

    @Test
    void getAllDepartments_shouldReturnAll() {
        when(departmentDAO.findAll()).thenReturn(List.of(new Department(), new Department()));

        Collection<Department> departments = departmentService.getAllDepartments();
        assertEquals(2, departments.size());
    }

    @Test
    void updateDepartment_shouldUpdateIfFound() {
        Department existing = new Department(1L, "IT", "TechCorp", "Warsaw", 500000, "oldmanager@test.com");
        when(departmentDAO.findById(1L)).thenReturn(Optional.of(existing));
        when(departmentDAO.save(any(Department.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Department changes = new Department(null, "Information Technology", "TechnoCorp", "Warszawa-Centrum", 550000, "newmanager@test.com");

        when(departmentDAO.findById(1L)).thenReturn(Optional.of(existing));
        departmentService.updateDepartment(1L, changes);

        assertAll(
                () -> assertEquals("Information Technology", existing.getName()),
                () -> assertEquals("Warszawa-Centrum", existing.getLocation())
        );
    }

    @Test
    void updateDepartment_shouldReturnEmptyForUnknownId() {
        Department changes = new Department();
        when(departmentDAO.findById(999L)).thenReturn(Optional.empty());

        Optional<Department> updated = departmentService.updateDepartment(999L, changes);
        assertTrue(updated.isEmpty());
    }

    @Test
    void deleteDepartment_shouldRemoveDepartment() {
        when(departmentDAO.delete(1L)).thenReturn(true);
        boolean result = departmentService.deleteDepartment(1L);

        assertTrue(result);
    }
}