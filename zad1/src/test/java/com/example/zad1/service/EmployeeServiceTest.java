package com.example.zad1.service;

import com.example.zad1.model.Employee;
import com.example.zad1.model.Position;

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
    void shouldAddEmployeeWhenEmployeeValid() {
        assertTrue(employeeService.addEmployee(emp));
    }

    @Test
    void shouldNotAddEmployeeWhenEmployeeDuplicate() {
        employeeService.addEmployee(emp);
        assertFalse(employeeService.addEmployee(emp));
    }

    @Test
    void shouldNotAddEmployeeWhenEmployeeIsNull() {
        assertFalse(employeeService.addEmployee(null));
    }

    @Test
    void shouldNotAddEmployeeWhenEmailIsNull() {
        Employee empInvalidEmail = new Employee("Anna Nowak", null, "TechCorp", Position.WICEPREZES, Position.WICEPREZES.getSalary());
        assertFalse(employeeService.addEmployee(empInvalidEmail));
    }


    @Test
    void shouldNotAddEmployeeWhenEmailIsBlank() {
        Employee emp2 = new Employee("Anna Nowak", "", "TechCorp", Position.WICEPREZES, Position.WICEPREZES.getSalary());
        assertFalse(employeeService.addEmployee(emp2));
    }

    @Test
    void displayAllWhenNoEmployees() {
        employeeService.displayAll();
    }

    @Test
    void displayAllWhenEmployeesExist() {
        employeeService.addEmployee(emp);
        employeeService.addEmployee(emp2);
        employeeService.displayAll();
    }

    @Test
    void getEmployeeByCompany_WhenNonExistingCompany() {
        assertTrue(employeeService.getEmployeeByCompany("NonExistent").isEmpty());
    }

    @Test
    void getEmployeeByCompany_WhenExistingCompany() {
        employeeService.addEmployee(emp);
        assertFalse(employeeService.getEmployeeByCompany("TechCorp").isEmpty());
    }

    @Test
    void getEmployeeByCompanyWhenCompanyNameIsNull() {
        assertTrue(employeeService.getEmployeeByCompany(null).isEmpty());
    }

    @Test
    void getEmployeeByCompany_WhenCompanyIsBlank() {
        assertTrue(employeeService.getEmployeeByCompany("").isEmpty());
    }

    @Test
    void getEmployeeByCompany_WhenEmployeeHasNullCompany() {
        Employee empNoCompany = new Employee("Roberta Svietlova", "svietlova@outlook.ru", null, Position.MANAGER, Position.MANAGER.getSalary());
        employeeService.addEmployee(empNoCompany);
        assertTrue(employeeService.getEmployeeByCompany("TechnoCrop").isEmpty());
    }

    @Test
    void getAlphabetically() {
        employeeService.addEmployee(emp);
        employeeService.addEmployee(emp2);
        assertEquals(2, employeeService.getAlphabetically().size());
        assertEquals(emp2, employeeService.getAlphabetically().get(0));
    }

    @Test
    void groupByPosition() {
        employeeService.addEmployee(emp);
        employeeService.addEmployee(emp2);
        assertEquals(2, employeeService.groupByPosition().size());
        assertTrue(employeeService.groupByPosition().containsKey(Position.PREZES));
        assertTrue(employeeService.groupByPosition().containsKey(Position.WICEPREZES));
    }

    @Test
    void countByPosition() {
        employeeService.addEmployee(emp);
        employeeService.addEmployee(emp2);
        assertEquals(2, employeeService.countByPosition().size());
        assertEquals(1, employeeService.countByPosition().get(Position.PREZES));
        assertEquals(1, employeeService.countByPosition().get(Position.WICEPREZES));
    }

    @Test
    void getAverageSalary_WhenNoEmployees() {
        assertEquals(0.0, employeeService.getAverageSalary());
    }

    @Test
    void getAverageSalary_WhenAddedEmployees() {
        employeeService.addEmployee(emp);
        assertEquals(Position.PREZES.getSalary(), employeeService.getAverageSalary());
    }

    @Test
    void getHighestSalary_WhenNoEmployee() {
        assertTrue(employeeService.getHighestSalary().isEmpty());
    }

    @Test
    void getHighestSalary_WhenAddedEmployee() {
        employeeService.addEmployee(emp);
        employeeService.getHighestSalary();
        assertFalse(employeeService.getHighestSalary().isEmpty());
    }

    @Test
    void testValidateSalaryConsistency() {
        Employee empNullPosition = new Employee("Svietlana Gerasimova", "svietla@outlook.com", "TechnoCorp", null, 3600);
        Employee empInvalidSalary = new Employee("Igor Ivanov", "igorek@outlook.ru", "TechnoCorp", Position.STAZYSTA, 29);
        employeeService.addEmployee(emp);
        employeeService.addEmployee(empNullPosition);
        employeeService.addEmployee(empInvalidSalary);
        assertEquals(1, employeeService.validateSalaryConsistency().size());
    }

    @Test
    void testGetCompanyStatisticsWhenEmployeesHasNoCompany(){
        Employee empNoCompany = new Employee("Roberta Svietlova", "robertica@pl.ru", "", Position.MANAGER, Position.MANAGER.getSalary());
        employeeService.addEmployee(empNoCompany);
        assertTrue(employeeService.getCompanyStatistics().isEmpty());
    }

    @Test
    void testGetCompanyStatisticsWhenEmployeesHasNullCompany(){
        Employee empNullCompany = new Employee("Olga Petrova", "petrovia@outlook.ru", null, Position.PROGRAMISTA, Position.PROGRAMISTA.getSalary());
        employeeService.addEmployee(empNullCompany);
        assertTrue(employeeService.getCompanyStatistics().isEmpty());
    }

    @Test
    void testGetCompanyStatisticsWhenNoEmployees() {
        assertTrue(employeeService.getCompanyStatistics().isEmpty());
    }

    @Test
    void testGetCompanyStatisticsWhenAddedEmployees() {
        employeeService.addEmployee(emp);
        employeeService.addEmployee(emp2);
        assertEquals(1, employeeService.getCompanyStatistics().size());
        assertTrue(employeeService.getCompanyStatistics().containsKey("TechCorp"));
    }
}
