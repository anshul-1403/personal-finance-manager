package com.example.finance.repository;

import com.example.finance.entity.CategoryEntity;
import com.example.finance.entity.CategoryType;
import com.example.finance.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    List<CategoryEntity> findByUser(UserEntity user);
    Optional<CategoryEntity> findByNameAndUser(String name, UserEntity user);
    Optional<CategoryEntity> findByNameAndTypeAndUserIsNull(String name, CategoryType type);
    Optional<CategoryEntity> findByNameAndUserIsNull(String name);
    Optional<CategoryEntity> findByNameAndTypeAndUser(String name, CategoryType type, UserEntity user);
    boolean existsByNameAndUser(String name, UserEntity user);
}
