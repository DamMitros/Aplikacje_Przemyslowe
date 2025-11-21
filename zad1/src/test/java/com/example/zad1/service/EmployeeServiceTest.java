package com.example.zad1.service;

import com.example.zad1.dao.EmployeeDAO;
import com.example.zad1.exception.EmployeeNotFoundException;
import com.example.zad1.model.Employee;
import com.example.zad1.model.EmploymentStatus;
import com.example.zad1.model.Position;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {
    private Employee emp1;
    private Employee emp2;

    @Mock
    private EmployeeDAO employeeDAO;

    @InjectMocks
    private EmployeeService employeeService;

     @BeforeEach
    void setUp() {
         emp1 = new Employee("Justyna Steczkowska", "steczkowska1764@gmail.com", "TechCorp", Position.PREZES, Position.PREZES.getSalary());
         emp2 = new Employee("Edyta Gorniak", "edyth@gmail.com", "TechCorp", Position.WICEPREZES, Position.WICEPREZES.getSalary());
     }

    @Test
    void shouldAddEmployeeWhenEmployeeValid() {
        when(employeeDAO.findByEmail(emp1.getEmail())).thenReturn(Optional.empty());
        assertTrue(employeeService.addEmployee(emp1));
    }

    @Test
    void shouldNotAddEmployeeWhenEmployeeDuplicate() {
        when(employeeDAO.findByEmail(emp1.getEmail())).thenReturn(Optional.of(emp1));
        assertFalse(employeeService.addEmployee(emp1));
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
        employeeService.addEmployee(emp1);
        employeeService.addEmployee(emp2);
        employeeService.displayAll();
    }

    @Test
    void getEmployeeByCompany_WhenNonExistingCompany() {
        assertTrue(employeeService.getEmployeeByCompany("NonExistent").isEmpty());
    }

    @Test
    void getEmployeeByCompany_WhenExistingCompany() {
         when(employeeDAO.findAll()).thenReturn(List.of(emp1));
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
         when(employeeDAO.findAll()).thenReturn(List.of(emp1, emp2));
         List<Employee> sorted = employeeService.getAllAlphabetically();

        assertAll(
                ()->assertEquals(2, sorted.size()),
                ()->assertEquals(emp2, sorted.get(0))
        );
    }

    @Test
    void groupByPosition() {
         when(employeeDAO.findAll()).thenReturn(List.of(emp1, emp2));

        assertAll(
                ()->assertEquals(2, employeeService.groupByPosition().size()),
                ()->assertTrue(employeeService.groupByPosition().containsKey(Position.PREZES)),
                ()->assertTrue(employeeService.groupByPosition().containsKey(Position.WICEPREZES))
        );
    }

    @Test
    void countByPosition() {
        when(employeeDAO.findAll()).thenReturn(List.of(emp1, emp2));
        Map<Position, Integer> counts = employeeService.countByPosition();

        assertAll(
                ()->assertEquals(2, counts.size()),
                ()->assertTrue(counts.containsKey(Position.PREZES)),
                ()->assertTrue(counts.containsKey(Position.WICEPREZES))
        );
    }

    @Test
    void getAverageSalary_WhenNoEmployees() {
        assertEquals(0.0, employeeService.getAverageSalary());
    }

    @Test
    void getAverageSalary_WhenAddedEmployees() {
        when(employeeDAO.findAll()).thenReturn(List.of(emp1));
        assertEquals(Position.PREZES.getSalary(), employeeService.getAverageSalary());
    }

    @Test
    void getHighestSalary_WhenNoEmployee() {
        assertTrue(employeeService.getHighestSalary().isEmpty());
    }

    @Test
    void getHighestSalary_WhenAddedEmployee() {
         when(employeeDAO.findAll()).thenReturn(List.of(emp1));
        assertFalse(employeeService.getHighestSalary().isEmpty());
    }

    @Test
    void testValidateSalaryConsistency() {
        Employee empNullPosition = new Employee("Svietlana Gerasimova", "svietla@outlook.com", "TechnoCorp", null, 3600);
        Employee empInvalidSalary = new Employee("Igor Ivanov", "igorek@outlook.ru", "TechnoCorp", Position.STAZYSTA, 29);
        when(employeeDAO.findAll()).thenReturn(List.of(emp1, empNullPosition, empInvalidSalary));
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
        when(employeeDAO.findAll()).thenReturn(List.of(emp1,emp2));
        assertAll(
                ()-> assertEquals(1, employeeService.getCompanyStatistics().size()),
                ()-> assertTrue(employeeService.getCompanyStatistics().containsKey("TechCorp"))
        );
    }

    @Test
    void testGetAllEmployees() {
        when(employeeDAO.findAll()).thenReturn(List.of(emp1,emp2));
        List<Employee> all = employeeService.getAllEmployees();

        assertAll(
                ()->assertEquals(2, all.size()),
                ()-> assertTrue(all.contains(emp1)),
                ()-> assertTrue(all.contains(emp2))
        );
    }

    @Test
    void testGetByStatus() {
        emp1.setStatus(EmploymentStatus.ACTIVE);
        emp2.setStatus(EmploymentStatus.TERMINATED);

        when(employeeDAO.findAll()).thenReturn(List.of(emp1,emp2));
        List<Employee> active = employeeService.getByStatus(EmploymentStatus.ACTIVE);
        assertAll(
                ()-> assertEquals(1, active.size()),
                ()-> assertEquals(emp1, active.get(0))
        );
    }

    @Test
    void testCountByStatus() {
        emp1.setStatus(EmploymentStatus.ACTIVE);
        emp2.setStatus(EmploymentStatus.ACTIVE);

        when(employeeDAO.findAll()).thenReturn(List.of(emp1,emp2));
        Map<EmploymentStatus, Integer> counts = employeeService.countByStatus();

        assertAll(
                ()->assertEquals(1, counts.size()),
                ()-> assertEquals(2, counts.get(EmploymentStatus.ACTIVE))
        );
    }

    @Test
    void testGetEmployeeByEmail() {
        when(employeeDAO.findByEmail(emp1.getEmail())).thenReturn(Optional.of(emp1));
        Optional<Employee> found = employeeService.getEmployeeByEmail(emp1.getEmail());
        assertAll(
                ()-> assertTrue(found.isPresent()),
                ()-> assertEquals(emp1, found.get())
        );
    }

    @Test
    void testGetAverageSalaryByCompany() {
         when(employeeDAO.findAll()).thenReturn(List.of(emp1,emp2));
        double avg = employeeService.getAverageSalaryByCompany("TechCorp");
        assertEquals((emp1.getSalary() + emp2.getSalary()) / 2.0, avg);
    }

    @Test
    void testUpdateStatusByEmail() {
        when(employeeDAO.findByEmail(emp1.getEmail())).thenReturn(Optional.of(emp1));

        Optional<Employee> updated = employeeService.updateStatusByEmail(emp1.getEmail(), EmploymentStatus.TERMINATED);
        assertAll(
                ()-> assertTrue(updated.isPresent()),
                ()-> assertEquals(EmploymentStatus.TERMINATED, updated.get().getStatus())
        );
    }

    @Test
    void testDeleteEmployeeByEmail() {
        when(employeeDAO.delete(emp1.getEmail())).thenReturn(true);
        when(employeeDAO.delete("nonexistent@email.com")).thenReturn(false);

        assertAll(
                ()-> assertTrue(employeeService.deleteEmployeeByEmail(emp1.getEmail())),
                ()-> assertFalse(employeeService.deleteEmployeeByEmail("nonexistent@email.com"))
        );
     }

    @Test
    void testUpdateEmployeeByEmailSuccess() {
        when(employeeDAO.findByEmail(emp1.getEmail())).thenReturn(Optional.of(emp1));

        Employee changes = new Employee("Justyna Nowa", "steczkowska1764@gmail.com", "NewCorp", Position.MANAGER, 5000);
        changes.setStatus(EmploymentStatus.TERMINATED);

        Optional<Employee> updated = employeeService.UpdateEmployeeByEmail(emp1.getEmail(), changes);

        assertTrue(updated.isPresent());
        Employee result = updated.get();

        assertAll(
                ()-> assertEquals("Justyna Nowa", result.getFullName()),
                ()-> assertEquals("NewCorp", result.getCompanyName()),
                ()-> assertEquals(Position.MANAGER, result.getPosition()),
                ()-> assertEquals(5000, result.getSalary()),
                ()-> assertEquals(EmploymentStatus.TERMINATED, result.getStatus())
        );
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
        employeeService.addEmployee(emp1);
        Optional<Employee> updated = employeeService.UpdateEmployeeByEmail(emp1.getEmail(), null);
        assertTrue(updated.isEmpty());
    }
}
