package com.example.zad1.controller;

import com.example.zad1.exception.EmployeeNotFoundException;
import com.example.zad1.exception.InvalidFileException;
import com.example.zad1.model.DocumentType;
import com.example.zad1.model.Employee;
import com.example.zad1.model.EmployeeDocument;
import com.example.zad1.model.ImportSummary;
import com.example.zad1.service.EmployeeService;
import com.example.zad1.service.FileStorageService;
import com.example.zad1.service.ImportService;
import com.example.zad1.service.ReportGeneratorService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {
    private final EmployeeService employeeService;
    private final FileStorageService storage;
    private final ImportService importService;
    private final ReportGeneratorService reportGeneratorService;

    public FileUploadController(EmployeeService employeeService, FileStorageService storage, ImportService importService, ReportGeneratorService reportGeneratorService) {
        this.employeeService = employeeService;
        this.storage = storage;
        this.importService = importService;
        this.reportGeneratorService = reportGeneratorService;
    }

    @PostMapping("/import/csv")
    public ImportSummary importCsv(@RequestParam("file") MultipartFile file){
        storage.validateFile(file, Set.of("csv"), 10L*1024*1024, Set.of("text/csv","application/vnd.ms-excel","application/csv"));
        String saved = storage.storeInUploads(file, null);
        return importService.importFromCsv(saved);
    }

    @PostMapping("/import/xml")
    public ImportSummary importXml(@RequestParam("file") MultipartFile file){
        storage.validateFile(file, Set.of("xml"), 10L*1024*1024, Set.of("application/xml","text/xml"));
        String saved = storage.storeInUploads(file, null);
        return importService.importFromXml(saved);
    }
    @GetMapping("/export/csv")
    public ResponseEntity<Resource> exportCsv(@RequestParam(value = "company", required = false) String company){
        Resource res = reportGeneratorService.generateCsv(company);
        String fileName = (company==null || company.isBlank()) ? "employees.csv" : "employees-"+company+".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(res);
    }

    @GetMapping("/reports/statistics/{companyName}")
    public ResponseEntity<Resource> statsPdf(@PathVariable String companyName){
        Resource res = reportGeneratorService.generateCompanyStatisticsPdf(companyName);
        String fileName = "statistics-" + companyName + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(res);
    }

    @PostMapping("/documents/{email}")
    public ResponseEntity<EmployeeDocument> uploadDocument(@PathVariable String email,
                                                           @RequestParam("file") MultipartFile file,
                                                           @RequestParam("type") DocumentType type){
        Employee emp = employeeService.GetEmployeeByEmail(email).orElseThrow(() -> new EmployeeNotFoundException(email));
        storage.validateFile(
                file,
                Set.of("pdf","png","jpg","jpeg","doc","docx","txt"),
                10L*1024*1024,
                Set.of(
                        "application/pdf",
                        "image/png",
                        "image/jpeg",
                        "application/msword",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        "text/plain"
                )
        );
        EmployeeDocument doc = storage.saveEmployeeDocument(email, file, type);
        return ResponseEntity.status(201).body(doc);
    }

    @GetMapping("/documents/{email}")
    public List<EmployeeDocument> listDocuments(@PathVariable String email){
        return storage.listEmployeeDocuments(email);
    }

    @GetMapping("/documents/{email}/{documentId}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable String email, @PathVariable String documentId){
        EmployeeDocument doc = storage.findDocument(email, documentId).orElseThrow(() -> new com.example.zad1.exception.FileNotFoundException("Dokument nie istnieje"));
        Resource res = storage.loadFromUploads(Paths.get("documents").resolve(email).toString(), doc.getFileName());
        String originalOrSaved = Optional.ofNullable(doc.getOriginalFileName()).orElse(doc.getFileName());
        String fileNameLower = originalOrSaved.toLowerCase(Locale.ROOT);
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (fileNameLower.endsWith(".pdf")) mediaType = MediaType.APPLICATION_PDF;
        else if (fileNameLower.endsWith(".png")) mediaType = MediaType.IMAGE_PNG;
        else if (fileNameLower.endsWith(".jpg") || fileNameLower.endsWith(".jpeg")) mediaType = MediaType.IMAGE_JPEG;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + originalOrSaved + "\"")
                .contentType(mediaType)
                .body(res);
    }

    @DeleteMapping("/documents/{email}/{documentId}")
    public ResponseEntity<Void> deleteDocument(@PathVariable String email, @PathVariable String documentId){
        storage.deleteEmployeeDocument(email, documentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/photos/{email}")
    public ResponseEntity<Map<String,String>> uploadPhoto(@PathVariable String email, @RequestParam("file") MultipartFile file){
        Employee emp = employeeService.GetEmployeeByEmail(email).orElseThrow(() -> new EmployeeNotFoundException(email));
        storage.validateFile(file, Set.of("jpg","jpeg","png"), 2L*1024*1024, Set.of("image/jpeg","image/png"));
        String ext = storage.getExtension(Optional.ofNullable(file.getOriginalFilename()).orElse("jpg"));
        String fileName = email + (ext.isBlank()?"":"."+ext);
        String savedRandom = storage.storeInUploads(file, "photos");
        Path photosDir = storage.getUploadDir().resolve("photos");
        try {
            String oldPhoto = emp.getPhotoFileName();
            if (oldPhoto != null && !oldPhoto.isBlank() && !oldPhoto.equals(fileName)) {
                storage.deleteFromUploads("photos", oldPhoto);
            }
            Files.move(photosDir.resolve(savedRandom), photosDir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e){
            throw new InvalidFileException("Nie udało się zapisać zdjęcia");
        }
        emp.setPhotoFileName(fileName);
        return ResponseEntity.status(201).body(Map.of("photoFileName", fileName));
    }

    @GetMapping("/photos/{email}")
    public ResponseEntity<Resource> getPhoto(@PathVariable String email){
        Employee emp = employeeService.GetEmployeeByEmail(email).orElseThrow(() -> new EmployeeNotFoundException(email));
        String name = emp.getPhotoFileName();
        if (name == null || name.isBlank()) throw new com.example.zad1.exception.FileNotFoundException("Brak zdjęcia dla pracownika");
        Resource res = storage.loadFromUploads("photos", name);
        MediaType type = name.toLowerCase().endsWith("png")? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG;
        return ResponseEntity.ok().contentType(type).body(res);
    }
}
