package com.carservice.auditservice.kafka;

import com.carservice.auditservice.entity.AuditLog;
import com.carservice.auditservice.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AuditConsumerTest {

    @Mock
    private AuditLogRepository auditRepository;

    @InjectMocks
    private AuditConsumer auditConsumer;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void consumeAuditEvent_HappyPath_ExtractsPayloadAndSavesToRepository() {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("action", "CREATE_RECORD");
        eventData.put("carRegistrationNumber", "MH12AB1234");
        eventData.put("status", "SUCCESS");
        eventData.put("carServiceId", 123);
        eventData.put("performedBy", "admin");
        eventData.put("details", "Created file successfully.");

        auditConsumer.consumeAuditEvent(eventData);

        verify(auditRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    public void consumeAuditEvent_FailurePath_SwallowsExceptionToAvoidInfiniteLoop() {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("action", "CREATE_RECORD");

        // Force repository to throw exception on save
        when(auditRepository.save(any(AuditLog.class))).thenThrow(new RuntimeException("Database error"));

        try {
            auditConsumer.consumeAuditEvent(eventData);
        } catch (Exception e) {
            fail("Exception should have been caught in consumeAuditEvent to avoid infinite consumer loops");
        }

        // Verify save was attempted
        verify(auditRepository, times(1)).save(any(AuditLog.class));
    }
}
