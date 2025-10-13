package com.example.zad1;

import com.example.zad1.exception.ApiException;
import com.example.zad1.model.Employee;
import com.example.zad1.model.Position;
import com.example.zad1.service.ApiService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ApiServiceTest {

    @Test
    void testFetchEmployeesFromApi() throws ApiException {
        ApiService apiService = new ApiService();
        List<Employee> employees = apiService.fetchEmployeesFromApi();

        assertEquals(10, employees.size());
        for (Employee e : employees) {
            assertEquals(Position.PROGRAMISTA, e.getPosition());
            assertEquals(Position.PROGRAMISTA.getSalary(), e.getSalary());
            assertNotNull(e.getEmail());
            assertNotNull(e.getCompanyName());
            assertNotNull(e.getFullName());
        }
    }
}
