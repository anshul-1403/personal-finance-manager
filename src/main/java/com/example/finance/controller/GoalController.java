package com.example.finance.controller;

import com.example.finance.dto.ApiResponse;
import com.example.finance.dto.GoalRequest;
import com.example.finance.dto.GoalResponse;
import com.example.finance.dto.GoalUpdateRequest;
import com.example.finance.service.GoalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @PostMapping
    public ResponseEntity<GoalResponse> createGoal(@Valid @RequestBody GoalRequest request) {
        GoalResponse response = goalService.createGoal(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Map<String, List<GoalResponse>>> getGoals() {
        List<GoalResponse> goals = goalService.getGoals();
        Map<String, List<GoalResponse>> payload = new HashMap<>();
        payload.put("goals", goals);
        return ResponseEntity.ok(payload);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoalResponse> getGoal(@PathVariable("id") Long id) {
        return ResponseEntity.ok(goalService.getGoal(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GoalResponse> updateGoal(@PathVariable("id") Long id, @Valid @RequestBody GoalUpdateRequest request) {
        return ResponseEntity.ok(goalService.updateGoal(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteGoal(@PathVariable("id") Long id) {
        goalService.deleteGoal(id);
        return ResponseEntity.ok(new ApiResponse("Goal deleted successfully"));
    }
}
