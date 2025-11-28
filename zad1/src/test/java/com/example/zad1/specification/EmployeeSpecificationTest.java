package com.example.zad1.specification;

import com.example.zad1.model.Employee;
import com.example.zad1.model.Position;
import com.example.zad1.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EmployeeSpecificationTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();

        employeeRepository.save(new Employee("Jan Kowalski", "jan@test.com", "TechCorp", Position.PROGRAMISTA, 10000));
        employeeRepository.save(new Employee("Anna Nowak", "anna@test.com", "SoftServe", Position.MANAGER, 15000));
        employeeRepository.save(new Employee("Piotr Zieliński", "piotr@test.com", "TechCorp", Position.STAZYSTA, 3000));
        employeeRepository.save(new Employee("Kasia Wiśniewska", "kasia@test.com", "Google", Position.PREZES, 25000));
    }

    @Test
    void filterBy_companyName_shouldFilterCorrectly() {
        Specification<Employee> spec = EmployeeSpecification.filterBy("TechCorp", null, null);
        List<Employee> results = employeeRepository.findAll(spec);

        assertThat(results).hasSize(2);
        assertThat(results).extracting(Employee::getFullName)
                .containsExactlyInAnyOrder("Jan Kowalski", "Piotr Zieliński");
    }

    @Test
    void filterBy_companyName_caseInsensitive() {
        Specification<Employee> spec = EmployeeSpecification.filterBy("techcorp", null, null);
        List<Employee> results = employeeRepository.findAll(spec);

        assertThat(results).hasSize(2);
    }

    @Test
    void filterBy_minSalary_shouldFilterCorrectly() {
        Specification<Employee> spec = EmployeeSpecification.filterBy(null, 12000, null);
        List<Employee> results = employeeRepository.findAll(spec);

        assertThat(results).hasSize(2);
    }

    @Test
    void filterBy_maxSalary_shouldFilterCorrectly() {
        Specification<Employee> spec = EmployeeSpecification.filterBy(null, null, 5000);
        List<Employee> results = employeeRepository.findAll(spec);

        assertThat(results).hasSize(1);
    }

    @Test
    void filterBy_combinedCriteria_shouldFilterCorrectly() {
        Specification<Employee> spec = EmployeeSpecification.filterBy("TechCorp", 5000, null);
        List<Employee> results = employeeRepository.findAll(spec);

        assertThat(results).hasSize(1);
    }

    @Test
    void filterBy_allNulls_shouldReturnAll() {
        Specification<Employee> spec = EmployeeSpecification.filterBy(null, null, null);
        List<Employee> results = employeeRepository.findAll(spec);

        assertThat(results).hasSize(4);
    }
}