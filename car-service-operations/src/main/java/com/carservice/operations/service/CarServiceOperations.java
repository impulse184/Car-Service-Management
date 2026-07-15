package com.carservice.operations.service;

import com.carservice.operations.client.CarValidationClient; 
import com.carservice.operations.client.UserProfileClient; 
import com.carservice.operations.entity.CarService;
import com.carservice.operations.kafka.AuditProducer; 
import com.carservice.operations.repository.CarServiceRepository;
import org.springframework.stereotype.Service;

@Service
public class CarServiceOperations {

    // Calls db repository, validation service, and kafka producer
    private final CarServiceRepository carRepository;
    private final CarValidationClient carValidationClient; 
    private final UserProfileClient userProfileClient;
    private final AuditProducer auditProducer; 

    // Constructor for these dependencies
    public CarServiceOperations(CarServiceRepository carRepository, 
                                CarValidationClient carValidationClient,
                                UserProfileClient userProfileClient,
                                AuditProducer auditProducer) {
        this.carRepository = carRepository;
        this.carValidationClient = carValidationClient;
        this.userProfileClient = userProfileClient;
        this.auditProducer = auditProducer;
    }

    // Handles service request registration
    public CarService createRecord(CarService service, String changedBy) {
        // 1. Verify that the customer exists in the user profile system
        try {
            userProfileClient.getProfileById(service.getCustomerId());
        } catch (Exception e) {
            // Log a failed event to Kafka audit
            auditProducer.sendAuditEvent(
                "CREATE_FAILED_CUSTOMER_NOT_FOUND", 
                service.getCarRegistrationNumber(), 
                "REJECTED", 
                null, 
                changedBy,
                "Customer profile with ID " + service.getCustomerId() + " not found in the system."
            );
            throw new RuntimeException("Validation Failed: Customer with ID " + service.getCustomerId() + " does not exist.");
        }

        // 2. Use car-details-validation-service to verify the car number format
        boolean isValidFormat = carValidationClient.isCarNumberValid(service.getCarRegistrationNumber());
        if (!isValidFormat) {
            // Log a failed event to Kafka audit
            auditProducer.sendAuditEvent(
                "CREATE_FAILED_INVALID_FORMAT", 
                service.getCarRegistrationNumber(), 
                "REJECTED", 
                null, 
                changedBy, // shows the name of the person making the changes
                "Registration number failed regex validation format rules."
            );
            // Stop and throw an exception
            throw new IllegalArgumentException("Validation Failed: The car registration number format is invalid.");
        }

        // 3. Check database to make sure there are no active service records (PENDING / IN_PROGRESS) for this car
        boolean hasActiveService = carRepository.findAllByCarRegistrationNumber(service.getCarRegistrationNumber()).stream()
                .anyMatch(record -> "PENDING".equalsIgnoreCase(record.getServiceStatus()) || "IN_PROGRESS".equalsIgnoreCase(record.getServiceStatus()));
        if (hasActiveService) {
            // Log an active service error event to Kafka audit
            auditProducer.sendAuditEvent(
                "CREATE_FAILED_ACTIVE_SERVICE_EXISTS", 
                service.getCarRegistrationNumber(), 
                "REJECTED", 
                null, 
                changedBy, 
                "Attempted to register service for a vehicle that already has an active service in progress."
            );
            throw new RuntimeException("Validation Failed: The car is already undergoing an active service.");
        }
        
        // Save car service record to database
        CarService savedRecord = carRepository.save(service);

        // Log a successful creation event to Kafka audit
        auditProducer.sendAuditEvent(
            "CREATE_RECORD", 
            savedRecord.getCarRegistrationNumber(), 
            "SUCCESS", 
            savedRecord.getId(), 
            changedBy, 
            "Successfully created new car service file for Customer ID: " + savedRecord.getCustomerId()
        );

        return savedRecord;
    }

    // Handles service status update
    public CarService updateServiceStatus(Long id, String status, String changedBy) {
        // Find existing record by ID, throw an exception if it doesn't exist
        CarService service = carRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service record not found with ID: " + id));
        
        // Keep old status for auditing, then apply the new status
        String oldStatus = service.getServiceStatus();
        service.setServiceStatus(status);
        // Save updated record back to the database
        CarService updatedRecord = carRepository.save(service);

        // Log a successful status change event to Kafka audit
        auditProducer.sendAuditEvent(
            "UPDATE_STATUS_" + status.toUpperCase(), 
            updatedRecord.getCarRegistrationNumber(), 
            "UPDATED", 
            updatedRecord.getId(), 
            changedBy, 
            "Changed service stage from '" + oldStatus + "' to '" + status + "'."
        );

        return updatedRecord;
    }

    // Returns only the status of a car number
    public String getStatusByCarNumber(String carRegistrationNumber) {
        CarService service = carRepository.findByCarRegistrationNumber(carRegistrationNumber)
                .orElseThrow(() -> new RuntimeException("No record found for registration number: " + carRegistrationNumber));
        return service.getServiceStatus();
    }

    // Returns a list of all car service records
    public java.util.List<CarService> getAllRecords() {
        return carRepository.findAll();
    }

    // Fetch service record using its ID
    public CarService getRecordById(Long id) {
        return carRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service record not found with ID: " + id));
    }

    // Handle deleting service record
    public void deleteRecord(Long id, String changedBy) {
        // Check if record exists before deleting
        CarService service = carRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service record not found with ID: " + id));
        
        // Remove the record from the database table
        carRepository.deleteById(id);

        // Log the deletion event to Kafka audit
        auditProducer.sendAuditEvent(
            "DELETE_RECORD", 
            service.getCarRegistrationNumber(), 
            "DELETED", 
            service.getId(), 
            changedBy, 
            "Permanently removed car service file from database records."
        );
    }

    // Fetch service records using customer ID
    public java.util.List<CarService> getRecordsByCustomerId(Long customerId) {
        return carRepository.findAllByCustomerId(customerId);
    }
}
