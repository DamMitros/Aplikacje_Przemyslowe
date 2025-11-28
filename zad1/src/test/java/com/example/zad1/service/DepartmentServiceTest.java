package com.example.zad1.service;

import com.example.zad1.model.Department;
import com.example.zad1.repository.DepartmentRepository;
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
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DepartmentService departmentService;

    @BeforeEach
    void setUp() {
        departmentService.addDepartment(new Department("IT", "TechCorp", "Gdańsk", 10000, "steczka@example.com"));
        departmentService.addDepartment(new Department("Sales", "TechCorp", "Gdańsk", 100000, "manager@example.com"));
    }

    @Test
    void addDepartment_shouldAssignIdAndStore() {
        Department input = new Department();
        input.setName("Test");

        Department saved = new Department();
        saved.setId(1L);
        saved.setName("Test");

        when(departmentRepository.save(any(Department.class))).thenReturn(saved);

        Department result = departmentService.addDepartment(input);

        assertAll(
                ()-> assertNotNull(result),
                ()-> assertEquals(1L, result.getId())
        );
    }

    @Test
    void getDepartmentById_shouldReturnStoredDepartment() {
         Department dept = new Department("Sales", "TechCorp", "Gdańsk", 100000, "manager@example.com");
         when(departmentRepository.findById(2L)).thenReturn(Optional.of(dept));
         Optional<Department> found = departmentService.getDepartmentById(2L);
         assertAll(
                () -> assertTrue(found.isPresent()),
                () -> assertEquals("Sales", found.get().getName())
        );
    }

    @Test
    void getDepartmentById_shouldReturnEmptyForUnknownId() {
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Department> dept = departmentService.getDepartmentById(999L);
        assertTrue(dept.isEmpty());
    }

    @Test
    void getAllDepartments_shouldReturnAll() {
        when(departmentRepository.findAll()).thenReturn(List.of(new Department(), new Department()));

        Collection<Department> departments = departmentService.getAllDepartments();
        assertEquals(2, departments.size());
    }

    @Test
    void updateDepartment_shouldUpdateIfFound() {
        Department existing = new Department("IT", "TechCorp", "Warsaw", 500000, "oldmanager@test.com");
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Department changes = new Department("Information Technology", "TechnoCorp", "Warszawa-Centrum", 550000, "newmanager@test.com");

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(existing));
        departmentService.updateDepartment(1L, changes);

        assertAll(
                () -> assertEquals("Information Technology", existing.getName()),
                () -> assertEquals("Warszawa-Centrum", existing.getLocation())
        );
    }

    @Test
    void updateDepartment_shouldReturnEmptyForUnknownId() {
        Department changes = new Department();
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Department> updated = departmentService.updateDepartment(999L, changes);
        assertTrue(updated.isEmpty());
    }

    @Test
    void deleteDepartment_shouldRemoveDepartment() {
        when(departmentRepository.existsById(1L)).thenReturn(true);
        boolean result = departmentService.deleteDepartment(1L);

        assertTrue(result);
    }
}