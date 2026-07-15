package com.carservice.validation.model;

public class CarValidationDTO {
    
    private String carRegistrationNumber;
    private boolean isValid;
    private String validationTimestamp;

    // Constructors
    public CarValidationDTO() {}

    public CarValidationDTO(String carRegistrationNumber, boolean isValid, String validationTimestamp) {
        this.carRegistrationNumber = carRegistrationNumber;
        this.isValid = isValid;
        this.validationTimestamp = validationTimestamp;
    }

    // Getters and Setters
    public String getCarRegistrationNumber() { return carRegistrationNumber; }
    public void setCarRegistrationNumber(String carRegistrationNumber) { this.carRegistrationNumber = carRegistrationNumber; }

    public boolean isValid() { return isValid; }
    public void setValid(boolean valid) { isValid = valid; }

    public String getValidationTimestamp() { return validationTimestamp; }
    public void setValidationTimestamp(String validationTimestamp) { this.validationTimestamp = validationTimestamp; }
}
