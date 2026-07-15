package com.carservice.operations.controller;

import com.carservice.operations.entity.CarService;
import com.carservice.operations.entity.ServiceCategory;
import com.carservice.operations.model.CarServiceRequestDTO;
import com.carservice.operations.service.CarServiceOperations;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CarServiceController.class)
public class CarServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CarServiceOperations operationsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void registerService_HappyPath_ConvertsDtoAndReturnsCreatedStatus() throws Exception {
        CarServiceRequestDTO dto = new CarServiceRequestDTO("MH12AB1234", 101L, ServiceCategory.OIL_CHANGE, LocalDate.now(), "need service");
        CarService saved = new CarService();
        saved.setId(1L);
        saved.setCarRegistrationNumber("MH12AB1234");
        saved.setServiceStatus("PENDING");

        when(operationsService.createRecord(any(CarService.class), eq("admin"))).thenReturn(saved);

        mockMvc.perform(post("/save")
                .header("X-Authenticated-User", "admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.carRegistrationNumber").value("MH12AB1234"));
    }

    @Test
    public void registerService_AlternativePath_UsesDefaultAnonymousHeaderWhenMissing() throws Exception {
        CarServiceRequestDTO dto = new CarServiceRequestDTO("MH12AB1234", 101L, ServiceCategory.OIL_CHANGE, LocalDate.now(), "need service");
        CarService saved = new CarService();
        saved.setId(1L);
        saved.setCarRegistrationNumber("MH12AB1234");

        // Should default to "anonymous" header when none is provided
        when(operationsService.createRecord(any(CarService.class), eq("anonymous"))).thenReturn(saved);

        mockMvc.perform(post("/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    public void updateServiceStatus_HappyPath_UpdatesStatusValueCaseInsensitively() throws Exception {
        CarService existing = new CarService();
        existing.setId(1L);
        existing.setServiceStatus("PENDING");

        CarService saved = new CarService();
        saved.setId(1L);
        saved.setServiceStatus("COMPLETED");

        when(operationsService.getRecordById(1L)).thenReturn(existing);
        when(operationsService.updateServiceStatus(1L, "COMPLETED", "admin")).thenReturn(saved);

        mockMvc.perform(put("/1/status")
                .header("X-Authenticated-Role", "admin")
                .header("X-Authenticated-User", "admin")
                .param("status", "  coMpleTed  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serviceStatus").value("COMPLETED"));
    }

    @Test
    public void updateServiceStatus_FailurePath_ReturnsBadRequestForInvalidEnumStrings() throws Exception {
        mockMvc.perform(put("/1/status")
                .header("X-Authenticated-User", "admin")
                .param("status", "INVALID_STATUS"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Allowed values are")));
    }

    @Test
    public void getServiceById_HappyPath_ReturnsOkStatusWithMatchingRecord() throws Exception {
        CarService saved = new CarService();
        saved.setId(1L);
        saved.setCarRegistrationNumber("MH12AB1234");

        when(operationsService.getRecordById(1L)).thenReturn(saved);

        mockMvc.perform(get("/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.carRegistrationNumber").value("MH12AB1234"));
    }

    @Test
    public void getServiceById_FailurePath_PropagatesExceptionWhenIdNotFound() throws Exception {
        when(operationsService.getRecordById(99L)).thenThrow(new RuntimeException("Record not found"));

        mockMvc.perform(get("/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Record not found"));
    }

    @Test
    public void deleteService_HappyPath_RemovesRecordAndReturnsConfirmationMap() throws Exception {
        mockMvc.perform(delete("/1")
                .header("X-Authenticated-User", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.deletedBy").value("admin"));
    }

    @Test
    public void deleteService_FailurePath_PropagatesExceptionWhenRecordNotFound() throws Exception {
        doThrow(new RuntimeException("Record not found")).when(operationsService).deleteRecord(99L, "admin");

        mockMvc.perform(delete("/99")
                .header("X-Authenticated-User", "admin"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Record not found"));
    }

    @Test
    public void getAllServices_AdminRole_ReturnsAllRecords() throws Exception {
        CarService service1 = new CarService();
        service1.setId(1L);
        service1.setCarRegistrationNumber("MH12AB1234");

        when(operationsService.getAllRecords()).thenReturn(java.util.List.of(service1));

        mockMvc.perform(get("/")
                .header("X-Authenticated-Role", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].carRegistrationNumber").value("MH12AB1234"));
    }

    @Test
    public void getAllServices_CustomerRole_ReturnsFilteredRecords() throws Exception {
        CarService service1 = new CarService();
        service1.setId(1L);
        service1.setCarRegistrationNumber("MH12AB1234");
        service1.setCustomerId(101L);

        when(operationsService.getRecordsByCustomerId(101L)).thenReturn(java.util.List.of(service1));

        mockMvc.perform(get("/")
                .header("X-Authenticated-Role", "customer")
                .header("X-Authenticated-Id", "101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].carRegistrationNumber").value("MH12AB1234"))
                .andExpect(jsonPath("$[0].customerId").value(101));
    }
}
