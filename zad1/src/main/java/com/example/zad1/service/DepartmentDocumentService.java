package com.example.zad1.service;

import com.example.zad1.model.DepartmentDocument;
import com.example.zad1.model.DocumentType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DepartmentDocumentService {

    private final Map<Long, List<DepartmentDocument>> documentsByDepartment = new ConcurrentHashMap<>();
    private final FileStorageService storageService;

    public DepartmentDocumentService(FileStorageService storageService) {
        this.storageService = storageService;
    }

    public DepartmentDocument saveDocument(Long departmentId, MultipartFile file, DocumentType type) {
        String savedName = storageService.storeInUploads(file, Path.of("departments").resolve(String.valueOf(departmentId)).toString());
        String original = Optional.ofNullable(file.getOriginalFilename()).orElse(savedName);
        Path path = storageService.getUploadDir().resolve("departments").resolve(String.valueOf(departmentId)).resolve(savedName);
        DepartmentDocument doc = new DepartmentDocument(departmentId, savedName, original, type, path.toString());
        documentsByDepartment.computeIfAbsent(departmentId, k -> new ArrayList<>()).add(doc);
        return doc;
    }

    public List<DepartmentDocument> listDocuments(Long departmentId) {
        return Collections.unmodifiableList(documentsByDepartment.getOrDefault(departmentId, List.of()));
    }

    public Optional<DepartmentDocument> findDocument(Long departmentId, String docId) {
        return documentsByDepartment.getOrDefault(departmentId, List.of())
                .stream().filter(d -> d.getId().equals(docId)).findFirst();
    }

    public boolean deleteDocument(Long departmentId, String docId) {
        List<DepartmentDocument> list = documentsByDepartment.getOrDefault(departmentId, new ArrayList<>());
        Optional<DepartmentDocument> docOpt = list.stream().filter(d -> d.getId().equals(docId)).findFirst();
        docOpt.ifPresent(doc -> {
            storageService.deleteFromUploads(Path.of("departments").resolve(String.valueOf(departmentId)).toString(), doc.getFileName());
            list.remove(doc);
        });
        return docOpt.isPresent();
    }

}

