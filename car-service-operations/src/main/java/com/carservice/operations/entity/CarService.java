package com.carservice.operations.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "car_services")
public class CarService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Registration number is mandatory")
    @Column(name = "car_registration_number", nullable = false, length = 20)
    private String carRegistrationNumber;

    @NotNull(message = "Customer ID is mandatory")
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @NotNull(message = "Service type is mandatory")
    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 50)
    private ServiceCategory serviceType;

    @NotNull(message = "Service date is mandatory")
    @Column(name = "service_date", nullable = false)
    private LocalDate serviceDate;

    @Column(name = "service_status", nullable = false, length = 50)
    private String serviceStatus;

    @Size(max = 250, message = "Notes cannot exceed 250 characters")
    @Column(name = "notes", length = 250)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Default constructor for JPA
    public CarService() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCarRegistrationNumber() { return carRegistrationNumber; }
    public void setCarRegistrationNumber(String carRegistrationNumber) { this.carRegistrationNumber = carRegistrationNumber; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public ServiceCategory getServiceType() { return serviceType; }
    public void setServiceType(ServiceCategory serviceType) { this.serviceType = serviceType; }

    public LocalDate getServiceDate() { return serviceDate; }
    public void setServiceDate(LocalDate serviceDate) { this.serviceDate = serviceDate; }

    public String getServiceStatus() { return serviceStatus; }
    public void setServiceStatus(String serviceStatus) { this.serviceStatus = serviceStatus; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
