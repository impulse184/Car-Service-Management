package com.carservice.operations.controller;

import com.carservice.operations.entity.CarService;
import com.carservice.operations.model.CarServiceRequestDTO; 
import com.carservice.operations.model.ServiceStatus; 
import com.carservice.operations.service.CarServiceOperations;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.Map;

@RestController
@RequestMapping("/") 
public class CarServiceController {

    private final CarServiceOperations operationsService;

    public CarServiceController(CarServiceOperations operationsService) {
        this.operationsService = operationsService;
    }

    // Maps to: POST http://localhost:8082/carservice/save
    @PostMapping("/save")
    public ResponseEntity<CarService> registerService(
            @Valid @RequestBody CarServiceRequestDTO requestDTO,
            @Parameter(hidden = true) @RequestHeader(value = "X-Authenticated-User", defaultValue = "anonymous") String changedBy) {
        
        // Convert DTO into database Entity 
        CarService carService = new CarService();
        carService.setCarRegistrationNumber(requestDTO.getCarRegistrationNumber());
        carService.setCustomerId(requestDTO.getCustomerId());
        carService.setServiceType(requestDTO.getServiceType());
        carService.setServiceDate(requestDTO.getServiceDate());
        carService.setNotes(requestDTO.getNotes());
        
        // Default status before saving to DB
        carService.setServiceStatus("PENDING");

        // Pass record to database layer
        CarService savedRecord = operationsService.createRecord(carService, changedBy);
        return new ResponseEntity<>(savedRecord, HttpStatus.CREATED);
    }

    // Maps to: PUT http://localhost:8082/carservice/{id}/status?status=
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateServiceStatus(
            @PathVariable("id") Long id, 
            @RequestParam("status") String status,
            @Parameter(hidden = true) @RequestHeader(value = "X-Authenticated-Role", defaultValue = "anonymous") String role,
            @Parameter(hidden = true) @RequestHeader(value = "X-Authenticated-User", defaultValue = "anonymous") String changedBy) {
        try {
            // Case insensitive
            ServiceStatus targetStatus = ServiceStatus.valueOf(status.trim().toUpperCase());

            CarService existing = operationsService.getRecordById(id);
            if ("COMPLETED".equalsIgnoreCase(existing.getServiceStatus()) && !"admin".equalsIgnoreCase(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access Denied: Only administrators can revert a completed service record.");
            }
            
            // Path to service layer with username 
            CarService updatedService = operationsService.updateServiceStatus(id, targetStatus.name(), changedBy);
            return ResponseEntity.ok(updatedService);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body("Invalid status value. Allowed values are: PENDING, IN_PROGRESS, COMPLETED, CANCELLED");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Maps to: GET http://localhost:8082/carservice
    @GetMapping
    public ResponseEntity<java.util.List<CarService>> getAllServices(
            @Parameter(hidden = true) @RequestHeader(value = "X-Authenticated-Role", required = false) String role,
            @Parameter(hidden = true) @RequestHeader(value = "X-Authenticated-Id", required = false) String userIdHeader) {
        
        if ("customer".equalsIgnoreCase(role) && userIdHeader != null) {
            Long customerId = Long.parseLong(userIdHeader);
            return ResponseEntity.ok(operationsService.getRecordsByCustomerId(customerId));
        }
        return ResponseEntity.ok(operationsService.getAllRecords());
    }

    // Maps to: GET http://localhost:8082/carservice/{id}
    @GetMapping("/{id}")
    public ResponseEntity<CarService> getServiceById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(operationsService.getRecordById(id)); 
    }

    // Maps to: DELETE http://localhost:8082/carservice/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteService(
            @PathVariable("id") Long id,
            @Parameter(hidden = true) @RequestHeader(value = "X-Authenticated-User", defaultValue = "anonymous") String changedBy) {
        operationsService.deleteRecord(id, changedBy);
        
        // Delete confirm message
        return ResponseEntity.ok(Map.of(
            "message", "Car service record with ID " + id + " has been successfully deleted.",
            "status", "SUCCESS",
            "deletedBy", changedBy
        ));
    }
}
