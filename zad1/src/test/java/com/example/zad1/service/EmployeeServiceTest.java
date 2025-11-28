package com.example.zad1.service;

import com.example.zad1.repository.EmployeeRepository;
import com.example.zad1.repository.DepartmentRepository;
import com.example.zad1.model.CompanyStatistics;
import com.example.zad1.model.Employee;
import com.example.zad1.model.EmploymentStatus;
import com.example.zad1.model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.Optional;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {
    private Employee emp1;
    private Employee emp2;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        emp1 = new Employee("Justyna Steczkowska", "steczkowska1764@gmail.com", "TechCorp", Position.PREZES, Position.PREZES.getSalary());
        emp2 = new Employee("Edyta Gorniak", "edyth@gmail.com", "TechCorp", Position.WICEPREZES, Position.WICEPREZES.getSalary());
    }

    @Test
    void shouldAddEmployeeWhenEmployeeValid() {
        when(employeeRepository.existsByEmail(emp1.getEmail())).thenReturn(false);
        when(employeeRepository.save(emp1)).thenReturn(emp1);
        assertTrue(employeeService.addEmployee(emp1));
        verify(employeeRepository).save(emp1);
    }

    @Test
    void shouldNotAddEmployeeWhenEmployeeDuplicate() {
        when(employeeRepository.existsByEmail(emp1.getEmail())).thenReturn(true);
        assertFalse(employeeService.addEmployee(emp1));
        verify(employeeRepository, never()).save(any());
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
        Employee empBlankEmail = new Employee("Anna Nowak", "", "TechCorp", Position.WICEPREZES, Position.WICEPREZES.getSalary());
        assertFalse(employeeService.addEmployee(empBlankEmail));
    }

    @Test
    void getEmployeeByCompany_WhenNonExistingCompany() {
        when(employeeRepository.findAll(any(Specification.class))).thenReturn(List.of());
        assertTrue(employeeService.getEmployeeByCompany("NonExistent").isEmpty());
    }

    @Test
    void getEmployeeByCompany_WhenExistingCompany() {
        when(employeeRepository.findAll(any(Specification.class))).thenReturn(List.of(emp1, emp2));
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
        when(employeeRepository.findAll(any(Specification.class))).thenReturn(List.of());
        assertTrue(employeeService.getEmployeeByCompany("TechnoCrop").isEmpty());
    }

    @Test
    void getAlphabetically() {
        when(employeeRepository.findAll()).thenReturn(List.of(emp1, emp2));
        List<Employee> sorted = employeeService.getAllAlphabetically();
        assertNotNull(sorted);
    }

    @Test
    void groupByPosition() {
        when(employeeRepository.findAll()).thenReturn(List.of(emp1, emp2));
        assertAll(
                () -> assertEquals(2, employeeService.groupByPosition().size()),
                () -> assertTrue(employeeService.groupByPosition().containsKey(Position.PREZES)),
                () -> assertTrue(employeeService.groupByPosition().containsKey(Position.WICEPREZES))
        );
    }

    @Test
    void countByPosition() {
        when(employeeRepository.countByPositionJPQL()).thenReturn(List.of(new Object[]{Position.PREZES, 1L}, new Object[]{Position.WICEPREZES, 1L}));
        Map<Position, Long> counts = employeeService.countByPosition();
        assertAll(
                () -> assertEquals(2, counts.size()),
                () -> assertEquals(1L, counts.get(Position.PREZES)),
                () -> assertEquals(1L, counts.get(Position.WICEPREZES))
        );
    }

    @Test
    void getAverageSalary_WhenNoEmployees() {
        when(employeeRepository.getAverageSalaryJPQL()).thenReturn(0.0);
        assertEquals(0.0, employeeService.getAverageSalary());
    }

    @Test
    void getAverageSalary_WhenAddedEmployees() {
        when(employeeRepository.getAverageSalaryJPQL()).thenReturn((double) Position.PREZES.getSalary());
        assertEquals(Position.PREZES.getSalary(), employeeService.getAverageSalary());
    }

    @Test
    void getHighestSalary_WhenNoEmployee() {
        when(employeeRepository.findAll()).thenReturn(List.of());
        assertTrue(employeeService.getHighestSalary().isEmpty());
    }

    @Test
    void getHighestSalary_WhenAddedEmployee() {
        when(employeeRepository.findAll()).thenReturn(List.of(emp1));
        assertFalse(employeeService.getHighestSalary().isEmpty());
    }

    @Test
    void testValidateSalaryConsistency() {
        Employee empNullPosition = new Employee("Svietlana Gerasimova", "svietla@outlook.com", "TechnoCorp", null, 3600);
        Employee empInvalidSalary = new Employee("Igor Ivanov", "igorek@outlook.ru", "TechnoCorp", Position.STAZYSTA, 29);
        when(employeeRepository.findAll()).thenReturn(List.of(emp1, empNullPosition, empInvalidSalary));
        assertEquals(1, employeeService.validateSalaryConsistency().size());
    }

    @Test
    void testGetCompanyStatisticsWhenEmployeesHasNoCompany() {
        when(employeeRepository.getCompanyStatisticsJPQL()).thenReturn(List.of());
        assertTrue(employeeService.getCompanyStatistics().isEmpty());
    }

    @Test
    void testGetCompanyStatisticsWhenEmployeesHasNullCompany() {
        when(employeeRepository.getCompanyStatisticsJPQL()).thenReturn(List.of());
        assertTrue(employeeService.getCompanyStatistics().isEmpty());
    }

    @Test
    void testGetCompanyStatisticsWhenNoEmployees() {
        when(employeeRepository.getCompanyStatisticsJPQL()).thenReturn(List.of());
        assertTrue(employeeService.getCompanyStatistics().isEmpty());
    }

    @Test
    void testGetCompanyStatisticsWhenAddedEmployees() {
        CompanyStatistics stats = new CompanyStatistics("TechCorp", 2L, 10500.0, 12500);
        when(employeeRepository.getCompanyStatisticsJPQL()).thenReturn(List.of(stats));
        Map<String, CompanyStatistics> result = employeeService.getCompanyStatistics();
        assertAll(
                () -> assertEquals(1, result.size()),
                () -> assertTrue(result.containsKey("TechCorp"))
        );
    }

    @Test
    void testGetAllEmployees() {
        when(employeeRepository.findAll()).thenReturn(List.of(emp1, emp2));
        List<Employee> all = employeeService.getAllEmployees();
        assertAll(
                () -> assertEquals(2, all.size()),
                () -> assertTrue(all.contains(emp1)),
                () -> assertTrue(all.contains(emp2))
        );
    }

    @Test
    void testGetByStatus() {
        emp1.setStatus(EmploymentStatus.ACTIVE);
        when(employeeRepository.findByStatus(EmploymentStatus.ACTIVE)).thenReturn(List.of(emp1));
        List<Employee> active = employeeService.getByStatus(EmploymentStatus.ACTIVE);
        assertAll(
                () -> assertEquals(1, active.size()),
                () -> assertEquals(emp1, active.get(0))
        );
    }

    @Test
    void testCountByStatus() {
        List<Object[]> mockResult = Collections.singletonList(new Object[]{EmploymentStatus.ACTIVE, 2L});
        when(employeeRepository.countByStatusJPQL()).thenReturn(mockResult);

        Map<EmploymentStatus, Long> counts = employeeService.countByStatus();
        assertAll(
                () -> assertEquals(1, counts.size()),
                () -> assertEquals(2L, counts.get(EmploymentStatus.ACTIVE))
        );
    }

    @Test
    void testGetEmployeeByEmail() {
        when(employeeRepository.findByEmail(emp1.getEmail())).thenReturn(Optional.of(emp1));
        Optional<Employee> found = employeeService.getEmployeeByEmail(emp1.getEmail());
        assertAll(
                () -> assertTrue(found.isPresent()),
                () -> assertEquals(emp1, found.get())
        );
    }

    @Test
    void testGetAverageSalaryByCompany() {
        double expectedAvg = (emp1.getSalary() + emp2.getSalary()) / 2.0;
        when(employeeRepository.getAverageSalaryByCompanyJPQL("TechCorp")).thenReturn(expectedAvg);

        double avg = employeeService.getAverageSalaryByCompany("TechCorp");
        assertEquals(expectedAvg, avg);
    }

    @Test
    void testUpdateStatusByEmail() {
        when(employeeRepository.findByEmail(emp1.getEmail())).thenReturn(Optional.of(emp1));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));
        Optional<Employee> updated = employeeService.updateStatusByEmail(emp1.getEmail(), EmploymentStatus.TERMINATED);
        assertAll(
                () -> assertTrue(updated.isPresent()),
                () -> assertEquals(EmploymentStatus.TERMINATED, updated.get().getStatus())
        );
    }

    @Test
    void testDeleteEmployeeByEmail() {
        when(employeeRepository.existsByEmail(emp1.getEmail())).thenReturn(true);
        when(employeeRepository.existsByEmail("nonexistent@email.com")).thenReturn(false);
        doNothing().when(employeeRepository).deleteByEmail(emp1.getEmail());
        assertAll(
                () -> assertTrue(employeeService.deleteEmployeeByEmail(emp1.getEmail())),
                () -> assertFalse(employeeService.deleteEmployeeByEmail("nonexistent@email.com"))
        );
    }

    @Test
    void testUpdateEmployeeByEmailSuccess() {
        when(employeeRepository.findByEmail(emp1.getEmail())).thenReturn(Optional.of(emp1));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));
        Employee changes = new Employee("Justyna Nowa", "steczkowska1764@gmail.com", "NewCorp", Position.MANAGER, 5000);
        changes.setStatus(EmploymentStatus.TERMINATED);
        Optional<Employee> updated = employeeService.updateEmployeeByEmail(emp1.getEmail(), changes);
        assertTrue(updated.isPresent());
        Employee result = updated.get();
        assertAll(
                () -> assertEquals("Justyna Nowa", result.getFullName()),
                () -> assertEquals("NewCorp", result.getCompanyName()),
                () -> assertEquals(Position.MANAGER, result.getPosition()),
                () -> assertEquals(5000, result.getSalary()),
                () -> assertEquals(EmploymentStatus.TERMINATED, result.getStatus())
        );
    }

    @Test
    void testUpdateEmployeeByEmailNonExistingEmail() {
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        Employee changes = new Employee("Anna Nowak", "anna@nowak.com", "CorpX", Position.MANAGER, 4000);
        Optional<Employee> updated = employeeService.updateEmployeeByEmail("nonexistent@email.com", changes);
        assertTrue(updated.isEmpty());
    }

    @Test
    void testUpdateEmployeeByEmailNullEmail() {
        Optional<Employee> updated = employeeService.updateEmployeeByEmail(null, emp1);
        assertTrue(updated.isEmpty());
    }
}