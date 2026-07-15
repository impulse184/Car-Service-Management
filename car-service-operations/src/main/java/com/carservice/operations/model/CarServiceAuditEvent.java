package com.carservice.operations.model;

import java.io.Serializable;

public class CarServiceAuditEvent implements Serializable {
    private String action;
    private String carRegistrationNumber;
    private String status;
    private Long carServiceId;
    private String performedBy;
    private String details;

    public CarServiceAuditEvent() {}

    // Constructor accepts all fields
    public CarServiceAuditEvent(String action, String carRegistrationNumber, String status, 
                                Long carServiceId, String performedBy, String details) {
        this.action = action;
        this.carRegistrationNumber = carRegistrationNumber;
        this.status = status;
        this.carServiceId = carServiceId;
        this.performedBy = performedBy;
        this.details = details;
    }

    // Getters and Setters
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getCarRegistrationNumber() { return carRegistrationNumber; }
    public void setCarRegistrationNumber(String carRegistrationNumber) { this.carRegistrationNumber = carRegistrationNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getCarServiceId() { return carServiceId; }
    public void setCarServiceId(Long carServiceId) { this.carServiceId = carServiceId; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
