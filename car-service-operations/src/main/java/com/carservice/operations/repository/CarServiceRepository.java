package com.carservice.operations.repository;

import com.carservice.operations.entity.CarService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CarServiceRepository extends JpaRepository<CarService, Long> {

    // Finds record by registration
    Optional<CarService> findByCarRegistrationNumber(String carRegistrationNumber);

    // Finds all history records by registration
    java.util.List<CarService> findAllByCarRegistrationNumber(String carRegistrationNumber);

    // Finds all records by customer ID
    java.util.List<CarService> findAllByCustomerId(Long customerId);
}
