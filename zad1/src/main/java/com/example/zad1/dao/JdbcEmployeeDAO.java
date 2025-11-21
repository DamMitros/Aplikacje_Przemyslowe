package com.example.zad1.dao;

import com.example.zad1.model.CompanyStatistics;
import com.example.zad1.model.Employee;
import com.example.zad1.model.EmploymentStatus;
import com.example.zad1.model.Position;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JdbcEmployeeDAO implements EmployeeDAO {

    private final JdbcTemplate jdbcTemplate;

    public JdbcEmployeeDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Employee> employeeRowMapper = (rs, rowNum) -> {
        Employee e = new Employee();
        e.setId(rs.getLong("id"));
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");
        e.setFullName((firstName + " " + lastName).trim());
        e.setEmail(rs.getString("email"));
        e.setCompanyName(rs.getString("company_name"));

        String posStr = rs.getString("position");
        if (posStr != null) e.setPosition(Position.valueOf(posStr));

        e.setSalary(rs.getInt("salary"));

        String statStr = rs.getString("status");
        if (statStr != null) e.setStatus(EmploymentStatus.valueOf(statStr));

        Long deptId = rs.getObject("department_id", Long.class);
        e.setDepartmentId(deptId);
        e.setPhotoFileName(rs.getString("photo_file_name"));

        return e;
    };

    @Override
    public List<Employee> findAll() {
        String sql = "SELECT * FROM employees";
        return jdbcTemplate.query(sql, employeeRowMapper);
    }

    @Override
    public Optional<Employee> findByEmail(String email) {
        String sql = "SELECT * FROM employees WHERE email = ?";
        try {
            Employee emp = jdbcTemplate.queryForObject(sql, employeeRowMapper, email);
            return Optional.ofNullable(emp);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void save(Employee employee) {
        Optional<Employee> existing = findByEmail(employee.getEmail());

        if (existing.isPresent()) {
            String sql = "UPDATE employees SET first_name=?, last_name=?, company_name=?, position=?, salary=?, status=?, department_id=?, photo_file_name=? WHERE email=?";
            jdbcTemplate.update(sql,
                    employee.getFirstName(),
                    employee.getLastName(),
                    employee.getCompanyName(),
                    employee.getPosition() != null ? employee.getPosition().name() : null,
                    employee.getSalary(),
                    employee.getStatus() != null ? employee.getStatus().name() : null,
                    employee.getDepartmentId(),
                    employee.getPhotoFileName(),
                    employee.getEmail()
            );
        } else {
            String sql = "INSERT INTO employees (first_name, last_name, email, company_name, position, salary, status, department_id, photo_file_name) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql,
                    employee.getFirstName(),
                    employee.getLastName(),
                    employee.getEmail(),
                    employee.getCompanyName(),
                    employee.getPosition() != null ? employee.getPosition().name() : null,
                    employee.getSalary(),
                    employee.getStatus() != null ? employee.getStatus().name() : null,
                    employee.getDepartmentId(),
                    employee.getPhotoFileName()
            );
        }
    }

    @Override
    public boolean delete(String email) {
        String sql = "DELETE FROM employees WHERE email = ?";
        return jdbcTemplate.update(sql, email) > 0;
    }

    @Override
    public void deleteAll() {
        jdbcTemplate.update("DELETE FROM employees");
    }

    @Override
    public List<CompanyStatistics> getCompanyStatistics() {
        String sql = "SELECT company_name, COUNT(*) as emp_count, AVG(salary) as avg_salary, MAX(salary) as max_salary " +
                "FROM employees " +
                "WHERE company_name IS NOT NULL AND company_name != '' " +
                "GROUP BY company_name";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new CompanyStatistics(
                rs.getString("company_name"),
                rs.getInt("emp_count"),
                rs.getDouble("avg_salary"),
                "Zobacz listę",
                rs.getInt("max_salary")
        ));
    }
}