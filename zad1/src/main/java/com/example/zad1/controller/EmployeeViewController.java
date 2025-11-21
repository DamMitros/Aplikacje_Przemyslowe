package com.example.zad1.controller;

import com.example.zad1.model.Employee;
import com.example.zad1.model.EmploymentStatus;
import com.example.zad1.model.ImportSummary;
import com.example.zad1.model.Position;
import com.example.zad1.service.DepartmentService;
import com.example.zad1.service.EmployeeService;
import com.example.zad1.service.FileStorageService;
import com.example.zad1.service.ImportService;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/employees")
public class EmployeeViewController {

    private final EmployeeService employeeService;
    private final ImportService importService;
    private final FileStorageService storageService;
    private final DepartmentService departmentService;

    public EmployeeViewController(EmployeeService employeeService, ImportService importService, FileStorageService storageService, DepartmentService departmentService) {
        this.employeeService = employeeService;
        this.importService = importService;
        this.storageService = storageService;
        this.departmentService = departmentService;
    }

    @GetMapping
    public String listEmployees(Model model) {
        model.addAttribute("employees", employeeService.getAllEmployees());
        return "employees/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("employee", new Employee());
        model.addAttribute("positions", Position.values());
        model.addAttribute("statuses", EmploymentStatus.values());
        model.addAttribute("departments", departmentService.getAllDepartments());
        return "employees/add-form";
    }

    @PostMapping("/add")
    public String addEmployee(@Valid @ModelAttribute("employee") Employee employee,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("positions", Position.values());
            model.addAttribute("statuses", EmploymentStatus.values());
            model.addAttribute("departments", departmentService.getAllDepartments());
            return "employees/add-form";
        }
        if (employeeService.getEmployeeByEmail(employee.getEmail()).isPresent()) {
            bindingResult.rejectValue("email", "error.employee", "Pracownik o tym emailu już istnieje");
            model.addAttribute("positions", Position.values());
            model.addAttribute("statuses", EmploymentStatus.values());
            model.addAttribute("departments", departmentService.getAllDepartments());
            return "employees/add-form";
        }

        employeeService.addEmployee(employee);
        redirectAttributes.addFlashAttribute("message", "Pracownik dodany pomyślnie.");
        return "redirect:/employees";
    }

    @GetMapping("/edit/{email}")
    public String showEditForm(@PathVariable String email, Model model) {
        Employee employee = employeeService.getEmployeeByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono pracownika: " + email));
        model.addAttribute("employee", employee);
        model.addAttribute("positions", Position.values());
        model.addAttribute("statuses", EmploymentStatus.values());
        model.addAttribute("departments", departmentService.getAllDepartments());
        return "employees/edit-form";
    }

    @PostMapping("/edit")
    public String updateEmployee(@Valid @ModelAttribute("employee") Employee employee,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("positions", Position.values());
            model.addAttribute("statuses", EmploymentStatus.values());
            model.addAttribute("departments", departmentService.getAllDepartments());
            return "employees/edit-form";
        }

        employeeService.UpdateEmployeeByEmail(employee.getEmail(), employee);
        redirectAttributes.addFlashAttribute("message", "Dane pracownika zaktualizowane.");
        return "redirect:/employees";
    }

    @GetMapping("/delete/{email}")
    public String deleteEmployee(@PathVariable String email, RedirectAttributes redirectAttributes) {
        employeeService.deleteEmployeeByEmail(email);
        redirectAttributes.addFlashAttribute("message", "Pracownik usunięty.");
        return "redirect:/employees";
    }

    @GetMapping("/search")
    public String showSearchForm() {
        return "employees/search-form";
    }

    @PostMapping("/search")
    public String searchEmployees(@RequestParam("company") String company, Model model) {
        List<Employee> results = employeeService.getEmployeeByCompany(company);
        model.addAttribute("results", results);
        model.addAttribute("companyQuery", company);
        return "employees/search-results";
    }

    @GetMapping("/import")
    public String showImportForm() {
        return "employees/import-form";
    }

    @PostMapping("/import")
    public String handleImport(@RequestParam("file") MultipartFile file,
                               @RequestParam("fileType") String fileType,
                               RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Proszę wybrać plik do importu.");
            return "redirect:/employees/import";
        }

        try {
            ImportSummary summary;
            String savedPath;

            if ("csv".equals(fileType)) {
                storageService.validateFile(file, Set.of("csv"), 10L*1024*1024, Set.of("text/csv","application/vnd.ms-excel"));
                savedPath = storageService.storeInUploads(file, null);
                summary = importService.importFromCsv(savedPath);
            } else if ("xml".equals(fileType)) {
                storageService.validateFile(file, Set.of("xml"), 10L*1024*1024, Set.of("application/xml","text/xml"));
                savedPath = storageService.storeInUploads(file, null);
                summary = importService.importFromXml(savedPath);
            } else {
                throw new IllegalArgumentException("Nieznany typ pliku: " + fileType);
            }

            redirectAttributes.addFlashAttribute("message",
                    "Import zakończony. Zaimportowano: " + summary.getImportedCount() + ". Błędów: " + summary.getErrors().size());
            redirectAttributes.addFlashAttribute("errors", summary.getErrors());

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Błąd importu: " + e.getMessage());
            return "redirect:/employees/import";
        }

        return "redirect:/employees";
    }
}