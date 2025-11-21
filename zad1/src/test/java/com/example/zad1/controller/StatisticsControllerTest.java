package com.example.zad1.controller;

import com.example.zad1.exception.GlobalExceptionHandler;
import com.example.zad1.model.CompanyStatistics;
import com.example.zad1.model.EmploymentStatus;
import com.example.zad1.model.Position;
import com.example.zad1.service.EmployeeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = StatisticsController.class)
@ContextConfiguration(classes = {StatisticsController.class, GlobalExceptionHandler.class, StatisticsControllerTest.TestConfig.class})
class StatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeService employeeService;

    @Test
    @DisplayName("GET /api/statistics/salary/average -> 200 and global average salary")
    void averageSalary_global() throws Exception {
        Mockito.when(employeeService.getAverageSalary()).thenReturn(12345.67);

        mockMvc.perform(get("/api/statistics/salary/average"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.averageSalary", is(12345.67)));
    }

    @Test
    @DisplayName("GET /api/statistics/salary/average?company=X -> 200 and average salary for company X")
    void averageSalary_byCompany() throws Exception {
        Mockito.when(employeeService.getAverageSalaryByCompany("TechCorp")).thenReturn(19250.0);

        mockMvc.perform(get("/api/statistics/salary/average").param("company", "TechCorp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageSalary", is(19250.0)));
    }

    @Test
    @DisplayName("GET /api/statistics/salary/average?company=null -> 200 and global average salary")
    void averageSalary_companyNull() throws Exception {
        Mockito.when(employeeService.getAverageSalary()).thenReturn(12345.67);

        mockMvc.perform(get("/api/statistics/salary/average").param("company", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageSalary", is(12345.67)));
    }

    @Test
    @DisplayName("GET /api/statistics/company/{companyName} -> 200 and company statistics")
    void companyStats_success() throws Exception {
        CompanyStatistics cs = new CompanyStatistics("TechCorp", 2, 19250.0, "Jan Kowalski", 27000);
        Map<String, CompanyStatistics> map = Map.of("TechCorp", cs);
        Mockito.when(employeeService.getCompanyStatistics()).thenReturn(map);

        mockMvc.perform(get("/api/statistics/company/{company}", "TechCorp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName", is("TechCorp")))
                .andExpect(jsonPath("$.employeeCount", is(2)))
                .andExpect(jsonPath("$.averageSalary", is(19250.0)))
                .andExpect(jsonPath("$.highestSalary", is(27000)))
                .andExpect(jsonPath("$.topEarnerFullName", is("Jan Kowalski")));
    }

    @Test
    @DisplayName("GET /api/statistics/company/{companyName} -> 400 when company not found in stats")
    void companyStats_notFoundInStats() throws Exception {
        Mockito.when(employeeService.getCompanyStatistics()).thenReturn(Map.of());

        mockMvc.perform(get("/api/statistics/company/{company}", "UnknownCorp"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("No stats for company: UnknownCorp")));
    }

    @Test
    @DisplayName("GET /api/statistics/positions -> 200 and positions distribution")
    void positions_distribution() throws Exception {
        Map<Position, Integer> counts = new LinkedHashMap<>();
        counts.put(null,1);
        counts.put(Position.MANAGER, 3);
        counts.put(Position.PROGRAMISTA, 5);
        Mockito.when(employeeService.countByPosition()).thenReturn(counts);

        mockMvc.perform(get("/api/statistics/positions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.MANAGER", is(3)))
                .andExpect(jsonPath("$.PROGRAMISTA", is(5)));
    }

    @Test
    @DisplayName("GET /api/statistics/status -> 200 and employment status distribution")
    void status_distribution() throws Exception {
        Map<EmploymentStatus, Integer> counts = new LinkedHashMap<>();
        counts.put(EmploymentStatus.ACTIVE, 4);
        counts.put(EmploymentStatus.ON_LEAVE, 1);
        counts.put(EmploymentStatus.TERMINATED, 2);
        counts.put(null, 3);
        Mockito.when(employeeService.countByStatus()).thenReturn(counts);

        mockMvc.perform(get("/api/statistics/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ACTIVE", is(4)))
                .andExpect(jsonPath("$.ON_LEAVE", is(1)))
                .andExpect(jsonPath("$.TERMINATED", is(2)));
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public EmployeeService employeeService() {
            return Mockito.mock(EmployeeService.class);
        }
    }
}
