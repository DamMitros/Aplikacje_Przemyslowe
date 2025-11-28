package com.example.zad1.controller;

import com.example.zad1.model.Department;
import com.example.zad1.model.Employee;
import com.example.zad1.model.Position;
import com.example.zad1.service.DepartmentDocumentService;
import com.example.zad1.service.DepartmentService;
import com.example.zad1.service.EmployeeService;
import com.example.zad1.service.FileStorageService;
import com.example.zad1.model.DepartmentDocument;
import com.example.zad1.model.DocumentType;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DepartmentViewController.class)
@ContextConfiguration(classes = {DepartmentViewController.class, com.example.zad1.exception.GlobalExceptionHandler.class, DepartmentViewControllerTest.TestConfig.class})
class DepartmentViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private DepartmentDocumentService departmentDocumentService;
    @Autowired
    private FileStorageService fileStorageService;

    @Test
    void listDepartments_shouldReturnListView() throws Exception {
        when(departmentService.getAllDepartments()).thenReturn(List.of(new Department("IT","GradientCorp", "Test", 100, null)));
        when(employeeService.getAllEmployees()).thenReturn(List.of());

        mockMvc.perform(get("/departments"))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/list"))
                .andExpect(model().attributeExists("departments", "employeeNames"));
    }

    @Test
    void showAddForm_shouldReturnForm() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of(
                new Employee("Test Manager", "manager@test.com", "Firma", Position.MANAGER, 10000)
        ));

        mockMvc.perform(get("/departments/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/form"))
                .andExpect(model().attributeExists("department", "managers"));
    }

    @Test
    void addDepartment_success_shouldRedirect() throws Exception {
        mockMvc.perform(post("/departments/add")
                        .param("name", "HR")
                        .param("companyName", "TechCorp")
                        .param("location", "Kraków")
                        .param("budget", "100000")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/departments"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    void addDepartment_validationError_shouldReturnForm() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of());

        mockMvc.perform(post("/departments/add")
                        .param("name", "")
                        .param("location", "Kraków")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("departments/form"))
                .andExpect(model().hasErrors());
    }

    @Test
    void showEditForm_shouldReturnForm() throws Exception {
        Department testDept = new Department("IT","AiCorporation","Warszawa", 500000, "email@email.com");
        when(departmentService.getDepartmentById(1L)).thenReturn(Optional.of(testDept));
        when(employeeService.getAllEmployees()).thenReturn(List.of());
        mockMvc.perform(get("/departments/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/form"))
                .andExpect(model().attributeExists("department", "managers"));
    }

    @Test
    void showEditForm_nonExistingDepartment_shouldReturnErrorView() throws Exception {
        when(departmentService.getDepartmentById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/departments/edit/999"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attributeExists("message"));
    }

    @Test
    void updateDepartment_success_shouldRedirect() throws Exception {
        mockMvc.perform(post("/departments/edit/1")
                        .param("name", "HR")
                        .param("companyName", "TechCorp")
                        .param("location", "Kraków")
                        .param("budget", "100000")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/departments"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    void updateDepartment_validationError_shouldReturnForm() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of());

        mockMvc.perform(post("/departments/edit/1")
                        .param("name", "")
                        .param("location", "Kraków")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("departments/form"))
                .andExpect(model().hasErrors());
    }

    @Test
    void deleteDepartment_shouldRedirect() throws Exception {
        mockMvc.perform(get("/departments/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/departments"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    void departmentDetails_shouldReturnDetailsView() throws Exception {
        Department testDept = new Department("IT","AiCorporation","Warszawa", 500000, "manager@test.com");
        Employee manager = new Employee("Manager", "manager@test.com", "Firma", Position.MANAGER, 10000);
        Employee worker = new Employee("Worker", "worker@test.com", "Firma", Position.PROGRAMISTA, 8000);
        worker.setDepartmentId(1L);

        when(departmentService.getDepartmentById(1L)).thenReturn(Optional.of(testDept));
        when(employeeService.getAllEmployees()).thenReturn(List.of(manager, worker));
        when(employeeService.getEmployeeByEmail("manager@test.com")).thenReturn(Optional.of(manager));

        mockMvc.perform(get("/departments/details/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/details"))
                .andExpect(model().attributeExists("department", "employeesInDept", "managerName"));
    }

    @Test
    void departmentDetails_noManagerEmail_shouldReturnDetailsView() throws Exception {
        Department testDept = new Department("IT","AiCorporation","Warszawa", 500000, null);
        Employee worker = new Employee("Worker", "worker@test.com", "Firma", Position.PROGRAMISTA, 8000);
        worker.setDepartmentId(1L);

        when(departmentService.getDepartmentById(1L)).thenReturn(Optional.of(testDept));
        when(employeeService.getAllEmployees()).thenReturn(List.of(worker));

        mockMvc.perform(get("/departments/details/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/details"))
                .andExpect(model().attributeExists("department", "employeesInDept", "managerName"));
    }

    @Test
    void nonExistingDepartment_shouldReturnErrorView() throws Exception {
        when(departmentService.getDepartmentById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/departments/details/999"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attributeExists("message"));
    }

    @Test
    void departmentDocuments_shouldReturnDocumentsView() throws Exception {
        Department dept = new Department("IT","Firma","Warszawa",1000,null);
        when(departmentService.getDepartmentById(1L)).thenReturn(Optional.of(dept));
        when(departmentDocumentService.listDocuments(1L)).thenReturn(List.of());
        mockMvc.perform(get("/departments/documents/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/documents"))
                .andExpect(model().attributeExists("department","documents","documentTypes"));
    }

    @Test
    void uploadDepartmentDocument_shouldRedirect() throws Exception {
        Department dept = new Department("IT","Firma","Warszawa",1000,null);
        when(departmentService.getDepartmentById(1L)).thenReturn(Optional.of(dept));
        MockMultipartFile file = new MockMultipartFile("file","test.pdf","application/pdf","pdf".getBytes());
        doNothing().when(fileStorageService).validateFile(any(), any(Set.class), anyLong(), any(Set.class));
        when(departmentDocumentService.saveDocument(eq(1L), any(MultipartFile.class), eq(DocumentType.CONTRACT)))
                .thenReturn(new DepartmentDocument(1L,"uuid.pdf","test.pdf",DocumentType.CONTRACT,"/path"));
        mockMvc.perform(multipart("/departments/documents/1").file(file).param("type","CONTRACT"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/departments/documents/1"));
    }

    @Test
    void downloadDepartmentDocument_shouldReturnFile() throws Exception {
        DepartmentDocument doc = new DepartmentDocument(1L, "uuid", "file.pdf", DocumentType.CONTRACT, "path");
        when(departmentDocumentService.findDocument(1L, "doc1")).thenReturn(Optional.of(doc));
        when(fileStorageService.loadFromUploads(anyString(), anyString())).thenReturn(new ByteArrayResource("test".getBytes()));

        mockMvc.perform(get("/departments/documents/1/download/doc1"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"file.pdf\""))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }

    @Test
    void deleteDepartmentDocument_shouldRedirect() throws Exception {
        when(departmentDocumentService.deleteDocument(1L, "doc1")).thenReturn(true);

        mockMvc.perform(get("/departments/documents/1/delete/doc1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/departments/documents/1"));
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public DepartmentService departmentService() {
            return Mockito.mock(DepartmentService.class);
        }
        @Bean
        public EmployeeService employeeService() {
            return Mockito.mock(EmployeeService.class);
        }
        @Bean
        public DepartmentDocumentService departmentDocumentService(FileStorageService fss) {
            return Mockito.mock(DepartmentDocumentService.class);
        }
        @Bean
        public FileStorageService fileStorageService() {
            return Mockito.mock(FileStorageService.class);
        }
    }
}