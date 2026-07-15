package com.carservice.auditservice.kafka;

import com.carservice.auditservice.entity.AuditLog;
import com.carservice.auditservice.repository.AuditLogRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.time.LocalDateTime;

@Component
public class AuditConsumer {

    // Repository used to talk to database
    private final AuditLogRepository auditRepository;

    // Constructor for repository dependency
    public AuditConsumer(AuditLogRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    // Listens continuously to the Kafka topic "car-service-audit-events" under the consumer group "audit-group"
    @KafkaListener(topics = "car-service-audit-events", groupId = "audit-group")
    public void consumeAuditEvent(Map<String, Object> eventData) {
        // Prints a console message showing payload received from Kafka
        System.out.println("Received Audit Event from Kafka: " + eventData);
        
        try {
            // Create a new AuditLog entity for database
            AuditLog log = new AuditLog();
            
            // Extract text data fields from incoming Kafka message and map them to entity
            log.setAction((String) eventData.get("action"));
            log.setCarRegistrationNumber((String) eventData.get("carRegistrationNumber"));
            log.setStatus((String) eventData.get("status"));
            
            // Add current date and time
            log.setTimestamp(LocalDateTime.now());
            
            // Look for and handle the unique ID field
            if (eventData.get("carServiceId") != null) {
                // Extracts number fields as Java Number object and turns it into Long value (handles Kafka int vs long issue)
                log.setCarServiceId(((Number) eventData.get("carServiceId")).longValue());
            }
            
            // Extract the security text strings from the payload
            log.setPerformedBy((String) eventData.get("performedBy"));
            log.setDetails((String) eventData.get("details"));
            
            // Save the audit record to database table
            auditRepository.save(log);
            System.out.println("Audit log securely saved to DB.");
            
        } catch (Exception e) {
            // Ensure if one bad message breaks, the entire application doesn't crash
            System.err.println("Error processing incoming audit event: " + e.getMessage());
            // Output system error to logs
            e.printStackTrace(); 
        }
    }
}
