package com.ccd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ccd.model.Car;

import java.util.List;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
	List<Car> findByStatus(String status);

	List<Car> findByVehicleType(String type);

	List<Car> findByRentRateBetween(double minRate, double maxRate);
}
