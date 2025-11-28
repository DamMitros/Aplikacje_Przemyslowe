package com.example.zad1.specification;

import com.example.zad1.model.Employee;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class EmployeeSpecification {

    public static Specification<Employee> filterBy(String companyName, Integer minSalary, Integer maxSalary) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (companyName != null && !companyName.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("companyName")), "%" + companyName.toLowerCase() + "%"));
            }

            if (minSalary != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("salary"), minSalary));
            }

            if (maxSalary != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("salary"), maxSalary));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}