package com.carservice.validation.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CarValidationController.class)
public class CarValidationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void isCarNumberValid_HappyPath_ReturnsTrueForValidRegistrationFormats() throws Exception {
        // MH12AB1234 matches pattern
        mockMvc.perform(get("/carvalidation/validate")
                .param("carNumber", "  mh12ab1234  "))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    public void isCarNumberValid_FailurePath_ReturnsFalseForNullOrMalformedInputs() throws Exception {
        // Null test
        mockMvc.perform(get("/carvalidation/validate"))
                .andExpect(status().isBadRequest()); // missing parameter throws 400 bad request in standard MVC unless optional, but let's test null value direct parameter:
        
        mockMvc.perform(get("/carvalidation/validate")
                .param("carNumber", "INVALID_FORMAT"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }
}
