package com.example.zad1.controller;

import com.example.zad1.model.CompanyStatistics;
import com.example.zad1.model.Employee;
import com.example.zad1.model.Position;
import com.example.zad1.service.DepartmentService;
import com.example.zad1.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StatisticsViewController.class)
@ContextConfiguration(classes = {StatisticsViewController.class, StatisticsViewControllerTest.TestConfig.class})
class StatisticsViewControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private DepartmentService departmentService;

    @TestConfiguration
    static class TestConfig {
        @Bean public EmployeeService employeeService() { return Mockito.mock(EmployeeService.class); }
        @Bean public DepartmentService departmentService() { return Mockito.mock(DepartmentService.class); }
    }

    @Test
    void statisticsDashboard_shouldReturnViewWithModel() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of(new Employee("Test","t@test.com","Firma", Position.PROGRAMISTA, 5000)));
        when(employeeService.getAverageSalary()).thenReturn(5000.0);
        when(departmentService.getAllDepartments()).thenReturn(List.of());
        when(employeeService.getCompanyStatistics()).thenReturn(Map.of("Firma", new CompanyStatistics("TechCorp", 1,5000.0,"Test",5000)));
        when(employeeService.countByPosition()).thenReturn(Map.of(Position.PROGRAMISTA,1));

        mockMvc.perform(get("/statistics"))
                .andExpect(status().isOk())
                .andExpect(view().name("statistics/index"))
                .andExpect(model().attributeExists("totalEmployees","averageSalary","totalDepartments","companyStats","positionCounts"));
    }

    @Test
    void companyStatistics_shouldReturnCompanyView() throws Exception {
        when(employeeService.getCompanyStatistics()).thenReturn(Map.of("Firma", new CompanyStatistics("StrawberryPie", 1,5000.0,"Test",5000)));
        when(employeeService.getEmployeeByCompany("Firma")).thenReturn(List.of(new Employee("Test","t@test.com","Firma", Position.PROGRAMISTA, 5000)));

        mockMvc.perform(get("/statistics/company/Firma"))
                .andExpect(status().isOk())
                .andExpect(view().name("statistics/company"))
                .andExpect(model().attributeExists("companyName","stats","employees"));
    }
}

