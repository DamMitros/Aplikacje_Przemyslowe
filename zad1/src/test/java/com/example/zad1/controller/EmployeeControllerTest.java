package com.example.zad1.controller;

import com.example.zad1.exception.GlobalExceptionHandler;
import com.example.zad1.model.Employee;
import com.example.zad1.model.EmploymentStatus;
import com.example.zad1.model.Position;
import com.example.zad1.service.EmployeeService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EmployeeController.class)
@ContextConfiguration(classes = {EmployeeController.class, GlobalExceptionHandler.class, EmployeeControllerTest.TestConfig.class})
public class EmployeeControllerTest {

    private Employee emp1;
    private Employee emp2;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeService employeeService;

    private Employee makeEmployee(String fullName, String email, String company, Position pos, int salary, EmploymentStatus status) {
        Employee e = new Employee(fullName, email, company, pos, salary);
        e.setStatus(status);
        return e;
    }

    @BeforeEach
    void setup() {
        emp1 = makeEmployee("Justyna Steczkowska", "steczkowska1764@gmail.com", "TechCorp", Position.PREZES, Position.PREZES.getSalary(), EmploymentStatus.ACTIVE);
        emp2 = makeEmployee("Edyta Gorniak", "edyth@gmail.com", "TechCorp", Position.WICEPREZES, Position.WICEPREZES.getSalary(), EmploymentStatus.ACTIVE);
    }

