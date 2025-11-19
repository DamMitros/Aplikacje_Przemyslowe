package com.example.zad1.controller;

import com.example.zad1.model.Employee;
import com.example.zad1.model.ImportSummary;
import com.example.zad1.model.Position;
import com.example.zad1.service.EmployeeService;
import com.example.zad1.service.FileStorageService;
import com.example.zad1.service.ImportService;
import com.example.zad1.service.DepartmentService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeViewController.class)
@ContextConfiguration(classes = {EmployeeViewController.class, com.example.zad1.exception.GlobalExceptionHandler.class, EmployeeViewControllerTest.TestConfig.class})
class EmployeeViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private ImportService importService;
    @Autowired
    private FileStorageService storageService;
    @Autowired
    private DepartmentService departmentService;

    @Test
    void listEmployees_shouldReturnListViewAndModel() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of(new Employee("Test", "test@test.com", "Firma", Position.PROGRAMISTA, 8000)));

        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/list"))
                .andExpect(model().attributeExists("employees"));
    }

    @Test
    void showAddForm_shouldReturnAddForm() throws Exception {
        when(departmentService.getAllDepartments()).thenReturn(List.of());
        mockMvc.perform(get("/employees/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/add-form"))
                .andExpect(model().attributeExists("employee", "positions", "statuses", "departments"));
    }

    @Test
    void addEmployee_success_shouldRedirect() throws Exception {
        when(employeeService.GetEmployeeByEmail(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(post("/employees/add")
                        .param("fullName", "Jan Kowalski")
                        .param("email", "jan@kowalski.com")
                        .param("salary", "5000")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    void addEmployee_validationError_shouldReturnForm() throws Exception {
        mockMvc.perform(post("/employees/add")
                        .param("fullName", "Jan Kowalski")
                        .param("email", "")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("employees/add-form"))
                .andExpect(model().hasErrors());
    }

    @Test
    void addEmployee_duplicateEmail_shouldReturnForm() throws Exception {
        when(employeeService.GetEmployeeByEmail("duplicate@test.com")).thenReturn(Optional.of(new Employee()));

        mockMvc.perform(post("/employees/add")
                        .param("fullName", "Jan Kowalski")
                        .param("email", "duplicate@test.com")
                        .param("salary", "5000")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("employees/add-form"))
                .andExpect(model().attributeHasFieldErrorCode("employee", "email", "error.employee"));
    }

    @Test
    void showEditForm_shouldReturnEditForm() throws Exception {
        Employee testEmp = new Employee("Test", "test@test.com", "Firma", Position.PROGRAMISTA, 8000);
        when(employeeService.GetEmployeeByEmail("test@test.com")).thenReturn(Optional.of(testEmp));
        when(departmentService.getAllDepartments()).thenReturn(List.of());
        mockMvc.perform(get("/employees/edit/test@test.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/edit-form"))
                .andExpect(model().attributeExists("employee", "positions", "statuses", "departments"));
    }

    @Test
    void showEditForm_employeeNotFound_shouldReturnError() throws Exception {
        when(employeeService.GetEmployeeByEmail("test@test.com")).thenReturn(Optional.empty());
        mockMvc.perform(get("/employees/edit/test@test.com"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void updateEmployee_success_shouldRedirect() throws Exception {
        Employee testEmp = new Employee("Test", "test@test.com", "Firma", Position.PROGRAMISTA, 8000);

        mockMvc.perform(post("/employees/edit")
                        .param("fullName", testEmp.getFullName())
                        .param("email", testEmp.getEmail())
                        .param("companyName", testEmp.getCompanyName())
                        .param("position", testEmp.getPosition().name())
                        .param("salary", String.valueOf(testEmp.getSalary()))
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    void updateEmployee_validationError_shouldReturnForm() throws Exception {
        mockMvc.perform(post("/employees/edit")
                        .param("fullName", "Test User")
                        .param("email", "")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("employees/edit-form"))
                .andExpect(model().hasErrors());
    }

    @Test
    void deleteEmployee_shouldRedirect() throws Exception {
        mockMvc.perform(get("/employees/delete/test@test.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    void showSearchForm_shouldReturnSearchForm() throws Exception{
        mockMvc.perform(get("/employees/search"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/search-form"));
    }

    @Test
    void searchEmployees_shouldReturnResultsView() throws Exception {
        List<Employee> results = List.of(new Employee("Test", "test@test.com", "Firma", Position.PROGRAMISTA, 8000));
        when(employeeService.getEmployeeByCompany("Firma")).thenReturn(results);
        mockMvc.perform(post("/employees/search")
                        .param("company", "Firma"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/search-results"))
                .andExpect(model().attribute("results", results))
                .andExpect(model().attribute("companyQuery", "Firma"));
    }

    @Test
    void showImportForm_shouldReturnImportForm() throws Exception {
        mockMvc.perform(get("/employees/import"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/import-form"));
    }

    @Test
    void handleImport_csv_success_shouldRedirect() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", "content".getBytes());
        ImportSummary summary = new ImportSummary(1, List.of());

        doNothing().when(storageService).validateFile(any(), any(Set.class), anyLong(), any(Set.class));
        when(storageService.storeInUploads(any(), isNull())).thenReturn("saved.csv");
        when(importService.importFromCsv("saved.csv")).thenReturn(summary);

        mockMvc.perform(multipart("/employees/import")
                        .file(file)
                        .param("fileType", "csv"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attribute("message", "Import zakończony. Zaimportowano: 1. Błędów: 0"));
    }

    @Test
    void handleImport_emptyFile_shouldRedirectWithError() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "empty.csv", "text/csv", new byte[0]);

        mockMvc.perform(multipart("/employees/import")
                        .file(file)
                        .param("fileType", "csv"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees/import"))
                .andExpect(flash().attribute("errorMessage", "Proszę wybrać plik do importu."));
    }

    @Test
    void handleImport_xml_success_shouldRedirect() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.xml", "application/xml", "content".getBytes());
        ImportSummary summary = new ImportSummary(2, List.of());

        doNothing().when(storageService).validateFile(any(), any(Set.class), anyLong(), any(Set.class));
        when(storageService.storeInUploads(any(), isNull())).thenReturn("saved.xml");
        when(importService.importFromXml("saved.xml")).thenReturn(summary);

        mockMvc.perform(multipart("/employees/import")
                        .file(file)
                        .param("fileType", "xml"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attribute("message", "Import zakończony. Zaimportowano: 2. Błędów: 0"));
    }

    @Test
    void handleImport_unknownFileType_shouldRedirectWithError() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());

        mockMvc.perform(multipart("/employees/import")
                        .file(file)
                        .param("fileType", "txt"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees/import"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public EmployeeService employeeService() { return Mockito.mock(EmployeeService.class); }
        @Bean
        public ImportService importService() { return Mockito.mock(ImportService.class); }
        @Bean
        public FileStorageService fileStorageService() { return Mockito.mock(FileStorageService.class); }
        @Bean
        public DepartmentService departmentService() { return Mockito.mock(DepartmentService.class); }
    }
}