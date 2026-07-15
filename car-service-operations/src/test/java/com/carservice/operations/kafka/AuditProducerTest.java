package com.carservice.operations.kafka;

import com.carservice.operations.model.CarServiceAuditEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.fail;

public class AuditProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private AuditProducer auditProducer;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void sendAuditEvent_HappyPath_SuccessfullySendsMessageToKafkaTopic() {
        auditProducer.sendAuditEvent("CREATE_RECORD", "MH12AB1234", "SUCCESS", 1L, "admin", "details");

        verify(kafkaTemplate, times(1)).send(eq("car-service-audit-events"), any(CarServiceAuditEvent.class));
    }

    @Test
    public void sendAuditEvent_FailurePath_CatchesExceptionAndLogsToSystemErr() {
        when(kafkaTemplate.send(anyString(), any())).thenThrow(new RuntimeException("Kafka down"));

        // Should catch exception and not propagate
        try {
            auditProducer.sendAuditEvent("CREATE_RECORD", "MH12AB1234", "SUCCESS", 1L, "admin", "details");
        } catch (Exception e) {
            fail("Exception should have been caught in sendAuditEvent method");
        }
    }
}
