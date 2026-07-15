package com.carservice.operations.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.carservice.operations.entity.ServiceCategory;

public class CarServiceRequestDTO {

    @NotBlank(message = "Registration number is mandatory")
    private String carRegistrationNumber;

    @NotNull(message = "Customer ID is mandatory")
    private Long customerId;

    @NotNull(message = "Service type is mandatory and must be a valid category")
    private ServiceCategory serviceType;

    @PastOrPresent(message = "Service date cannot be in the future")
    @NotNull(message = "Service date is mandatory")
    private LocalDate serviceDate;

    @Size(max = 250, message = "Notes cannot exceed 250 characters")
    private String notes;

    // Constructors
    public CarServiceRequestDTO() {}

    public CarServiceRequestDTO(String carRegistrationNumber, Long customerId, ServiceCategory serviceType, LocalDate serviceDate, String notes) {
        this.carRegistrationNumber = carRegistrationNumber;
        this.customerId = customerId;
        this.serviceType = serviceType;
        this.serviceDate = serviceDate;
        this.notes = notes;
    }

    // Getters and Setters
    public String getCarRegistrationNumber() { return carRegistrationNumber; }
    public void setCarRegistrationNumber(String carRegistrationNumber) { this.carRegistrationNumber = carRegistrationNumber; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public ServiceCategory getServiceType() { return serviceType; }
    public void setServiceType(ServiceCategory serviceType) { this.serviceType = serviceType; }

    public LocalDate getServiceDate() { return serviceDate; }
    public void setServiceDate(LocalDate serviceDate) { this.serviceDate = serviceDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
