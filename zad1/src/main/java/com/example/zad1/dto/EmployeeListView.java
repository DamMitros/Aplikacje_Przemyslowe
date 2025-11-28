package com.example.zad1.dto;

import com.example.zad1.model.EmploymentStatus;
import org.springframework.beans.factory.annotation.Value;

public interface EmployeeListView {
    String getFullName();
    String getEmail();
    String getPosition();
    String getCompanyName();
    Integer getSalary();
    String getStatus();

    @Value("#{target.department != null ? target.department.name : 'Brak'}")
    String getDepartmentName();
}