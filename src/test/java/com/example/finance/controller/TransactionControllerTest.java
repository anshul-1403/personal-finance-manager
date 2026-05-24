package com.example.finance.controller;

import com.example.finance.dto.TransactionRequest;
import com.example.finance.dto.TransactionResponse;
import com.example.finance.dto.TransactionUpdateRequest;
import com.example.finance.service.TransactionService;
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
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void createTransaction_success() throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(BigDecimal.valueOf(100));
        request.setDate("2024-01-01");
        request.setCategory("Food");
        request.setDescription("Lunch");

        TransactionResponse response = new TransactionResponse(1L, BigDecimal.valueOf(100), "2024-01-01", "Food", "Lunch", "EXPENSE");
        when(transactionService.createTransaction(any(TransactionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(100));
    }

    @Test
    @WithMockUser
    void getTransactions_success() throws Exception {
        TransactionResponse response = new TransactionResponse(1L, BigDecimal.valueOf(100), "2024-01-01", "Food", "Lunch", "EXPENSE");
        when(transactionService.getTransactions(any(), any(), any())).thenReturn(Collections.singletonList(response));

        mockMvc.perform(get("/api/transactions")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-02")
                        .param("category", "Food"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions[0].amount").value(100));
    }

    @Test
    @WithMockUser
    void updateTransaction_success() throws Exception {
        TransactionUpdateRequest request = new TransactionUpdateRequest();
        request.setAmount(BigDecimal.valueOf(120));

        TransactionResponse response = new TransactionResponse(1L, BigDecimal.valueOf(120), "2024-01-01", "Food", "Lunch", "EXPENSE");
        when(transactionService.updateTransaction(eq(1L), any(TransactionUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/transactions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(120));
    }

    @Test
    @WithMockUser
    void deleteTransaction_success() throws Exception {
        doNothing().when(transactionService).deleteTransaction(1L);

        mockMvc.perform(delete("/api/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Transaction deleted successfully"));
    }
}
