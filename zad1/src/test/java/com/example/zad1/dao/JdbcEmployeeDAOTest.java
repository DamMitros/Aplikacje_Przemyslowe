package com.example.zad1.dao;

import com.example.zad1.model.Employee;
import com.example.zad1.model.EmploymentStatus;
import com.example.zad1.model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@Import(JdbcEmployeeDAO.class)
public class JdbcEmployeeDAOTest {

    @Autowired
    private JdbcEmployeeDAO employeeDAO;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        employeeDAO.deleteAll();
    }

    @Test
    void shouldSaveAndFindEmployee() {
        Employee emp = new Employee("Jan Kowalski", "jan@test.com", "TechCorp", Position.PROGRAMISTA, 10000);
        employeeDAO.save(emp);

        Optional<Employee> found = employeeDAO.findByEmail("jan@test.com");
        assertTrue(found.isPresent());
        assertEquals("Jan Kowalski", found.get().getFullName());
        assertEquals(Position.PROGRAMISTA, found.get().getPosition());
    }

    @Test
    void shouldUpdateExistingEmployee() {
        Employee emp = new Employee("Anna Nowak", "anna@test.com", "TechCorp", Position.MANAGER, 12000);
        employeeDAO.save(emp);

        emp.setSalary(15000);
        emp.setStatus(EmploymentStatus.ON_LEAVE);
        employeeDAO.save(emp);

        Optional<Employee> updated = employeeDAO.findByEmail("anna@test.com");
        assertTrue(updated.isPresent());
        assertEquals(15000, updated.get().getSalary());
        assertEquals(EmploymentStatus.ON_LEAVE, updated.get().getStatus());
    }

    @Test
    void shouldDeleteEmployee() {
        Employee emp = new Employee("Piotr Lis", "piotr@test.com", "TechCorp", Position.STAZYSTA, 3000);
        employeeDAO.save(emp);

        boolean deleted = employeeDAO.delete("piotr@test.com");
        assertTrue(deleted);

        Optional<Employee> found = employeeDAO.findByEmail("piotr@test.com");
        assertTrue(found.isEmpty());
    }

    @Test
    void shouldFindAll() {
        employeeDAO.save(new Employee("A A", "a@a.com", "C1", Position.PREZES, 20000));
        employeeDAO.save(new Employee("B B", "b@b.com", "C1", Position.WICEPREZES, 15000));

        List<Employee> all = employeeDAO.findAll();
        assertEquals(2, all.size());
    }
}