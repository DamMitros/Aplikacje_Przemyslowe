package com.example.zad1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.zad1.model.Employee;
import com.example.zad1.model.Position;
import com.example.zad1.service.EmployeeService;

import java.util.Optional;

@SpringBootApplication
public class Zad1Application implements CommandLineRunner{

    public static void main(String[] args) {
        SpringApplication.run(Zad1Application.class, args);
    }

    @Override
    public void run(String... args) {
        EmployeeService service = new EmployeeService();

        System.out.println("Pracownik z najwyższym wynagrodzeniem (bez pracowników):");
        Optional<Employee> topEmployee = service.getHighestSalary();
        System.out.println(topEmployee.map(Employee::toString).orElse("Brak pracowników."));

        service.addEmployee(new Employee("Justyna Steczkowska", "steczkowska1764@gmail.com", "TechCorp", Position.PREZES, Position.PREZES.getSalary()));
        service.addEmployee(new Employee("Edyta Gorniak", "edyth.gorniak@gmail.com", "TechCorp", Position.WICEPREZES, Position.WICEPREZES.getSalary()));
        service.addEmployee(new Employee("Doda Rabczewska", "doda@gmail.com", "TechCorp", Position.MANAGER, Position.MANAGER.getSalary()));
        service.addEmployee(new Employee("Kasia Kowalska", "kasia.kowal@gmail.com", "ItTomans", Position.PROGRAMISTA, Position.PROGRAMISTA.getSalary()));
        service.addEmployee(new Employee("Marta Zalewska", "MartaZlewka@gmail.com", "TechCorp", Position.PROGRAMISTA, Position.PROGRAMISTA.getSalary()));
        service.addEmployee(new Employee("Anna Nowak", "nowaAnia@gmail.com", "ItTomans", Position.STAZYSTA, Position.STAZYSTA.getSalary()));

        System.out.println("Próba dodania pracownika z duplikatem email:");
        boolean added = service.addEmployee(new Employee("Anna Nowak", "nowaAnia@gmail.com", "TechCorp", Position.WICEPREZES, Position.WICEPREZES.getSalary()));
        if (!added) {
            System.out.println("Nie można dodać pracownika z duplikatem email.");
        } else {
            System.out.println("Pracownik dodany mimo duplikatu.");
        }

        System.out.println("\nWszyscy pracownicy:");
        service.displayAll();

        System.out.println("\nPracownicy z firmy TechCorp:");
        service.getEmployeeByCompany("TechCorp").forEach(System.out::println);

        System.out.println("\nPracownicy posortowani alfabetycznie:");
        service.getAlphabetically().forEach(System.out::println);

        System.out.println("\nPracownicy pogrupowani według stanowisk:");
        service.groupByPosition().forEach((position, employees) -> {
            System.out.println(position + ":");
            employees.forEach(emp -> System.out.println("  " + emp));
        });

        System.out.println("\nLiczba pracowników na każdym stanowisku:");
        service.countByPosition().forEach((position, count) ->
                System.out.println(position + ": " + count));

        System.out.println("\nŚrednie wynagrodzenie wszystkich pracowników:");
        System.out.printf("%.2f%n", service.getAverageSalary());

        System.out.println("\nPracownik z najwyższym wynagrodzeniem:");
        topEmployee = service.getHighestSalary();
        System.out.println(topEmployee.map(Employee::toString).orElse("Brak pracowników ."));
    }
}