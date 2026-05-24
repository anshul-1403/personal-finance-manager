package com.example.finance.controller;

import com.example.finance.dto.ReportMonthlyResponse;
import com.example.finance.dto.ReportYearlyResponse;
import com.example.finance.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @Test
    @WithMockUser
    void getMonthlyReport_success() throws Exception {
        ReportMonthlyResponse response = new ReportMonthlyResponse(1, 2024, Collections.emptyMap(), Collections.emptyMap(), BigDecimal.ZERO);
        when(reportService.getMonthlyReport(2024, 1)).thenReturn(response);

        mockMvc.perform(get("/api/reports/monthly/2024/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value(1))
                .andExpect(jsonPath("$.year").value(2024));
    }

    @Test
    @WithMockUser
    void getYearlyReport_success() throws Exception {
        ReportYearlyResponse response = new ReportYearlyResponse(2024, Collections.emptyMap(), Collections.emptyMap(), BigDecimal.ZERO);
        when(reportService.getYearlyReport(2024)).thenReturn(response);

        mockMvc.perform(get("/api/reports/yearly/2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2024));
    }
}
