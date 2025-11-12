package com.example.zad1.controller;

import com.example.zad1.exception.GlobalExceptionHandler;
import com.example.zad1.exception.InvalidFileException;
import com.example.zad1.model.DocumentType;
import com.example.zad1.model.Employee;
import com.example.zad1.model.EmployeeDocument;
import com.example.zad1.model.ImportSummary;
import com.example.zad1.service.EmployeeService;
import com.example.zad1.service.FileStorageService;
import com.example.zad1.service.ImportService;
import com.example.zad1.service.ReportGeneratorService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FileUploadController.class)
@ContextConfiguration(classes = {FileUploadController.class, GlobalExceptionHandler.class, FileUploadControllerTest.TestConfig.class})
class FileUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private ImportService importService;
    @Autowired
    private ReportGeneratorService reportGeneratorService;

    @TestConfiguration
    static class TestConfig {
        @Bean EmployeeService employeeService(){ return Mockito.mock(EmployeeService.class); }
        @Bean FileStorageService fileStorageService(){ return Mockito.mock(FileStorageService.class); }
        @Bean ImportService importService(){ return Mockito.mock(ImportService.class); }
        @Bean ReportGeneratorService reportGeneratorService(){ return Mockito.mock(ReportGeneratorService.class); }
    }

    @AfterEach
    void resetMocks(){
        Mockito.reset(fileStorageService);
    }

    @Test
    @DisplayName("POST /api/files/import/csv - 200 OK and returns ImportSummary")
    void importCsv_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "employees.csv", "text/csv",
                ("fullName,email,companyName,position,salary\n" +
                        "Jan,Kowalski,TechCorp,PREZES,10000\n").getBytes(StandardCharsets.UTF_8));

        when(fileStorageService.storeInUploads(any(), isNull())).thenReturn("tmp.csv");
        when(importService.importFromCsv(anyString())).thenReturn(new ImportSummary(1, List.of()));

        mockMvc.perform(multipart("/api/files/import/csv").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.importedCount").value(1))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @DisplayName("POST /api/files/import/csv - 400 Bad Request on invalid extension")
    void importCsv_invalidExtension_returns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "bad.txt", "text/plain", "hello".getBytes());
        Mockito.doThrow(new InvalidFileException("Niedozwolone rozszerzenie"))
                .when(fileStorageService).validateFile(any(), anySet(), anyLong(), anySet());

        mockMvc.perform(multipart("/api/files/import/csv").file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/files/import/csv - 413 Payload Too Large when MaxUploadSizeExceeded")
    void importCsv_tooLarge_returns413() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "employees.csv", "text/csv", new byte[1]);
        Mockito.doThrow(new MaxUploadSizeExceededException(10L))
                .when(fileStorageService).validateFile(any(), anySet(), anyLong(), anySet());

        mockMvc.perform(multipart("/api/files/import/csv").file(file))
                .andExpect(status().isPayloadTooLarge());
    }

    @Test
    @DisplayName("GET /api/files/export/csv - 200 OK with CSV headers")
    void exportCsv_success() throws Exception {
        String csv = "fullName,email,companyName,position,salary\n";
        Resource res = new ByteArrayResource(csv.getBytes(StandardCharsets.UTF_8));
        when(reportGeneratorService.generateCsv(isNull())).thenReturn(res);

        mockMvc.perform(get("/api/files/export/csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("text/csv")))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")));
    }

    @Test
    @DisplayName("GET /api/files/export/csv?company=TechCorp - 200 OK with filtered filename")
    void exportCsv_withCompany_success() throws Exception {
        String csv = "fullName,email,companyName,position,salary\n";
        Resource res = new ByteArrayResource(csv.getBytes(StandardCharsets.UTF_8));
        when(reportGeneratorService.generateCsv(eq("TechCorp"))).thenReturn(res);

        mockMvc.perform(get("/api/files/export/csv").param("company", "TechCorp"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("employees-TechCorp.csv")));
    }

    @Test
    @DisplayName("POST /api/files/documents/{email} - 201 Created and returns EmployeeDocument")
    void uploadDocument_success_returns201() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "contract.pdf", "application/pdf", new byte[]{1,2,3});
        String email = "jan@example.com";
        when(employeeService.GetEmployeeByEmail(email)).thenReturn(Optional.of(new Employee("Jan Kowalski", email, "TechCorp", com.example.zad1.model.Position.PREZES, 10000)));
        when(fileStorageService.saveEmployeeDocument(eq(email), any(), eq(DocumentType.CONTRACT)))
                .thenAnswer(inv -> new com.example.zad1.model.EmployeeDocument(email, "uuid.pdf", "contract.pdf", DocumentType.CONTRACT, "/uploads/documents/jan@example.com/uuid.pdf"));

        mockMvc.perform(multipart("/api/files/documents/{email}", email)
                        .file(file)
                        .param("type", "CONTRACT"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employeeEmail").value(email))
                .andExpect(jsonPath("$.originalFileName").value("contract.pdf"))
                .andExpect(jsonPath("$.fileType").value("CONTRACT"));
    }

    @Test
    @DisplayName("POST /api/files/photos/{email} - 201 Created on valid image upload")
    void uploadPhoto_success() throws Exception {
        String email = "anna@example.com";
        when(employeeService.GetEmployeeByEmail(email)).thenReturn(Optional.of(new Employee("Anna Nowak", email, "TechCorp", com.example.zad1.model.Position.PREZES, 9000)));
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", new byte[]{1,2});
        when(fileStorageService.storeInUploads(any(), eq("photos"))).thenReturn("randomname.jpg");
        Path tmp = Files.createTempDirectory("uploadsTest");
        Files.createDirectories(tmp.resolve("photos"));
        Files.write(tmp.resolve("photos").resolve("randomname.jpg"), new byte[]{1,2});
        when(fileStorageService.getUploadDir()).thenReturn(tmp);
        when(fileStorageService.getExtension(anyString())).thenReturn("jpg");

        mockMvc.perform(multipart("/api/files/photos/{email}", email).file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.photoFileName", org.hamcrest.Matchers.containsString(email)));
    }

    @Test
    @DisplayName("POST /api/files/photos/{email} - 400 Bad Request on invalid image extension")
    void uploadPhoto_invalidExtension() throws Exception {
        String email = "anna@example.com";
        when(employeeService.GetEmployeeByEmail(email)).thenReturn(Optional.of(new Employee("Anna Nowak", email, "TechCorp", com.example.zad1.model.Position.PREZES, 9000)));
        MockMultipartFile file = new MockMultipartFile("file", "photo.gif", "image/gif", new byte[]{1,2});
        Mockito.doThrow(new InvalidFileException("Niedozwolone rozszerzenie"))
                .when(fileStorageService).validateFile(any(), anySet(), anyLong(), anySet());

        mockMvc.perform(multipart("/api/files/photos/{email}", email).file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/files/documents/{email}/{documentId} - 200 OK download document")
    void downloadDocument_success() throws Exception {
        String email = "jan@example.com";
        String docId = "doc-123";
        var employee = new Employee("Jan Kowalski", email, "TechCorp", com.example.zad1.model.Position.PREZES, 10000);
        when(employeeService.GetEmployeeByEmail(email)).thenReturn(Optional.of(employee));
        var stored = new EmployeeDocument(email, "saved.pdf", "contract.pdf", DocumentType.CONTRACT, "/uploads/documents/jan@example.com/saved.pdf");
        when(fileStorageService.findDocument(email, docId)).thenReturn(Optional.of(stored));
        when(fileStorageService.loadFromUploads(anyString(), anyString())).thenReturn(new ByteArrayResource(new byte[]{1,2,3}));

        mockMvc.perform(get("/api/files/documents/{email}/{documentId}", email, docId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("contract.pdf")))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }

    @Test
    @DisplayName("DELETE /api/files/documents/{email}/{documentId} - 204 No Content")
    void deleteDocument_success() throws Exception {
        String email = "jan@example.com";
        String docId = "doc-123";
        mockMvc.perform(delete("/api/files/documents/{email}/{documentId}", email, docId))
                .andExpect(status().isNoContent());
        Mockito.verify(fileStorageService).deleteEmployeeDocument(email, docId);
    }

    @Test
    @DisplayName("GET /api/files/photos/{email} - 404 gdy brak zdjęcia")
    void getPhoto_notFound() throws Exception {
        String email = "brak@example.com";
        when(employeeService.GetEmployeeByEmail(email)).thenReturn(Optional.of(new Employee("Brak Zdjęcia", email, "TechCorp", com.example.zad1.model.Position.PREZES, 5000)));
        mockMvc.perform(get("/api/files/photos/{email}", email))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/files/reports/statistics/{companyName} - 200 OK PDF")
    void statsPdf_success() throws Exception {
        String company = "TechCorp";
        when(reportGeneratorService.generateCompanyStatisticsPdf(company)).thenReturn(new ByteArrayResource(new byte[]{1,2}));
        mockMvc.perform(get("/api/files/reports/statistics/{companyName}", company))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("application/pdf")))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("statistics-"+company+".pdf")));
    }
}
