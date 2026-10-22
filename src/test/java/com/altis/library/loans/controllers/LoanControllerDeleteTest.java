package com.altis.library.loans.controllers;

import com.altis.library.loans.services.LoanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LoanControllerDeleteTest {

    private MockMvc mockMvc;
    private LoanService loanService;

    @BeforeEach
    void setUp() {
        loanService = mock(LoanService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new LoanController(loanService))
                .build();
    }

    @Test
    void deleteLoanIsNotAllowed() throws Exception {
        mockMvc.perform(delete("/loans/{id}", 1L))
                .andExpect(status().isMethodNotAllowed());

        verifyNoInteractions(loanService);
    }
}
