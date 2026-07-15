package com.carservice.operations.kafka;

import com.carservice.operations.model.CarServiceAuditEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class AuditProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String AUDIT_TOPIC = "car-service-audit-events";

    public AuditProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // Makes list to collect audit metadata
    public void sendAuditEvent(String action, String carNumber, String status, 
                               Long carServiceId, String performedBy, String details) {
        try {
            // Pass all fields into the object
            CarServiceAuditEvent event = new CarServiceAuditEvent(
                action, carNumber, status, carServiceId, performedBy, details
            );
            
            kafkaTemplate.send(AUDIT_TOPIC, event);
        } catch (Exception e) {
            System.err.println("Failure pushing to audit topic: " + e.getMessage());
        }
    }
}
