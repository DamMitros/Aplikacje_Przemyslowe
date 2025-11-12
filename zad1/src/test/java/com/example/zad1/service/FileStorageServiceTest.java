package com.example.zad1.service;

import com.example.zad1.exception.FileNotFoundException;
import com.example.zad1.exception.InvalidFileException;
import com.example.zad1.exception.FileStorageException;
import com.example.zad1.model.DocumentType;
import com.example.zad1.model.EmployeeDocument;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceTest {

    private Path uploads;
    private Path reports;
    private FileStorageService service;

    @BeforeEach
    void setUp() throws IOException {
        uploads = Files.createTempDirectory("uploads-test-");
        reports = Files.createTempDirectory("reports-test-");
        service = new FileStorageService(uploads.toString(), reports.toString());
        service.ensureDir(service.getUploadDir());
        service.ensureDir(service.getReportsDir());
    }

    @AfterEach
    void tearDown() throws IOException {
        try (Stream<Path> walk = Files.walk(uploads)) {
            walk.sorted((a, b) -> b.getNameCount() - a.getNameCount())
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException ignored) {}
                    });
        }
        try (Stream<Path> walk = Files.walk(reports)) {
            walk.sorted((a, b) -> b.getNameCount() - a.getNameCount())
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException ignored) {}
                    });
        }
    }

    @Test
    void validateFile_allowsValidCsv() {
        MockMultipartFile file = new MockMultipartFile("file", "employees.csv", "text/csv", new byte[]{1, 2, 3});
        assertDoesNotThrow(() -> service.validateFile(file, Set.of("csv"), 10_000, Set.of("text/csv")));
    }

    @Test
    void validateFile_rejectsInvalidExtension() {
        MockMultipartFile file = new MockMultipartFile("file", "bad.txt", "text/plain", new byte[]{1});
        assertThrows(InvalidFileException.class, () -> service.validateFile(file, Set.of("csv"), 10_000, Set.of("text/csv")));
    }

    @Test
    void validateFile_rejectsEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.csv", "text/csv", new byte[]{});
        assertThrows(InvalidFileException.class, () -> service.validateFile(file, Set.of("csv"), 10_000, Set.of("text/csv")));
    }

    @Test
    void validateFile_rejectsTooLargeFile() {
        MockMultipartFile file = new MockMultipartFile("file", "big.csv", "text/csv", new byte[]{1, 2, 3, 4, 5});
        assertThrows(InvalidFileException.class, () -> service.validateFile(file, Set.of("csv"), 4, Set.of("text/csv")));
    }

    @Test
    void validateFile_rejectsInvalidMimeType() {
        MockMultipartFile file = new MockMultipartFile("file", "employees.csv", "text/plain", new byte[]{1});
        assertThrows(InvalidFileException.class, () -> service.validateFile(file, Set.of("csv"), 10_000, Set.of("text/csv")));
    }

    @Test
    void validateFile_allowsWhenNoRestrictions() {
        MockMultipartFile file = new MockMultipartFile("file", "data.any", "application/octet-stream", new byte[]{1});
        assertDoesNotThrow(() -> service.validateFile(file, null, 0, null));
    }

    @Test
    void validateFile_allowsWhenMimeIsNullEvenIfAllowedMimeProvided() {
        MockMultipartFile file = new MockMultipartFile("file", "employees.csv", null, new byte[]{1, 2});
        assertDoesNotThrow(() -> service.validateFile(file, Set.of("csv"), 10_000, Set.of("text/csv")));
    }

    @Test
    void validateFile_nullFile_throws() {
        assertThrows(InvalidFileException.class, () -> service.validateFile(null, Set.of("csv"), 10_000, Set.of("text/csv")));
    }

    @Test
    void probeContentType_returnsGracefully() throws IOException {
        Path tmp = Files.createTempFile(uploads, "sample-", ".csv");
        Files.writeString(tmp, "a,b\n1,2\n");
        String ct = service.probeContentType(tmp);
        assertAll(
                () -> assertDoesNotThrow(() -> service.probeContentType(tmp)),
                () -> assertTrue(ct == null || !ct.isBlank())
        );
    }

    @Test
    void store_load_delete_roundtrip() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", new byte[]{5, 6, 7});
        String savedName = service.storeInUploads(file, "docs");
        Resource res = service.loadFromUploads("docs", savedName);

        byte[] bytes;
        try (java.io.InputStream is = res.getInputStream()) {
            bytes = is.readAllBytes();
        }

        assertAll(
                () -> assertNotNull(savedName),
                () -> assertTrue(res.exists()),
                () -> assertArrayEquals(new byte[]{5, 6, 7}, bytes)
        );

        service.deleteFromUploads("docs", savedName);
        assertThrows(FileNotFoundException.class, () -> service.loadFromUploads("docs", savedName));
    }

    @Test
    void storeInUploads_nullSubfolder_savesInRoot() {
        MockMultipartFile file = new MockMultipartFile("file", "root.txt", "text/plain", new byte[]{3});
        String savedName = service.storeInUploads(file, null);
        assertTrue(Files.exists(uploads.resolve(savedName)));
    }

    @Test
    void save_list_find_delete_employee_document_flow() {
        String email = "john@example.com";
        MockMultipartFile file = new MockMultipartFile("file", "contract.pdf", "application/pdf", new byte[]{9, 8, 7});

        EmployeeDocument doc = service.saveEmployeeDocument(email, file, DocumentType.CONTRACT);
        List<EmployeeDocument> list = service.listEmployeeDocuments(email);
        Optional<EmployeeDocument> found = service.findDocument(email, doc.getId());

        assertAll(
                () -> assertNotNull(doc.getId()),
                () -> assertEquals(email, doc.getEmployeeEmail()),
                () -> assertEquals("contract.pdf", doc.getOriginalFileName()),
                () -> assertEquals(DocumentType.CONTRACT, doc.getFileType()),
                () -> assertTrue(Files.exists(Path.of(doc.getFilePath()))),
                () -> assertEquals(1, list.size()),
                () -> assertEquals(doc.getId(), list.get(0).getId()),
                () -> assertTrue(found.isPresent())
        );

        service.deleteEmployeeDocument(email, doc.getId());
        assertAll(
                () -> assertFalse(Files.exists(Path.of(doc.getFilePath()))),
                () -> assertTrue(service.listEmployeeDocuments(email).isEmpty()),
                () -> assertTrue(service.findDocument(email, doc.getId()).isEmpty())
        );
    }

    @Test
    void findDocument_unknownId_returnsEmpty() {
        String email = "user@example.com";
        MockMultipartFile file = new MockMultipartFile("file", "id.pdf", "application/pdf", new byte[]{1});
        service.saveEmployeeDocument(email, file, DocumentType.ID_CARD);
        assertTrue(service.findDocument(email, "does-not-exist").isEmpty());
    }

    @Test
    void deleteEmployeeDocument_unknownId_noChange() {
        String email = "alice@example.com";
        MockMultipartFile file = new MockMultipartFile("file", "id.png", "image/png", new byte[]{1, 2});
        EmployeeDocument doc = service.saveEmployeeDocument(email, file, DocumentType.ID_CARD);

        service.deleteEmployeeDocument(email, "non-existing-id");
        List<EmployeeDocument> list = service.listEmployeeDocuments(email);

        assertAll(
                () -> assertEquals(1, list.size()),
                () -> assertTrue(Files.exists(Path.of(doc.getFilePath())))
        );
    }

    @Test
    void deleteEmployeeDocument_unknownEmail_noError() {
        service.deleteEmployeeDocument("no@user", "unknown-id");
        assertTrue(service.listEmployeeDocuments("no@user").isEmpty());
    }

    @Test
    void listEmployeeDocuments_emptyForUnknownEmail() {
        List<EmployeeDocument> list = service.listEmployeeDocuments("nobody@example.com");
        assertAll(
                () -> assertNotNull(list),
                () -> assertTrue(list.isEmpty()),
                () -> assertThrows(UnsupportedOperationException.class, () -> list.add(null))
        );
    }

    @Test
    void getExtension_cases() {
        assertAll(
                () -> assertEquals("csv", service.getExtension("a.csv")),
                () -> assertEquals("", service.getExtension("nofile")),
                () -> assertEquals("gz", service.getExtension("archive.tar.gz")),
                () -> assertEquals("hidden", service.getExtension(".hidden"))
        );
    }

    @Test
    void constructor_ensureDir_wrapsIOException_whenPathIsFile() throws IOException {
        Path notADirectory = Files.createTempFile("notdir-", ".tmp");
        try {
            assertThrows(FileStorageException.class, () -> new FileStorageService(notADirectory.toString(), reports.toString()));
        } finally {
            Files.deleteIfExists(notADirectory);
        }
    }

    @Test
    void storeInUploads_wrapsIOExceptionFromMultipartStream() throws IOException {
        MultipartFile mf = Mockito.mock(MultipartFile.class);
        Mockito.when(mf.getOriginalFilename()).thenReturn("a.txt");
        Mockito.when(mf.getInputStream()).thenThrow(new IOException("boom"));
        Mockito.when(mf.getSize()).thenReturn(1L);
        Mockito.when(mf.isEmpty()).thenReturn(false);
        assertThrows(FileStorageException.class, () -> service.storeInUploads(mf, "docs"));
    }

    @Test
    void deleteFromUploads_wrapsIOException_whenTargetIsNonEmptyDirectory() throws IOException {
        Path docs = uploads.resolve("docs");
        Files.createDirectories(docs);
        Path inner = docs.resolve("inner");
        Files.createDirectories(inner);
        Files.writeString(inner.resolve("file.txt"), "content");

        assertAll(
                () -> assertThrows(FileStorageException.class, () -> service.deleteFromUploads("docs", "inner")),
                () -> assertTrue(Files.exists(inner))
        );
    }

    @Test
    void probeContentType_whenIOException_returnsNull() throws IOException {
        Path any = uploads.resolve("x.bin");
        Files.writeString(any, "data");
        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            filesMock.when(() -> Files.probeContentType(any)).thenThrow(new IOException("io"));
            assertNull(service.probeContentType(any));
        }
    }
}
