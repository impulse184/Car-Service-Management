package com.carservice.validation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/carvalidation") 
public class CarValidationController {

    @GetMapping("/validate")
    public boolean isCarNumberValid(@RequestParam("carNumber") String carNumber) {
        // Check the national number plate format
        String regex = "^[A-Z]{2}[0-9]{1,2}[A-Z]{1,2}[0-9]{4}$";
        
        if (carNumber == null) {
            return false;
        }
        
        // makes all uppercase, removes spaces, and matches to format
        return carNumber.toUpperCase().trim().matches(regex);
    }
}
