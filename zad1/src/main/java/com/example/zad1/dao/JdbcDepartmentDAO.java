package com.example.zad1.dao;

import com.example.zad1.model.Department;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcDepartmentDAO implements DepartmentDAO {

    private final JdbcTemplate jdbcTemplate;

    public JdbcDepartmentDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Department> departmentRowMapper = (rs, rowNum) -> {
        Department d = new Department();
        d.setId(rs.getLong("id"));
        d.setName(rs.getString("name"));
        d.setCompanyName(rs.getString("company_name"));
        d.setLocation(rs.getString("location"));
        d.setBudget(rs.getDouble("budget"));
        d.setManagerEmail(rs.getString("manager_email"));
        return d;
    };

    @Override
    public List<Department> findAll() {
        return jdbcTemplate.query("SELECT * FROM departments", departmentRowMapper);
    }

    @Override
    public Optional<Department> findById(Long id) {
        try {
            Department dept = jdbcTemplate.queryForObject("SELECT * FROM departments WHERE id = ?", departmentRowMapper, id);
            return Optional.ofNullable(dept);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Department save(Department department) {
        if (department.getId() != null && findById(department.getId()).isPresent()) {
            String sql = "UPDATE departments SET name=?, company_name=?, location=?, budget=?, manager_email=? WHERE id=?";
            jdbcTemplate.update(sql,
                    department.getName(),
                    department.getCompanyName(),
                    department.getLocation(),
                    department.getBudget(),
                    department.getManagerEmail(),
                    department.getId());
            return department;
        } else {
            String sql = "INSERT INTO departments (name, company_name, location, budget, manager_email) VALUES (?, ?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, department.getName());
                ps.setString(2, department.getCompanyName());
                ps.setString(3, department.getLocation());
                ps.setDouble(4, department.getBudget());
                ps.setString(5, department.getManagerEmail());
                return ps;
            }, keyHolder);

            Number key = keyHolder.getKey();
            if (key != null) {
                department.setId(key.longValue());
            }
            return department;
        }
    }

    @Override
    public boolean delete(Long id) {
        return jdbcTemplate.update("DELETE FROM departments WHERE id = ?", id) > 0;
    }
}