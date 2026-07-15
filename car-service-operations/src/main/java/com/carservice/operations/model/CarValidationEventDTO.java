package com.carservice.operations.model;

public class CarValidationEventDTO {
    
    private Long serviceId;
    private String carRegistrationNumber;

    public CarValidationEventDTO() {}

    // Prepares serviceID and car number for validation 
    public CarValidationEventDTO(Long serviceId, String carRegistrationNumber) {
        this.serviceId = serviceId;
        this.carRegistrationNumber = carRegistrationNumber;
    }

    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }

    public String getCarRegistrationNumber() { return carRegistrationNumber; }
    public void setCarRegistrationNumber(String carRegistrationNumber) { this.carRegistrationNumber = carRegistrationNumber; }
}
