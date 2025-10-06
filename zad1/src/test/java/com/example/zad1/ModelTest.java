package com.example.zad1;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.example.zad1.model.Employee;
import com.example.zad1.model.Position;

public class ModelTest {

    Employee emp = new Employee("Justyna Steczkowska", "steczkowska1764@gmail.com", "TechCorp", Position.PREZES, Position.PREZES.getSalary());

    @Test
    void testGetFullName() {
        assertEquals("Justyna Steczkowska", emp.getFullName());
    }

    @Test
    void testGetFirstName() {
        assertEquals("Justyna", emp.getFirstName());
    }

    @Test
    void testGetLastName() {
        assertEquals("Steczkowska", emp.getLastName());
    }

    @Test
    void testGetEmail() {
        assertEquals("steczkowska1764@gmail.com", emp.getEmail());
    }

    @Test
    void testGetCompanyName() {
        assertEquals("TechCorp", emp.getCompanyName());
    }

    @Test
    void testGetPosition() {
        assertEquals(Position.PREZES, emp.getPosition());
    }

    @Test
    void testGetSalary() {
        assertEquals(25000, emp.getSalary());
    }

    @Test
    void testSetFullName() {
        emp.setFullName("Anna Nowak");
        assertEquals("Anna Nowak", emp.getFullName());
    }

    @Test
    void testSetEmail() {
        emp.setEmail("steczkowska1765@gmail.com");
        assertEquals("steczkowska1765@gmail.com", emp.getEmail());
    }

    @Test
    void testSetCompanyName() {
        emp.setCompanyName("NewTechCorp");
        assertEquals("NewTechCorp", emp.getCompanyName());
    }

    @Test
    void testSetPosition() {
        emp.setPosition(Position.WICEPREZES);
        assertEquals(Position.WICEPREZES, emp.getPosition());
    }

    @Test
    void testSetSalary() {
        emp.setSalary(30000);
        assertEquals(30000, emp.getSalary());
    }

    @Test
    void testGetHierarchy() {
        assertEquals(1, Position.PREZES.getHierarchy());
        assertEquals(2, Position.WICEPREZES.getHierarchy());
        assertEquals(3, Position.MANAGER.getHierarchy());
        assertEquals(4, Position.PROGRAMISTA.getHierarchy());
        assertEquals(5, Position.STAZYSTA.getHierarchy());
    }

    @Test
    void testGetSalaryPosition() {
        assertEquals(25000, Position.PREZES.getSalary());
        assertEquals(18000, Position.WICEPREZES.getSalary());
        assertEquals(12000, Position.MANAGER.getSalary());
        assertEquals(8000, Position.PROGRAMISTA.getSalary());
        assertEquals(3000, Position.STAZYSTA.getSalary());
    }
}
