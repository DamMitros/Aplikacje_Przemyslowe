package com.example.zad1.controller;

import com.example.zad1.model.Department;
import com.example.zad1.model.DepartmentDocument;
import com.example.zad1.model.DocumentType;
import com.example.zad1.model.Employee;
import com.example.zad1.service.DepartmentDocumentService;
import com.example.zad1.service.DepartmentService;
import com.example.zad1.service.EmployeeService;
import com.example.zad1.service.FileStorageService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/departments")
public class DepartmentViewController {

    private final DepartmentService departmentService;
    private final EmployeeService employeeService;
    private final DepartmentDocumentService departmentDocumentService;
    private final FileStorageService fileStorageService;

    public DepartmentViewController(DepartmentService departmentService, EmployeeService employeeService, DepartmentDocumentService departmentDocumentService, FileStorageService fileStorageService) {
        this.departmentService = departmentService;
        this.employeeService = employeeService;
        this.departmentDocumentService = departmentDocumentService;
        this.fileStorageService = fileStorageService;
    }

    private List<Employee> getPotentialManagers() {
        return employeeService.getAllEmployees().stream()
                .filter(e -> e.getPosition() != null && e.getPosition().getHierarchy() <= 3)
                .collect(Collectors.toList());
    }

    @GetMapping
    public String listDepartments(Model model) {
        model.addAttribute("departments", departmentService.getAllDepartments());
        Map<String, String> employeeNames = employeeService.getAllEmployees().stream()
                .collect(Collectors.toMap(Employee::getEmail, Employee::getFullName));
        model.addAttribute("employeeNames", employeeNames);
        return "departments/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("department", new Department());
        model.addAttribute("managers", getPotentialManagers());
        return "departments/form";
    }

    @PostMapping("/add")
    public String addDepartment(@Valid @ModelAttribute("department") Department department,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("managers", getPotentialManagers());
            return "departments/form";
        }
        departmentService.addDepartment(department);
        redirectAttributes.addFlashAttribute("message", "Departament dodany pomyślnie.");
        return "redirect:/departments";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Department department = departmentService.getDepartmentById(id).orElse(null);
        if (department == null) {
            model.addAttribute("message", "Nie znaleziono departamentu: " + id);
            return "error";
        }
        model.addAttribute("department", department);
        model.addAttribute("managers", getPotentialManagers());
        return "departments/form";
    }

    @PostMapping("/edit/{id}")
    public String updateDepartment(@PathVariable Long id,
                                   @Valid @ModelAttribute("department") Department department,
                                   BindingResult bindingResult,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("managers", getPotentialManagers());
            return "departments/form";
        }
        departmentService.updateDepartment(id, department);
        redirectAttributes.addFlashAttribute("message", "Departament zaktualizowany.");
        return "redirect:/departments";
    }

    @GetMapping("/delete/{id}")
    public String deleteDepartment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        departmentService.deleteDepartment(id);
        redirectAttributes.addFlashAttribute("message", "Departament usunięty.");
        return "redirect:/departments";
    }

    @GetMapping("/details/{id}")
    public String departmentDetails(@PathVariable Long id, Model model) {
        Department department = departmentService.getDepartmentById(id).orElse(null);
        if (department == null) {
            model.addAttribute("message", "Nie znaleziono departamentu: " + id);
            return "error";
        }
        List<Employee> employeesInDept = employeeService.getAllEmployees().stream()
                .filter(e -> Objects.equals(e.getDepartmentId(), id))
                .collect(Collectors.toList());

        String managerName = department.getManagerEmail() != null ?
                employeeService.GetEmployeeByEmail(department.getManagerEmail())
                        .map(Employee::getFullName)
                        .orElse("Brak danych") : "Nieprzypisany";

        model.addAttribute("department", department);
        model.addAttribute("employeesInDept", employeesInDept);
        model.addAttribute("managerName", managerName);
        return "departments/details";
    }

    @GetMapping("/documents/{id}")
    public String departmentDocuments(@PathVariable Long id, Model model) {
        Department department = departmentService.getDepartmentById(id).orElse(null);
        if (department == null) {
            model.addAttribute("message", "Nie znaleziono departamentu: " + id);
            return "error";
        }
        model.addAttribute("department", department);
        model.addAttribute("documents", departmentDocumentService.listDocuments(id));
        model.addAttribute("documentTypes", DocumentType.values());
        return "departments/documents";
    }

    @PostMapping("/documents/{id}")
    public String uploadDepartmentDocument(@PathVariable Long id,
                                           @RequestParam("file") MultipartFile file,
                                           @RequestParam("type") DocumentType type,
                                           RedirectAttributes redirectAttributes,
                                           Model model) {
        Department department = departmentService.getDepartmentById(id).orElse(null);
        if (department == null) {
            model.addAttribute("message", "Nie znaleziono departamentu: " + id);
            return "error";
        }
        try {
            fileStorageService.validateFile(file, Set.of("pdf","png","jpg","jpeg","doc","docx","txt"), 10L*1024*1024,
                    Set.of("application/pdf","image/png","image/jpeg","application/msword","application/vnd.openxmlformats-officedocument.wordprocessingml.document","text/plain"));
            departmentDocumentService.saveDocument(id, file, type);
            redirectAttributes.addFlashAttribute("message", "Dokument zapisany pomyślnie.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Błąd uploadu: " + e.getMessage());
        }
        return "redirect:/departments/documents/" + id;
    }

    @GetMapping("/documents/{id}/download/{docId}")
    public ResponseEntity<Resource> downloadDepartmentDocument(@PathVariable Long id, @PathVariable String docId) {
        DepartmentDocument doc = departmentDocumentService.findDocument(id, docId)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono dokumentu"));
        Resource res = fileStorageService.loadFromUploads("departments/" + id, doc.getFileName());
        String nameLower = doc.getOriginalFileName().toLowerCase(Locale.ROOT);
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (nameLower.endsWith(".pdf")) mediaType = MediaType.APPLICATION_PDF;
        else if (nameLower.endsWith(".png")) mediaType = MediaType.IMAGE_PNG;
        else if (nameLower.endsWith(".jpg") || nameLower.endsWith(".jpeg")) mediaType = MediaType.IMAGE_JPEG;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getOriginalFileName() + "\"")
                .contentType(mediaType)
                .body(res);
    }

    @GetMapping("/documents/{id}/delete/{docId}")
    public String deleteDepartmentDocument(@PathVariable Long id, @PathVariable String docId, RedirectAttributes redirectAttributes) {
        boolean removed = departmentDocumentService.deleteDocument(id, docId);
        redirectAttributes.addFlashAttribute(removed ? "message" : "errorMessage", removed ? "Dokument usunięty." : "Nie znaleziono dokumentu.");
        return "redirect:/departments/documents/" + id;
    }
}