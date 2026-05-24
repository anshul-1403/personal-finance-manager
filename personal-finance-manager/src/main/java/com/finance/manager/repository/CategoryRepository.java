package com.finance.manager.repository;

import com.finance.manager.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUserIdOrIsCustomFalse(Long userId);
    Optional<Category> findByNameAndUserId(String name, Long userId);
    Optional<Category> findByNameAndIsCustomFalse(String name);
    boolean existsByNameAndUserId(String name, Long userId);
}
