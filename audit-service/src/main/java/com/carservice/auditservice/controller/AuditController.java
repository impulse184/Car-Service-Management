package com.carservice.auditservice.controller;

import com.carservice.auditservice.entity.AuditLog;
import com.carservice.auditservice.repository.AuditLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/audits")
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    public AuditController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    // Maps to /audits via Gateway
    @GetMapping
    public ResponseEntity<List<AuditLog>> getAllAuditLogs() {
        // Return logs sorted by ID descending to see the newest events first
        List<AuditLog> logs = auditLogRepository.findAll();
        // Reverse to show latest first
        return ResponseEntity.ok(logs.reversed());
    }
}
