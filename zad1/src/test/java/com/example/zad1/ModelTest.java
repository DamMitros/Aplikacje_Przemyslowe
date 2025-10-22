package com.example.zad1;

import com.example.zad1.model.ImportSummary;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.example.zad1.model.Employee;
import com.example.zad1.model.Position;

import java.util.List;

public class ModelTest {

    Employee emp = new Employee("Justyna Steczkowska", "steczkowska1764@gmail.com", "TechCorp", Position.PREZES, Position.PREZES.getSalary());

    @Test
    void testGetFirstName() {
        assertEquals("Justyna", emp.getFirstName());
    }

    @Test
    void testGetLastName() {
        assertEquals("Steczkowska", emp.getLastName());
    }

    @Test
    void shouldReturnEmptyStringWhenFullNameNull() {
        Employee empNoLastName = new Employee(null, "steczka@outlook.ru", "TechCorp", Position.MANAGER, Position.MANAGER.getSalary());
        assertEquals("", empNoLastName.getLastName());
        assertEquals("", empNoLastName.getFirstName());
    }

    @Test
    void shouldReturnEmptyStringWhenFullNameBlank() {
        Employee empNoLastName = new Employee("   ", "steczka@outlook.ru", "TechCorp", Position.MANAGER, Position.MANAGER.getSalary());
        assertEquals("", empNoLastName.getLastName());
        assertEquals("", empNoLastName.getFirstName());
    }

    @Test
    void shouldReturnEmptyStringWhenOnlyFirstName() {
        Employee emp = new Employee("Justyna", "steczka@outlook.ru", "TechCorp", Position.MANAGER, 1000);
        assertEquals("", emp.getLastName());
        assertEquals("Justyna", emp.getFirstName());
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
    void ShouldFalseWhenEqualsNonEmployeeObject() {
        assertFalse(emp.equals("Some String"));
    }

    @Test
    void shouldCreateEmptyErrorWhenNullPassed(){
        ImportSummary summary = new ImportSummary(0, null);
        assertNotNull(summary.getErrors());
        assertTrue(summary.getErrors().isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> summary.getErrors().add("nie powinno działać"));
    }

    @Test
    void testImportSummaryToString() {
        List<String> emptyErrors = List.of();
        ImportSummary summary = new ImportSummary(5, emptyErrors);
        String expected = "ImportSummary{importedCount=5, errors=[]}";
        assertEquals(expected, summary.toString());
    }

    @Test
    void testGetHierarchy() {
        assertEquals(1, Position.PREZES.getHierarchy());
        assertEquals(2, Position.WICEPREZES.getHierarchy());
        assertEquals(3, Position.MANAGER.getHierarchy());
        assertEquals(4, Position.PROGRAMISTA.getHierarchy());
        assertEquals(5, Position.STAZYSTA.getHierarchy());
    }
}
