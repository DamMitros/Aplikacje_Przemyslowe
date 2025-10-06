package com.example.zad1;

import com.example.zad1.model.Employee;
import com.example.zad1.model.Position;

import com.example.zad1.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EmployeeServiceTest {
    private EmployeeService employeeService;
    Employee emp = new Employee("Justyna Steczkowska", "steczkowska1764@gmail.com", "TechCorp", Position.PREZES, Position.PREZES.getSalary());
    Employee emp2 = new Employee("Edyta Gorniak", "edyth@gmail.com", "TechCorp", Position.WICEPREZES, Position.WICEPREZES.getSalary());
    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService();
    }

    @Test
    void testAddEmployee() {
        assertTrue(employeeService.addEmployee(emp));
    }

    @Test
    void testAddEmployee_DuplicateEmail() {
        employeeService.addEmployee(emp);
        assertFalse(employeeService.addEmployee(emp));
    }

    @Test
    void testNullEmployee() {
        assertFalse(employeeService.addEmployee(null));
    }

    @Test
    void testEmptyMail() {
        Employee emp2 = new Employee("Anna Nowak", "", "TechCorp", Position.WICEPREZES, Position.WICEPREZES.getSalary());
        assertFalse(employeeService.addEmployee(emp2));
    }

    @Test
    void testGetHighestSalary_NoEmployees() {
        assertTrue(employeeService.getHighestSalary().isEmpty());
    }

    @Test
    void testGetHighestSalary_WithEmployees() {
        employeeService.addEmployee(emp);
        employeeService.getHighestSalary();
        assertFalse(employeeService.getHighestSalary().isEmpty());
    }

    @Test
    void testGetAverageSalary_NoEmployees() {
        assertEquals(0.0, employeeService.getAverageSalary());
    }

    @Test
    void testGetAverageSalary_WithEmployees() {
        employeeService.addEmployee(emp);
        assertEquals(Position.PREZES.getSalary(), employeeService.getAverageSalary());
    }

    @Test
    void testGetEmployeeByCompany_NoCompany() {
        assertTrue(employeeService.getEmployeeByCompany("NonExistent").isEmpty());
    }

    @Test
    void testGetEmployeeByCompany_WithCompany() {
        employeeService.addEmployee(emp);
        assertFalse(employeeService.getEmployeeByCompany("TechCorp").isEmpty());
    }

    @Test
    void testGetAlphabetically() {
        employeeService.addEmployee(emp);
        employeeService.addEmployee(emp2);
        assertEquals(2, employeeService.getAlphabetically().size());
        assertEquals(emp2, employeeService.getAlphabetically().get(0));
    }

    @Test
    void testGroupByPosition() {
        employeeService.addEmployee(emp);
        employeeService.addEmployee(emp2);
        assertEquals(2, employeeService.groupByPosition().size());
        assertTrue(employeeService.groupByPosition().containsKey(Position.PREZES));
        assertTrue(employeeService.groupByPosition().containsKey(Position.WICEPREZES));
    }

    @Test
    void testCountByPosition() {
        employeeService.addEmployee(emp);
        employeeService.addEmployee(emp2);
        assertEquals(2, employeeService.countByPosition().size());
        assertEquals(1, employeeService.countByPosition().get(Position.PREZES));
        assertEquals(1, employeeService.countByPosition().get(Position.WICEPREZES));
    }
}
