package com.example.finance.repository;

import com.example.finance.entity.GoalEntity;
import com.example.finance.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalRepository extends JpaRepository<GoalEntity, Long> {
    List<GoalEntity> findByUser(UserEntity user);
    Optional<GoalEntity> findByIdAndUser(Long id, UserEntity user);
}
