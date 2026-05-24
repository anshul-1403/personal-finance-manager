package com.example.finance.repository;

import com.example.finance.entity.CategoryEntity;
import com.example.finance.entity.TransactionEntity;
import com.example.finance.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
    List<TransactionEntity> findByUserOrderByDateDesc(UserEntity user);
    List<TransactionEntity> findByUserAndDateBetweenOrderByDateDesc(UserEntity user, LocalDate startDate, LocalDate endDate);
    List<TransactionEntity> findByUserAndCategoryOrderByDateDesc(UserEntity user, CategoryEntity category);
    List<TransactionEntity> findByUserAndCategoryAndDateBetweenOrderByDateDesc(UserEntity user, CategoryEntity category, LocalDate startDate, LocalDate endDate);
    boolean existsByCategory(CategoryEntity category);
    List<TransactionEntity> findByUserAndDateGreaterThanEqual(UserEntity user, LocalDate startDate);
    List<TransactionEntity> findByUserAndDateBetween(UserEntity user, LocalDate startDate, LocalDate endDate);
}
