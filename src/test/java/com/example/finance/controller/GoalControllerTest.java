package com.example.finance.controller;

import com.example.finance.dto.GoalRequest;
import com.example.finance.dto.GoalResponse;
import com.example.finance.dto.GoalUpdateRequest;
import com.example.finance.service.GoalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GoalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GoalService goalService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void createGoal_success() throws Exception {
        GoalRequest request = new GoalRequest();
        request.setGoalName("Emergency Fund");
        request.setTargetAmount(BigDecimal.valueOf(10000));
        request.setTargetDate("2025-12-31");

        GoalResponse response = new GoalResponse(1L, "Emergency Fund", BigDecimal.valueOf(10000), "2025-12-31", "2024-01-01", BigDecimal.ZERO, 0.0, BigDecimal.valueOf(10000));
        when(goalService.createGoal(any(GoalRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.goalName").value("Emergency Fund"));
    }

    @Test
    @WithMockUser
    void getGoals_success() throws Exception {
        GoalResponse response = new GoalResponse(1L, "Emergency Fund", BigDecimal.valueOf(10000), "2025-12-31", "2024-01-01", BigDecimal.ZERO, 0.0, BigDecimal.valueOf(10000));
        when(goalService.getGoals()).thenReturn(Collections.singletonList(response));

        mockMvc.perform(get("/api/goals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goals[0].goalName").value("Emergency Fund"));
    }

    @Test
    @WithMockUser
    void getGoal_success() throws Exception {
        GoalResponse response = new GoalResponse(1L, "Emergency Fund", BigDecimal.valueOf(10000), "2025-12-31", "2024-01-01", BigDecimal.ZERO, 0.0, BigDecimal.valueOf(10000));
        when(goalService.getGoal(1L)).thenReturn(response);

        mockMvc.perform(get("/api/goals/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goalName").value("Emergency Fund"));
    }

    @Test
    @WithMockUser
    void updateGoal_success() throws Exception {
        GoalUpdateRequest request = new GoalUpdateRequest();
        request.setTargetAmount(BigDecimal.valueOf(15000));

        GoalResponse response = new GoalResponse(1L, "Emergency Fund", BigDecimal.valueOf(15000), "2025-12-31", "2024-01-01", BigDecimal.ZERO, 0.0, BigDecimal.valueOf(15000));
        when(goalService.updateGoal(eq(1L), any(GoalUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/goals/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetAmount").value(15000));
    }

    @Test
    @WithMockUser
    void deleteGoal_success() throws Exception {
        doNothing().when(goalService).deleteGoal(1L);

        mockMvc.perform(delete("/api/goals/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Goal deleted successfully"));
    }
}
