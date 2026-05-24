package com.finance.manager.controller;

import com.finance.manager.dto.GoalRequest;
import com.finance.manager.dto.GoalResponse;
import com.finance.manager.dto.GoalUpdateRequest;
import com.finance.manager.security.CustomUserDetails;
import com.finance.manager.service.GoalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @PostMapping
    public ResponseEntity<GoalResponse> createGoal(
            @Valid @RequestBody GoalRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return new ResponseEntity<>(goalService.createGoal(request, userDetails.getUser()), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<?> getGoals(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(Map.of("goals", goalService.getAllGoals(userDetails.getUser())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoalResponse> getGoal(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(goalService.getGoal(id, userDetails.getUser()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GoalResponse> updateGoal(
            @PathVariable Long id,
            @Valid @RequestBody GoalUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(goalService.updateGoal(id, request, userDetails.getUser()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGoal(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        goalService.deleteGoal(id, userDetails.getUser());
        return ResponseEntity.ok(Map.of("message", "Goal deleted successfully"));
    }
}
