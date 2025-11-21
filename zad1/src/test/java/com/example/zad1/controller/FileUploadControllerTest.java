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
    @DisplayName("POST /api/files/import/xml - 200 OK and returns ImportSummary")
    void importXml_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "employees.xml", "application/xml",
                ("<employees>\n" +
                        "  <employee>\n" +
                        "    <fullName>Jan Kowalski</fullName>\n" +
                        "    <email>Kowalskiii@outlook.ru</email>\n" +
                        "    <companyName>TechCorp</companyName>\n" +
                        "    <position>PREZES</position>\n" +
                        "    <salary>10000</salary>\n" +
                        "  </employee>\n" +
                        "</employees>").getBytes(StandardCharsets.UTF_8));
        when(fileStorageService.storeInUploads(any(), isNull())).thenReturn("tmp.xml");
        when(importService.importFromXml(anyString())).thenReturn(new ImportSummary(1, List.of()));
        mockMvc.perform(multipart("/api/files/import/xml").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.importedCount").value(1))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @DisplayName("POST /api/files/import/xml - 400 Bad Request on invalid extension")
    void importXml_invalidExtension_returns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "bad.txt", "text/plain", "hello".getBytes());
        Mockito.doThrow(new InvalidFileException("Niedozwolone rozszerzenie"))
                .when(fileStorageService).validateFile(any(), anySet(), anyLong(), anySet());

        mockMvc.perform(multipart("/api/files/import/xml").file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/files/import/xml - 413 Payload Too Large")
    void importXml_tooLarge_returns413_additional() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "employees.xml", "application/xml", new byte[1]);
        Mockito.doThrow(new MaxUploadSizeExceededException(10L))
                .when(fileStorageService).validateFile(any(), anySet(), anyLong(), anySet());

        mockMvc.perform(multipart("/api/files/import/xml").file(file))
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
    @DisplayName("GET /api/files/export/csv?company='' - default filename employees.csv")
    void exportCsv_blankCompany_usesDefaultFileName() throws Exception {
        String csv = "fullName,email,companyName,position,salary\n";
        Resource res = new ByteArrayResource(csv.getBytes(StandardCharsets.UTF_8));
        when(reportGeneratorService.generateCsv(eq(""))).thenReturn(res);

        mockMvc.perform(get("/api/files/export/csv").param("company", ""))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("employees.csv")));
    }

    @Test
    @DisplayName("POST /api/files/documents/{email} - 201 Created and returns EmployeeDocument")
    void uploadDocument_success_returns201() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "contract.pdf", "application/pdf", new byte[]{1,2,3});
        String email = "jan@example.com";
        when(employeeService.getEmployeeByEmail(email)).thenReturn(Optional.of(new Employee("Jan Kowalski", email, "TechCorp", com.example.zad1.model.Position.PREZES, 10000)));
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
    @DisplayName("POST /api/files/documents/{email} - 404 gdy brak pracownika")
    void uploadDocument_employeeNotFound_returns404() throws Exception {
        String email = "missing@example.com";
        when(employeeService.getEmployeeByEmail(email)).thenReturn(Optional.empty());
        MockMultipartFile file = new MockMultipartFile("file", "contract.pdf", "application/pdf", new byte[]{1});

        mockMvc.perform(multipart("/api/files/documents/{email}", email)
                        .file(file)
                        .param("type", "CONTRACT"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/files/documents/{email} - 400 na nieprawidłowe rozszerzenie")
    void uploadDocument_invalidExtension_returns400() throws Exception {
        String email = "jan@example.com";
        when(employeeService.getEmployeeByEmail(email)).thenReturn(Optional.of(new Employee("Jan", email, "Tech", com.example.zad1.model.Position.PROGRAMISTA, 5000)));
        MockMultipartFile file = new MockMultipartFile("file", "bad.exe", "application/octet-stream", new byte[]{1});
        Mockito.doThrow(new InvalidFileException("Niedozwolone rozszerenie"))
                .when(fileStorageService).validateFile(any(), anySet(), anyLong(), anySet());

        mockMvc.perform(multipart("/api/files/documents/{email}", email)
                        .file(file)
                        .param("type", "CONTRACT"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api.files/documents/{email} - 200 OK returns JSON list")
    void listDocuments_success() throws Exception {
        String email = "jan@example.com";
        when(fileStorageService.listEmployeeDocuments(email)).thenReturn(List.of(
                new EmployeeDocument(email, "saved1.pdf", "doc1.pdf", DocumentType.CONTRACT, "/p1"),
                new EmployeeDocument(email, "saved2.pdf", "doc2.pdf", DocumentType.CERTIFICATE, "/p2")
        ));
        mockMvc.perform(get("/api/files/documents/{email}", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employeeEmail").value(email))
                .andExpect(jsonPath("$[1].originalFileName").value("doc2.pdf"));
    }

    @Test
    @DisplayName("GET /api/files/documents/{email}/{documentId} - 200 OK download document")
    void downloadDocument_success() throws Exception {
        String email = "jan@example.com";
        String docId = "doc-123";
        var employee = new Employee("Jan Kowalski", email, "TechCorp", com.example.zad1.model.Position.PREZES, 10000);
        when(employeeService.getEmployeeByEmail(email)).thenReturn(Optional.of(employee));
        var stored = new EmployeeDocument(email, "saved.pdf", "contract.pdf", DocumentType.CONTRACT, "/uploads/documents/jan@example.com/saved.pdf");
        when(fileStorageService.findDocument(email, docId)).thenReturn(Optional.of(stored));
        when(fileStorageService.loadFromUploads(anyString(), anyString())).thenReturn(new ByteArrayResource(new byte[]{1,2,3}));

        mockMvc.perform(get("/api/files/documents/{email}/{documentId}", email, docId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("contract.pdf")))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }

    @Test
    @DisplayName("GET /api/files/documents/{email}/{documentId} - 200 OK download PNG")
    void downloadDocument_png_success() throws Exception {
        String email = "jan@example.com";
        String docId = "doc-123";
        var stored = new EmployeeDocument(email, "saved.png", "image.png", DocumentType.CONTRACT, "/uploads/documents/jan@example.com/saved.png");
        when(fileStorageService.findDocument(email, docId)).thenReturn(Optional.of(stored));
        when(fileStorageService.loadFromUploads(anyString(), anyString())).thenReturn(new ByteArrayResource(new byte[]{1}));

        mockMvc.perform(get("/api/files/documents/{email}/{documentId}", email, docId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.IMAGE_PNG.toString()))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("image.png")));
    }

    @Test
    @DisplayName("GET /api/files/documents/{email}/{documentId} - 200 OK download JPG")
    void downloadDocument_jpg_success() throws Exception {
        String email = "jan@example.com";
        String docId = "doc-456";
        var stored = new EmployeeDocument(email, "saved.jpg", "photo.jpg", DocumentType.CONTRACT, "/uploads/documents/jan@example.com/saved.jpg");
        when(fileStorageService.findDocument(email, docId)).thenReturn(Optional.of(stored));
        when(fileStorageService.loadFromUploads(anyString(), anyString())).thenReturn(new ByteArrayResource(new byte[]{1}));

        mockMvc.perform(get("/api/files/documents/{email}/{documentId}", email, docId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.IMAGE_JPEG.toString()))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("photo.jpg")));
    }

    @Test
    @DisplayName("GET /api/files/documents/{email}/{documentId} - 404 gdy brak dokumentu")
    void downloadDocument_notFound_returns404() throws Exception {
        String email = "jan@example.com";
        String docId = "doc-404";
        when(fileStorageService.findDocument(email, docId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/files/documents/{email}/{documentId}", email, docId))
                .andExpect(status().isNotFound());
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
    @DisplayName("POST /api/files/photos/{email} - 201 Created on valid image upload")
    void uploadPhoto_success() throws Exception {
        String email = "anna@example.com";
        when(employeeService.getEmployeeByEmail(email)).thenReturn(Optional.of(new Employee("Anna Nowak", email, "TechCorp", com.example.zad1.model.Position.PREZES, 9000)));
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
        when(employeeService.getEmployeeByEmail(email)).thenReturn(Optional.of(new Employee("Anna Nowak", email, "TechCorp", com.example.zad1.model.Position.PREZES, 9000)));
        MockMultipartFile file = new MockMultipartFile("file", "photo.gif", "image/gif", new byte[]{1,2});
        Mockito.doThrow(new InvalidFileException("Niedozwolone rozszerzenie"))
                .when(fileStorageService).validateFile(any(), anySet(), anyLong(), anySet());

        mockMvc.perform(multipart("/api/files/photos/{email}", email).file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/files/photos/{email} - usuwa stare zdjęcie jeśli nazwa inna")
    void uploadPhoto_replacesOldPhoto_deletesOld() throws Exception {
        String email = "old@example.com";
        var emp = new Employee("Old Photo", email, "Tech", com.example.zad1.model.Position.PROGRAMISTA, 7000);
        emp.setPhotoFileName("old.jpg");
        when(employeeService.getEmployeeByEmail(email)).thenReturn(Optional.of(emp));
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", new byte[]{1});
        when(fileStorageService.storeInUploads(any(), eq("photos"))).thenReturn("random.jpg");
        Path tmp = Files.createTempDirectory("uploadsTest2");
        Files.createDirectories(tmp.resolve("photos"));
        Files.write(tmp.resolve("photos").resolve("random.jpg"), new byte[]{1});
        when(fileStorageService.getUploadDir()).thenReturn(tmp);
        when(fileStorageService.getExtension(anyString())).thenReturn("jpg");

        mockMvc.perform(multipart("/api/files/photos/{email}", email).file(file))
                .andExpect(status().isCreated());

        Mockito.verify(fileStorageService).deleteFromUploads("photos", "old.jpg");
    }

    @Test
    @DisplayName("POST /api/files/photos/{email} - 404 gdy brak pracownika")
    void uploadPhoto_employeeNotFound_returns404() throws Exception {
        String email = "missing@example.com";
        when(employeeService.getEmployeeByEmail(email)).thenReturn(Optional.empty());
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", new byte[]{1});

        mockMvc.perform(multipart("/api/files/photos/{email}", email).file(file))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/files/photos/{email} - 400 gdy błąd podczas zapisu (move)")
    void uploadPhoto_moveFailure_returns400() throws Exception {
        String email = "fail@example.com";
        when(employeeService.getEmployeeByEmail(email)).thenReturn(Optional.of(new Employee("X", email, "Tech", com.example.zad1.model.Position.PROGRAMISTA, 4000)));
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", new byte[]{1});
        when(fileStorageService.storeInUploads(any(), eq("photos"))).thenReturn("notExisting.jpg");
        Path tmp = Files.createTempDirectory("uploadsTest3");
        Files.createDirectories(tmp.resolve("photos"));
        when(fileStorageService.getUploadDir()).thenReturn(tmp);
        when(fileStorageService.getExtension(anyString())).thenReturn("jpg");

        mockMvc.perform(multipart("/api/files/photos/{email}", email).file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/files/photos/{email} - 404 gdy brak użytkownika")
    void getPhoto_employeeNotFound_returns404() throws Exception {
        String email = "none@example.com";
        when(employeeService.getEmployeeByEmail(email)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/files/photos/{email}", email))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/files/photos/{email} - 200 OK PNG")
    void getPhoto_success_png() throws Exception {
        String email = "png@example.com";
        var emp = new Employee("PNG", email, "Tech", com.example.zad1.model.Position.MANAGER, 6000);
        emp.setPhotoFileName("img.png");
        when(employeeService.getEmployeeByEmail(email)).thenReturn(Optional.of(emp));
        when(fileStorageService.loadFromUploads(eq("photos"), eq("img.png"))).thenReturn(new ByteArrayResource(new byte[]{1}));

        mockMvc.perform(get("/api/files/photos/{email}", email))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.IMAGE_PNG.toString()));
    }

    @Test
    @DisplayName("GET /api/files/photos/{email} - 200 OK JPEG")
    void getPhoto_success_jpeg() throws Exception {
        String email = "jpg@example.com";
        var emp = new Employee("JPG", email, "Tech", com.example.zad1.model.Position.MANAGER, 6000);
        emp.setPhotoFileName("img.jpg");
        when(employeeService.getEmployeeByEmail(email)).thenReturn(Optional.of(emp));
        when(fileStorageService.loadFromUploads(eq("photos"), eq("img.jpg"))).thenReturn(new ByteArrayResource(new byte[]{1}));

        mockMvc.perform(get("/api/files/photos/{email}", email))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.IMAGE_JPEG.toString()));
    }

    @Test
    @DisplayName("GET /api/files/documents/{email}/{documentId} - 200 OK unknown extension -> application/octet-stream")
    void downloadDocument_unknownExtension_defaultsOctetStream() throws Exception {
        String email = "jan@example.com";
        String docId = "doc-789";
        var stored = new EmployeeDocument(email, "saved.bin", "artifact.bin", DocumentType.CONTRACT, "/uploads/documents/jan@example.com/saved.bin");
        when(fileStorageService.findDocument(email, docId)).thenReturn(Optional.of(stored));
        when(fileStorageService.loadFromUploads(anyString(), anyString())).thenReturn(new ByteArrayResource(new byte[]{1}));

        mockMvc.perform(get("/api/files/documents/{email}/{documentId}", email, docId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_OCTET_STREAM.toString()))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("artifact.bin")));
    }

    @Test
    @DisplayName("GET /api/files/photos/{email} - 404 gdy brak zdjęcia")
    void getPhoto_notFound() throws Exception {
        String email = "brak@example.com";
        var emp = new Employee("Brak Zdjęcia", email, "TechCorp", com.example.zad1.model.Position.PREZES, 5000);
        emp.setPhotoFileName(null);
        when(employeeService.getEmployeeByEmail(email)).thenReturn(Optional.of(emp));
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