    @Test
    @DisplayName("GET /api/employees - 200 and return list of employees")
    void getAll_shouldReturn200AndList() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of(emp1, emp2));

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("steczkowska1764@gmail.com"))
                .andExpect(jsonPath("$[0].firstName").value("Justyna"))
                .andExpect(jsonPath("$[0].lastName").value("Steczkowska"))
                .andExpect(jsonPath("$[1].email").value("edyth@gmail.com"))
                .andExpect(jsonPath("$[1].position").value("WICEPREZES"));
    }

    @Test
    @DisplayName("GET /api/employees?company=TechCorp - 200 and return filtered list")
    void getAll_withCompanyFilter_shouldReturn200AndFilteredList() throws Exception {
        when(employeeService.getEmployeeByCompany("TechCorp")).thenReturn(List.of(emp1, emp2));

        mockMvc.perform(get("/api/employees").param("company", "TechCorp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].company").value("TechCorp"))
                .andExpect(jsonPath("$[1].company").value("TechCorp"));
    }

    @Test
    @DisplayName("GET /api/employees?company=NonExistent - 200 and return empty list")
    void getAll_withNonExistentCompanyFilter_shouldReturn200AndEmptyList() throws Exception {
        when(employeeService.getEmployeeByCompany("NonExistent")).thenReturn(List.of());

        mockMvc.perform(get("/api/employees").param("company", "NonExistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("GET /api/employees - 200 and return empty list when no employees")
    void getAll_noEmployees_shouldReturn200AndEmptyList() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of());

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("GET /api/employees/{email} - 200 and return employee by email")
    void getByEmail_existingEmail_shouldReturn200AndEmployee() throws Exception {
        when(employeeService.getEmployeeByEmail("steczkowska1764@gmail.com")).thenReturn(Optional.of(emp1));

        mockMvc.perform(get("/api/employees/{email}", "steczkowska1764@gmail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("steczkowska1764@gmail.com"))
                .andExpect(jsonPath("$.firstName").value("Justyna"))
                .andExpect(jsonPath("$.lastName").value("Steczkowska"))
                .andExpect(jsonPath("$.company").value("TechCorp"))
                .andExpect(jsonPath("$.position").value("PREZES"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("GET /api/employees/{email} - 404 when employee not found")
    void getByEmail_nonExistingEmail_shouldReturn404() throws Exception {
        when(employeeService.getEmployeeByEmail("nonExistent")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/employees/{email}", "nonExistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message", Matchers.containsString("Employee not found")));
    }

    @Test
    @DisplayName("POST /api/employees - 201 Created and return new employee")
    void create_shouldReturn201AndLocation() throws Exception {
        String email = "steczkowska1764@gmail.com";
        Map<String, Object> body = Map.of(
                "firstName", "Justyna",
                "lastName", "Steczkowska",
                "email", email,
                "company", "TechCorp",
                "position", "PREZES",
                "salary", 15000,
                "status", "ACTIVE"
        );

        when(employeeService.getEmployeeByEmail(email)).thenReturn(Optional.empty());
        when(employeeService.addEmployee(Mockito.any(Employee.class))).thenReturn(true);

        mockMvc.perform(post("/api/employees")
                        .contentType("application/json")
                        .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", Matchers.containsString("/api/employees/steczkowska1764@gmail.com")))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.position").value("PREZES"));
    }

    @Test
    @DisplayName("POST /api/employees - 409 Conflict when duplicate email")
    void create_shouldReturn409WhenDuplicate() throws Exception {
        when(employeeService.getEmployeeByEmail("edyth@gmail.com")).thenReturn(Optional.of(emp2));

        Map<String, Object> body = Map.of(
                "firstName", "Edyta",
                "lastName", "Gorniak",
                "email", "edyth@gmail.com",
                "company", "TechCorp",
                "position", "WICEPREZES",
                "salary", 12000,
                "status", "ACTIVE"
        );

        mockMvc.perform(post("/api/employees")
                        .contentType("application/json")
                        .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("PUT /api/employees/{email} - 200 OK after update")
    void update_existingEmployee_shouldReturn200() throws Exception {
        var updated = makeEmployee("Justyna Steczkowska", "steczkowska1764@gmail.com", "TechCorp", Position.WICEPREZES, 17000, EmploymentStatus.ON_LEAVE);

        when(employeeService.getEmployeeByEmail("steczkowska1764@gmail.com")).thenReturn(Optional.of(emp1));
        when(employeeService.UpdateEmployeeByEmail(Mockito.eq("steczkowska1764@gmail.com"), Mockito.any(Employee.class)))
                .thenReturn(Optional.of(updated));

        Map<String, Object> body = Map.of(
                "firstName", "Justyna",
                "lastName", "Steczkowska",
                "email", "steczkowska1764@gmail.com",
                "company", "TechCorp",
                "position", "WICEPREZES",
                "salary", 17000,
                "status", "ON_LEAVE"
        );

        mockMvc.perform(put("/api/employees/{email}", "steczkowska1764@gmail.com")
                        .contentType("application/json")
                        .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.position").value("WICEPREZES"))
                .andExpect(jsonPath("$.status").value("ON_LEAVE"));
    }

    @Test
    @DisplayName("PUT /api/employees/{email} - 400 when email mismatch")
    void update_shouldReturn400WhenEmailMismatch() throws Exception {
        Map<String, Object> body = Map.of("email", "other@example.com");

        mockMvc.perform(put("/api/employees/{email}", "jan@example.com")
                        .contentType("application/json")
                        .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("DELETE /api/employees/{email} - 204 No Content when deleted")
    void delete_existingEmployee_shouldReturn204() throws Exception {
        when(employeeService.deleteEmployeeByEmail("steczkowska1764@gmail.com")).thenReturn(true);

        mockMvc.perform(delete("/api/employees/{email}", "steczkowska1764@gmail.com"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/employees/{email} - 404 when not found")
    void delete_nonExistingEmployee_shouldReturn404() throws Exception {
        when(employeeService.deleteEmployeeByEmail("missing@example.com")).thenReturn(false);

        mockMvc.perform(delete("/api/employees/{email}", "missing@example.com"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PATCH /api/employees/{email}/status - 200 OK after status change")
    void patchStatus_existingEmployee_shouldReturn200() throws Exception {
        var updated = makeEmployee("Edyta Gorniak", "edyth@gmail.com", "TechCorp", Position.WICEPREZES, 12000, EmploymentStatus.TERMINATED);
        when(employeeService.updateStatusByEmail("edyth@gmail.com", EmploymentStatus.TERMINATED)).thenReturn(Optional.of(updated));

        String body = "{\"status\":\"TERMINATED\"}";

        mockMvc.perform(patch("/api/employees/{email}/status", "edyth@gmail.com")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("TERMINATED"));
    }

    @Test
    @DisplayName("PATCH /api/employees/{email}/status - 400 when invalid status")
    void patchStatus_invalidStatus_shouldReturn400() throws Exception {
        String body = "{\"status\":\"BAD_STATUS\"}";

        mockMvc.perform(patch("/api/employees/{email}/status", "edyth@gmail.com")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("GET /api/employees/status/{status} - 200 and list employees with given status")
    void getByStatus_shouldReturn200AndList() throws Exception {
        when(employeeService.getByStatus(EmploymentStatus.ACTIVE)).thenReturn(List.of(emp1, emp2));

        mockMvc.perform(get("/api/employees/status/{status}", "active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[1].status").value("ACTIVE"));
    }

    @Test
    @DisplayName("POST /api/employees - 400 when email missing")
    void create_shouldReturn400WhenEmailMissing() throws Exception {
        Map<String, Object> body = Map.of(
                "firstName", "Jan",
                "lastName", "Kowalski",
                "email", "",
                "company", "TechCorp",
                "position", "PREZES",
                "salary", 10000,
                "status", "ACTIVE"
        );

        mockMvc.perform(post("/api/employees")
                        .contentType("application/json")
                        .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/employees - 400 when position invalid")
    void create_shouldReturn400WhenPositionInvalid() throws Exception {
        Map<String, Object> body = Map.of(
                "firstName", "Jan",
                "lastName", "Kowalski",
                "email", "jan@example.com",
                "company", "TechCorp",
                "position", "BAD_POS",
                "salary", 10000,
                "status", "ACTIVE"
        );

        when(employeeService.getEmployeeByEmail("jan@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/employees")
                        .contentType("application/json")
                        .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("PUT /api/employees/{email} - 400 when body is null")
    void update_shouldReturn400WhenBodyNull() throws Exception {
        mockMvc.perform(put("/api/employees/{email}", "jan@example.com")
                        .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("PUT /api/employees/{email} - 404 when employee not found")
    void update_nonExistingEmployee_shouldReturn404() throws Exception {
        when(employeeService.getEmployeeByEmail("missing@example.com")).thenReturn(Optional.empty());

        Map<String, Object> body = Map.of(
                "email", "missing@example.com",
                "firstName", "X",
                "lastName", "Y"
        );

        mockMvc.perform(put("/api/employees/{email}", "missing@example.com")
                        .contentType("application/json")
                        .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(body)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PUT /api/employees/{email} - 400 when position invalid")
    void update_shouldReturn400WhenPositionInvalid() throws Exception {
        when(employeeService.getEmployeeByEmail("steczkowska1764@gmail.com")).thenReturn(Optional.of(emp1));

        Map<String, Object> body = Map.of(
                "email", "steczkowska1764@gmail.com",
                "firstName", "Justyna",
                "lastName", "Steczkowska",
                "company", "TechCorp",
                "position", "BAD_POS",
                "salary", 15000,
                "status", "ACTIVE"
        );

        mockMvc.perform(put("/api/employees/{email}", "steczkowska1764@gmail.com")
                        .contentType("application/json")
                        .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("PATCH /api/employees/{email}/status - 404 when employee not found")
    void patchStatus_nonExistingEmployee_shouldReturn404() throws Exception {
        when(employeeService.updateStatusByEmail("missing@example.com", EmploymentStatus.TERMINATED)).thenReturn(Optional.empty());

        String body = "{\"status\":\"TERMINATED\"}";

        mockMvc.perform(patch("/api/employees/{email}/status", "missing@example.com")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/employees/status/{status} - 400 when invalid status")
    void getByStatus_invalidStatus_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/employees/status/{status}", "WRONG"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("GET /api/employees?company= (blank) - 200 and returns all employees")
    void getAll_blankCompany_shouldReturnAll() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of(emp1, emp2));

        mockMvc.perform(get("/api/employees").param("company", "  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("steczkowska1764@gmail.com"))
                .andExpect(jsonPath("$[1].email").value("edyth@gmail.com"));
    }

    @Test
    @DisplayName("POST /api/employees - 400 when body missing")
    void create_missingBody_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/employees").contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/employees - 400 when addEmployee returns false (invalid data)")
    void create_shouldReturn400WhenAddReturnsFalse() throws Exception {
        String email = "new@example.com";
        Map<String, Object> body = Map.of(
                "firstName", "New",
                "lastName", "User",
                "email", email,
                "company", "TechCorp",
                "position", "PREZES",
                "salary", 0,
                "status", "ACTIVE"
        );
        when(employeeService.getEmployeeByEmail(email)).thenReturn(Optional.empty());
        when(employeeService.addEmployee(Mockito.any(Employee.class))).thenReturn(false);

        mockMvc.perform(post("/api/employees")
                        .contentType("application/json")
                        .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public EmployeeService employeeService() {
            return Mockito.mock(EmployeeService.class);
        }
    }
}
