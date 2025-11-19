package com.example.zad1.controller;

import com.example.zad1.model.CompanyStatistics;
import com.example.zad1.service.DepartmentService;
import com.example.zad1.service.EmployeeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/statistics")
public class StatisticsViewController {

    private final EmployeeService employeeService;
    private final DepartmentService departmentService;

    public StatisticsViewController(EmployeeService employeeService, DepartmentService departmentService) {
        this.employeeService = employeeService;
        this.departmentService = departmentService;
    }

    @GetMapping
    public String showStatisticsDashboard(Model model) {
        model.addAttribute("totalEmployees", employeeService.getAllEmployees().size());
        model.addAttribute("averageSalary", employeeService.getAverageSalary());
        model.addAttribute("totalDepartments", departmentService.getAllDepartments().size());

        model.addAttribute("companyStats", employeeService.getCompanyStatistics().entrySet());

        model.addAttribute("positionCounts", employeeService.countByPosition());
        return "statistics/index";
    }

    @GetMapping("/company/{name}")
    public String showCompanyStatistics(@PathVariable String name, Model model) {
        CompanyStatistics stats = employeeService.getCompanyStatistics().get(name);
        model.addAttribute("companyName", name);
        model.addAttribute("stats", stats);
        model.addAttribute("employees", employeeService.getEmployeeByCompany(name));
        return "statistics/company";
    }
}