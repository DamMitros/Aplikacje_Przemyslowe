package com.example.zad1.model;

import java.time.Instant;
import java.util.UUID;

public class DepartmentDocument {
    private final String id;
    private final Long departmentId;
    private final String fileName;
    private final String originalFileName;
    private final DocumentType fileType;
    private final Instant uploadDate;
    private final String filePath;

    public DepartmentDocument(Long departmentId, String fileName, String originalFileName, DocumentType fileType, String filePath) {
        this.id = UUID.randomUUID().toString();
        this.departmentId = departmentId;
        this.fileName = fileName;
        this.originalFileName = originalFileName;
        this.fileType = fileType;
        this.uploadDate = Instant.now();
        this.filePath = filePath;
    }

    public String getId() { return id; }
    public Long getDepartmentId() { return departmentId; }
    public String getFileName() { return fileName; }
    public String getOriginalFileName() { return originalFileName; }
    public DocumentType getFileType() { return fileType; }
    public Instant getUploadDate() { return uploadDate; }
    public String getFilePath() { return filePath; }
}

