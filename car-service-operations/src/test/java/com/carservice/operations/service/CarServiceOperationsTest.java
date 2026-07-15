package com.carservice.operations.service;

import com.carservice.operations.client.CarValidationClient;
import com.carservice.operations.client.UserProfileClient;
import com.carservice.operations.entity.CarService;
import com.carservice.operations.entity.ServiceCategory;
import com.carservice.operations.kafka.AuditProducer;
import com.carservice.operations.repository.CarServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CarServiceOperationsTest {

    @Mock
    private CarServiceRepository carRepository;

    @Mock
    private CarValidationClient carValidationClient;

    @Mock
    private UserProfileClient userProfileClient;

    @Mock
    private AuditProducer auditProducer;

    @InjectMocks
    private CarServiceOperations serviceOperations;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void createRecord_HappyPath_SavesRecordAndSendsSuccessAudit() {
        CarService service = new CarService();
        service.setCarRegistrationNumber("MH12AB1234");
        service.setCustomerId(101L);
        service.setServiceType(ServiceCategory.OIL_CHANGE);
        service.setServiceDate(LocalDate.now());

        // Mock customer validation
        when(userProfileClient.getProfileById(101L)).thenReturn(new HashMap<>());
        // Mock car format validation
        when(carValidationClient.isCarNumberValid("MH12AB1234")).thenReturn(true);
        // Mock active service checking - return empty list
        when(carRepository.findAllByCarRegistrationNumber("MH12AB1234")).thenReturn(Collections.emptyList());
        
        CarService saved = new CarService();
        saved.setId(1L);
        saved.setCarRegistrationNumber("MH12AB1234");
        saved.setCustomerId(101L);
        when(carRepository.save(any(CarService.class))).thenReturn(saved);

        CarService result = serviceOperations.createRecord(service, "admin");

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(auditProducer, times(1)).sendAuditEvent(eq("CREATE_RECORD"), eq("MH12AB1234"), eq("SUCCESS"), eq(1L), eq("admin"), anyString());
    }

    @Test
    public void createRecord_FailurePath_ThrowsExceptionWhenFormatInvalid() {
        CarService service = new CarService();
        service.setCarRegistrationNumber("INVALID");
        service.setCustomerId(101L);

        when(userProfileClient.getProfileById(101L)).thenReturn(new HashMap<>());
        when(carValidationClient.isCarNumberValid("INVALID")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> {
            serviceOperations.createRecord(service, "admin");
        });

        verify(auditProducer, times(1)).sendAuditEvent(eq("CREATE_FAILED_INVALID_FORMAT"), eq("INVALID"), eq("REJECTED"), eq(null), eq("admin"), anyString());
        verify(carRepository, never()).save(any());
    }

    @Test
    public void createRecord_FailurePath_ThrowsExceptionWhenCustomerNotFound() {
        CarService service = new CarService();
        service.setCarRegistrationNumber("MH12AB1234");
        service.setCustomerId(999L);

        // Mock client throws exception when customer doesn't exist
        when(userProfileClient.getProfileById(999L)).thenThrow(new RuntimeException("Not found"));

        assertThrows(RuntimeException.class, () -> {
            serviceOperations.createRecord(service, "admin");
        });

        verify(auditProducer, times(1)).sendAuditEvent(eq("CREATE_FAILED_CUSTOMER_NOT_FOUND"), eq("MH12AB1234"), eq("REJECTED"), eq(null), eq("admin"), anyString());
        verify(carRepository, never()).save(any());
    }

    @Test
    public void createRecord_FailurePath_ThrowsExceptionWhenActiveServiceExists() {
        CarService service = new CarService();
        service.setCarRegistrationNumber("MH12AB1234");
        service.setCustomerId(101L);

        when(userProfileClient.getProfileById(101L)).thenReturn(new HashMap<>());
        when(carValidationClient.isCarNumberValid("MH12AB1234")).thenReturn(true);

        CarService activeRecord = new CarService();
        activeRecord.setServiceStatus("IN_PROGRESS");
        when(carRepository.findAllByCarRegistrationNumber("MH12AB1234")).thenReturn(List.of(activeRecord));

        assertThrows(RuntimeException.class, () -> {
            serviceOperations.createRecord(service, "admin");
        });

        verify(auditProducer, times(1)).sendAuditEvent(eq("CREATE_FAILED_ACTIVE_SERVICE_EXISTS"), eq("MH12AB1234"), eq("REJECTED"), eq(null), eq("admin"), anyString());
        verify(carRepository, never()).save(any());
    }

    @Test
    public void updateServiceStatus_HappyPath_UpdatesStatusAndSendsAudit() {
        CarService record = new CarService();
        record.setId(1L);
        record.setCarRegistrationNumber("MH12AB1234");
        record.setServiceStatus("PENDING");

        when(carRepository.findById(1L)).thenReturn(Optional.of(record));
        when(carRepository.save(any(CarService.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CarService result = serviceOperations.updateServiceStatus(1L, "COMPLETED", "admin");

        assertEquals("COMPLETED", result.getServiceStatus());
        verify(auditProducer, times(1)).sendAuditEvent(eq("UPDATE_STATUS_COMPLETED"), eq("MH12AB1234"), eq("UPDATED"), eq(1L), eq("admin"), anyString());
    }

    @Test
    public void updateServiceStatus_FailurePath_ThrowsExceptionWhenIdNotFound() {
        when(carRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            serviceOperations.updateServiceStatus(99L, "COMPLETED", "admin");
        });

        verify(auditProducer, never()).sendAuditEvent(any(), any(), any(), any(), any(), any());
    }

    @Test
    public void getStatusByCarNumber_HappyPath_ReturnsStatusString() {
        CarService service = new CarService();
        service.setServiceStatus("IN_PROGRESS");
        when(carRepository.findByCarRegistrationNumber("MH12AB1234")).thenReturn(Optional.of(service));

        String status = serviceOperations.getStatusByCarNumber("MH12AB1234");
        assertEquals("IN_PROGRESS", status);
    }

    @Test
    public void getStatusByCarNumber_FailurePath_ThrowsExceptionWhenNotFound() {
        when(carRepository.findByCarRegistrationNumber("NOTFOUND")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            serviceOperations.getStatusByCarNumber("NOTFOUND");
        });
    }

    @Test
    public void getRecordById_HappyPath_ReturnsMatchingRecord() {
        CarService service = new CarService();
        service.setId(1L);
        when(carRepository.findById(1L)).thenReturn(Optional.of(service));

        CarService result = serviceOperations.getRecordById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    public void getRecordById_FailurePath_ThrowsExceptionWhenMissing() {
        when(carRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            serviceOperations.getRecordById(99L);
        });
    }

    @Test
    public void deleteRecord_HappyPath_DeletesFromDatabaseAndAudits() {
        CarService service = new CarService();
        service.setId(1L);
        service.setCarRegistrationNumber("MH12AB1234");

        when(carRepository.findById(1L)).thenReturn(Optional.of(service));

        serviceOperations.deleteRecord(1L, "admin");

        verify(carRepository, times(1)).deleteById(1L);
        verify(auditProducer, times(1)).sendAuditEvent(eq("DELETE_RECORD"), eq("MH12AB1234"), eq("DELETED"), eq(1L), eq("admin"), anyString());
    }

    @Test
    public void deleteRecord_FailurePath_ThrowsExceptionWhenNotFound() {
        when(carRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            serviceOperations.deleteRecord(99L, "admin");
        });

        verify(carRepository, never()).deleteById(anyLong());
        verify(auditProducer, never()).sendAuditEvent(any(), any(), any(), any(), any(), any());
    }
}
