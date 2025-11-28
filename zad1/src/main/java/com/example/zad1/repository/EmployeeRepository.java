package com.example.zad1.repository;

import com.example.zad1.dto.EmployeeListView;
import com.example.zad1.model.CompanyStatistics;
import com.example.zad1.model.Employee;
import com.example.zad1.model.EmploymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    @Query("SELECT e FROM Employee e")
    Page<EmployeeListView> findAllProjectedBy(Pageable pageable);

    boolean existsByEmail(String email);
    Optional<Employee> findByEmail(String email);
    void deleteByEmail(String email);

    @Query("SELECT new com.example.zad1.model.CompanyStatistics(" +
            "e.companyName, " +
            "COUNT(e), " +
            "AVG(e.salary), " +
            "MAX(e.salary)) " +
            "FROM Employee e " +
            "WHERE e.companyName IS NOT NULL AND e.companyName != '' " +
            "GROUP BY e.companyName")
    List<CompanyStatistics> getCompanyStatisticsJPQL();

    @Query("SELECT e.position, COUNT(e) FROM Employee e WHERE e.position IS NOT NULL GROUP BY e.position")
    List<Object[]> countByPositionJPQL();

    @Query("SELECT e.status, COUNT(e) FROM Employee e WHERE e.status IS NOT NULL GROUP BY e.status")
    List<Object[]> countByStatusJPQL();

    @Query("SELECT COALESCE(AVG(e.salary), 0.0) FROM Employee e")
    Double getAverageSalaryJPQL();

    @Query("SELECT COALESCE(AVG(e.salary), 0.0) FROM Employee e WHERE e.companyName = :companyName")
    Double getAverageSalaryByCompanyJPQL(String companyName);

    List<Employee> findByStatus(EmploymentStatus status);
}