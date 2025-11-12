package com.example.zad1.service;

import com.example.zad1.model.Employee;
import com.example.zad1.model.EmploymentStatus;
import com.example.zad1.model.Position;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.List;
import java.util.Map;

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

    @Test
    void testGetAllEmployees() {
        employeeService.addEmployee(emp);
        employeeService.addEmployee(emp2);
        List<Employee> all = employeeService.getAllEmployees();
        assertEquals(2, all.size());
        assertTrue(all.contains(emp));
        assertTrue(all.contains(emp2));
    }

    @Test
    void testGetByStatus() {
        emp.setStatus(EmploymentStatus.ACTIVE);
        emp2.setStatus(EmploymentStatus.TERMINATED);
        employeeService.addEmployee(emp);
        employeeService.addEmployee(emp2);

        List<Employee> active = employeeService.getByStatus(EmploymentStatus.ACTIVE);
        assertEquals(1, active.size());
        assertEquals(emp, active.get(0));
    }

    @Test
    void testCountByStatus() {
        emp.setStatus(EmploymentStatus.ACTIVE);
        emp2.setStatus(EmploymentStatus.ACTIVE);
        employeeService.addEmployee(emp);
        employeeService.addEmployee(emp2);

        Map<EmploymentStatus, Integer> counts = employeeService.countByStatus();
        assertEquals(1, counts.size());
        assertEquals(2, counts.get(EmploymentStatus.ACTIVE));
    }

    @Test
    void testGetEmployeeByEmail() {
        employeeService.addEmployee(emp);
        Optional<Employee> found = employeeService.GetEmployeeByEmail(emp.getEmail());
        assertTrue(found.isPresent());
        assertEquals(emp, found.get());
    }

    @Test
    void testGetAverageSalaryByCompany() {
        employeeService.addEmployee(emp);
        employeeService.addEmployee(emp2);
        double avg = employeeService.getAverageSalaryByCompany("TechCorp");
        assertEquals((emp.getSalary() + emp2.getSalary()) / 2.0, avg);
    }

    @Test
    void testUpdateStatusByEmail() {
        employeeService.addEmployee(emp);
        employeeService.updateStatusByEmail(emp.getEmail(), EmploymentStatus.TERMINATED);
        Optional<Employee> updated = employeeService.GetEmployeeByEmail(emp.getEmail());
        assertTrue(updated.isPresent());
        assertEquals(EmploymentStatus.TERMINATED, updated.get().getStatus());
    }

    @Test
    void testDeleteEmployeeByEmail() {
        employeeService.addEmployee(emp);
        assertTrue(employeeService.deleteEmployeeByEmail(emp.getEmail()));
        assertFalse(employeeService.getAllEmployees().contains(emp));

        assertFalse(employeeService.deleteEmployeeByEmail("nonexistent@email.com"));
    }

    @Test
    void testUpdateEmployeeByEmailSuccess() {
        employeeService.addEmployee(emp);
        Employee changes = new Employee("Justyna Nowa", "steczkowska1764@gmail.com", "NewCorp", Position.MANAGER, 5000);
        changes.setStatus(EmploymentStatus.TERMINATED);

        Optional<Employee> updated = employeeService.UpdateEmployeeByEmail(emp.getEmail(), changes);
        assertTrue(updated.isPresent());

        Employee result = updated.get();
        assertEquals("Justyna Nowa", result.getFullName());
        assertEquals("NewCorp", result.getCompanyName());
        assertEquals(Position.MANAGER, result.getPosition());
        assertEquals(5000, result.getSalary());
        assertEquals(EmploymentStatus.TERMINATED, result.getStatus());
    }

    @Test
    void testUpdateEmployeeByEmailNonExistingEmail() {
        Employee changes = new Employee("Anna Nowak", "anna@nowak.com", "CorpX", Position.MANAGER, 4000);
        Optional<Employee> updated = employeeService.UpdateEmployeeByEmail("nonexistent@email.com", changes);
        assertTrue(updated.isEmpty());
    }

    @Test
    void testUpdateEmployeeByEmailNullEmail() {
        Employee changes = new Employee("Anna Nowak", "anna@nowak.com", "CorpX", Position.MANAGER, 4000);
        Optional<Employee> updated = employeeService.UpdateEmployeeByEmail(null, changes);
        assertTrue(updated.isEmpty());
    }

    @Test
    void testUpdateEmployeeByEmailNullChanges() {
        employeeService.addEmployee(emp);
        Optional<Employee> updated = employeeService.UpdateEmployeeByEmail(emp.getEmail(), null);
        assertTrue(updated.isEmpty());
    }
}
