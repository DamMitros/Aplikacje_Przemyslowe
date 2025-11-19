package com.example.zad1.service;

import com.example.zad1.model.DepartmentDocument;
import com.example.zad1.model.DocumentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentDocumentServiceTest {

    @Mock
    private FileStorageService fileStorageService;

    private DepartmentDocumentService departmentDocumentService;

    @BeforeEach
    void setUp() {
        departmentDocumentService = new DepartmentDocumentService(fileStorageService);
    }

    @Test
    void saveDocument_shouldCreateAndReturnDoc() {
        when(fileStorageService.getUploadDir()).thenReturn(Paths.get("uploads"));
        Long deptId = 5L;
        String storedName = "uuid123.pdf";
        when(fileStorageService.storeInUploads(any(), eq("departments/" + deptId))).thenReturn(storedName);

        DepartmentDocument doc = departmentDocumentService.saveDocument(deptId, new FakeMultipartFile("plik.pdf"), DocumentType.CONTRACT);

        assertAll(
                () -> assertNotNull(doc.getId(), "Generowane ID nie powinno być null"),
                () -> assertEquals(deptId, doc.getDepartmentId()),
                () -> assertEquals(storedName, doc.getFileName()),
                () -> assertEquals("plik.pdf", doc.getOriginalFileName()),
                () -> assertEquals(DocumentType.CONTRACT, doc.getFileType()),
                () -> assertTrue(doc.getFilePath().contains("departments"), "Ścieżka powinna zawierać katalog departments")
        );
    }

    @Test
    void listDocuments_emptyAndUnmodifiable() {
        Long deptId = 10L;
        List<DepartmentDocument> list = departmentDocumentService.listDocuments(deptId);

        assertAll(
                () -> assertTrue(list.isEmpty()),
                () ->  assertThrows(UnsupportedOperationException.class, () -> list.add(mock(DepartmentDocument.class)), "Lista ma być niemodyfikowalna")
        );
    }

    @Test
    void findDocument_shouldReturnOptional() {
        when(fileStorageService.getUploadDir()).thenReturn(Paths.get("uploads"));
        Long deptId = 2L;
        when(fileStorageService.storeInUploads(any(), eq("departments/" + deptId))).thenReturn("fileA.pdf");
        DepartmentDocument saved = departmentDocumentService.saveDocument(deptId, new FakeMultipartFile("A.pdf"), DocumentType.CONTRACT);

        Optional<DepartmentDocument> found = departmentDocumentService.findDocument(deptId, saved.getId());
        assertAll(
                () -> assertTrue(found.isPresent()),
                () -> assertEquals(saved.getId(), found.get().getId())
        );
    }

    @Test
    void deleteDocument_shouldRemove() {
        when(fileStorageService.getUploadDir()).thenReturn(Paths.get("uploads"));
        Long deptId = 3L;
        when(fileStorageService.storeInUploads(any(), eq("departments/" + deptId))).thenReturn("fileB.pdf");
        DepartmentDocument saved = departmentDocumentService.saveDocument(deptId, new FakeMultipartFile("B.pdf"), DocumentType.CONTRACT);

        boolean first = departmentDocumentService.deleteDocument(deptId, saved.getId());
        boolean second = departmentDocumentService.deleteDocument(deptId, saved.getId());

        assertAll(
                () -> assertTrue(first, "Pierwsze usunięcie powinno zwrócić true"),
                () -> assertFalse(second, "Drugie usunięcie nie powinno znaleźć dokumentu")
        );
    }

    private static class FakeMultipartFile implements org.springframework.web.multipart.MultipartFile {
        private final String originalFilename;
        FakeMultipartFile(String originalFilename) { this.originalFilename = originalFilename; }
        @Override public String getName() { return originalFilename; }
        @Override public String getOriginalFilename() { return originalFilename; }
        @Override public String getContentType() { return "application/pdf"; }
        @Override public boolean isEmpty() { return false; }
        @Override public long getSize() { return 10L; }
        @Override public byte[] getBytes() { return new byte[0]; }
        @Override public java.io.InputStream getInputStream() { return new java.io.ByteArrayInputStream(new byte[0]); }
        @Override public void transferTo(java.io.File dest) { /* no-op */ }
    }
}
