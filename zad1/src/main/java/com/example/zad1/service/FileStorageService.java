package com.example.zad1.service;

import com.example.zad1.exception.FileNotFoundException;
import com.example.zad1.exception.FileStorageException;
import com.example.zad1.exception.InvalidFileException;
import com.example.zad1.model.DocumentType;
import com.example.zad1.model.EmployeeDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.*;

@Service
public class FileStorageService {
    private final Path uploadDir;
    private final Path reportsDir;

    private final Map<String, List<EmployeeDocument>> documents = new HashMap<>();

    public FileStorageService(
            @Value("${app.upload.directory}") String uploadDir,
            @Value("${app.reports.directory}") String reportsDir
    ) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.reportsDir = Paths.get(reportsDir).toAbsolutePath().normalize();

        ensureDir(this.uploadDir);
        ensureDir(this.reportsDir);
    }

    public Path getUploadDir() { return uploadDir; }
    public Path getReportsDir() { return reportsDir; }

    public void ensureDir(Path dir){
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new FileStorageException("Nie można utworzyć katalogu: " + dir, e);
        }
    }

    public void validateFile(MultipartFile file, Set<String> allowedExtensions, long maxSizeBytes, Set<String> allowedMime){
        if (file == null || file.isEmpty()) throw new InvalidFileException("Plik jest pusty");
        String original = Optional.ofNullable(file.getOriginalFilename()).orElse("");
        String ext = getExtension(original).toLowerCase(Locale.ROOT);
        if (allowedExtensions!=null && !allowedExtensions.isEmpty() && !allowedExtensions.contains(ext))
            throw new InvalidFileException("Niedozwolone rozszerzenie: " + ext);
        if (maxSizeBytes > 0 && file.getSize() > maxSizeBytes)
            throw new InvalidFileException("Plik przekracza dozwolony rozmiar: " + maxSizeBytes + " B");
        String contentType = file.getContentType();
        if (allowedMime != null && !allowedMime.isEmpty() && contentType != null && !allowedMime.contains(contentType))
            throw new InvalidFileException("Niedozwolony typ MIME: " + contentType);
    }

    public String storeInUploads(MultipartFile file, String subfolder){
        String original = Optional.ofNullable(file.getOriginalFilename()).orElse("file");
        String ext = getExtension(original);
        String unique = UUID.randomUUID() + (ext.isBlank()?"":"."+ext);
        Path targetDir = uploadDir.resolve(Optional.ofNullable(subfolder).orElse(""));
        ensureDir(targetDir);
        Path target = targetDir.resolve(unique);
        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return unique;
        } catch (IOException e) {
            throw new FileStorageException("Błąd zapisu pliku", e);
        }
    }

    public Resource loadFromUploads(String subfolder, String fileName){
        Path path = uploadDir.resolve(Optional.ofNullable(subfolder).orElse("")).resolve(fileName).normalize();
        try {
            Resource res = new UrlResource(path.toUri());
            if (res.exists() && res.isReadable()) return res;
            throw new FileNotFoundException("Plik nie istnieje: " + path);
        } catch (MalformedURLException e) {
            throw new FileNotFoundException("Błędna ścieżka: " + path);
        }
    }

    public void deleteFromUploads(String subfolder, String fileName){
        Path path = uploadDir.resolve(Optional.ofNullable(subfolder).orElse("")).resolve(fileName);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new FileStorageException("Błąd usuwania pliku: " + path, e);
        }
    }

    public String getExtension(String name){
        int idx = name.lastIndexOf('.');
        return idx>=0? name.substring(idx+1):"";
    }

    public String probeContentType(Path path){
        try { return Files.probeContentType(path); } catch (IOException e) { return null; }
    }

    public EmployeeDocument saveEmployeeDocument(String email, MultipartFile file, DocumentType type){
        String savedName = storeInUploads(file, Paths.get("documents").resolve(email).toString());
        String original = Optional.ofNullable(file.getOriginalFilename()).orElse(savedName);
        Path path = uploadDir.resolve("documents").resolve(email).resolve(savedName);
        EmployeeDocument doc = new EmployeeDocument(email, savedName, original, type, path.toString());
        documents.computeIfAbsent(email, k -> new ArrayList<>()).add(doc);
        return doc;
    }

    public List<EmployeeDocument> listEmployeeDocuments(String email){
        return Collections.unmodifiableList(documents.getOrDefault(email, List.of()));
    }

    public Optional<EmployeeDocument> findDocument(String email, String documentId){
        return documents.getOrDefault(email, List.of()).stream().filter(d -> d.getId().equals(documentId)).findFirst();
    }

    public void deleteEmployeeDocument(String email, String documentId){
        List<EmployeeDocument> list = documents.getOrDefault(email, new ArrayList<>());
        list.stream().filter(d -> d.getId().equals(documentId)).findFirst().ifPresent(doc -> {
            deleteFromUploads(Paths.get("documents").resolve(email).toString(), doc.getFileName());
            list.remove(doc);
        });
    }
}
