package com.ccd.service;

import com.ccd.model.Car;
import com.ccd.model.Maintenance;
import com.ccd.exception.InvalidEntityException;
import com.ccd.repository.CarRepository;
import com.ccd.repository.MaintenanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class MaintenanceService implements IMaintenanceService {

	@Autowired
	private MaintenanceRepository maintenanceRepository;

	@Autowired
	private CarRepository carRepository;

	@Override
	public String addMaintenance(Maintenance maintenance, Long carId) throws InvalidEntityException {
		if (maintenance == null) {
			throw new InvalidEntityException("Maintenance details cannot be null.");
		}

		if (maintenance.getDefectType() == null || maintenance.getDefectType().isEmpty()) {
			throw new InvalidEntityException("Defect type cannot be empty.");
		}

		if (maintenance.getDefectDescription() == null || maintenance.getDefectDescription().isEmpty()) {
			throw new InvalidEntityException("Defect description cannot be empty.");
		}

		if (maintenance.getMaintenanceCost() <= 0) {
			throw new InvalidEntityException("Maintenance cost must be greater than zero.");
		}

		if (maintenance.getExpectedDeliveryDate().isBefore(LocalDate.now())) {
			throw new InvalidEntityException("Expected delivery date must be today or in the future.");
		}
		Car car = new Car();
		car.setCarId(carId);
		maintenance.setCar(car);

		// Save the maintenance to the repository
		Maintenance savedMaintenance = maintenanceRepository.save(maintenance);

		// Return a success message with the saved maintenance ID
		return "Maintenance added successfully with ID: " + savedMaintenance.getMaintenanceId();
	}

	@Override
	public Maintenance updateMaintenance(Long maintenanceId, Long carId, Maintenance maintenance)
			throws InvalidEntityException {
		// Ensure the maintenance record exists
		Maintenance existingMaintenance = maintenanceRepository.findById(maintenanceId)
				.orElseThrow(() -> new InvalidEntityException("Maintenance record not found"));

		// Ensure the car exists
		Car car = carRepository.findById(carId).orElseThrow(() -> new InvalidEntityException("Car not found"));

		// Update the maintenance object with new values
		existingMaintenance.setDefectType(maintenance.getDefectType());
		existingMaintenance.setDefectDescription(maintenance.getDefectDescription());
		existingMaintenance.setDateReceivedForMaintenance(maintenance.getDateReceivedForMaintenance());
		existingMaintenance.setExpectedDeliveryDate(maintenance.getExpectedDeliveryDate());
		existingMaintenance.setMaintenanceCost(maintenance.getMaintenanceCost());
		existingMaintenance.setMaintenanceStatus(maintenance.getMaintenanceStatus());
		existingMaintenance.setCar(car); // Associate the car

		// Save and return the updated maintenance object
		return maintenanceRepository.save(existingMaintenance);
	}

	@Override
	public Maintenance getMaintenanceById(Long maintenanceId) throws InvalidEntityException {
		if (maintenanceId == null) {
			throw new InvalidEntityException("Maintenance ID cannot be null.");
		}

		return maintenanceRepository.findById(maintenanceId)
				.orElseThrow(() -> new InvalidEntityException("Maintenance not found with ID: " + maintenanceId));
	}

	@Override
	public List<Maintenance> getAllMaintenanceByStatus(String status) {
		return maintenanceRepository.findByMaintenanceStatus(status);
	}

	@Override
	public long getMaintenanceCountByCarId(Long carId) {
		return maintenanceRepository.countByCar_CarId(carId);
	}

	@Override
	public long getMaintenanceCountByDateReceived(LocalDate date) {
		return maintenanceRepository.countByDateReceivedForMaintenance(date);
	}

	@Override
	public long getCountByExpectedDeliveryDate(LocalDate expectedDeliveryDate) {
		return maintenanceRepository.countCarsByExpectedDeliveryDate(expectedDeliveryDate);
	}

	@Override
	public List<Maintenance> getAllMaintenance() {
		return maintenanceRepository.findAll();
	}

	@Override
	public String getFormattedCarMaintenanceCounts() {
		List<Object[]> rawCounts = maintenanceRepository.getCarMaintenanceCounts();
		StringBuilder result = new StringBuilder();

		for (Object[] entry : rawCounts) {
			Long carId = (Long) entry[0];
			Long count = (Long) entry[1];
			result.append("Car ID: ").append(carId).append(", Count: ").append(count).append("\n");
		}

		return result.toString();
	}

	@Override
	public List<Maintenance> getMaintenanceByCarId(Long carId) {
		return maintenanceRepository.findByCar_CarId(carId);
	}

	@Override
	public void deleteMaintenanceById(Long maintenanceId) throws InvalidEntityException {
		// Check if the maintenance record exists
		if (!maintenanceRepository.existsById(maintenanceId)) {
			throw new InvalidEntityException("Maintenance record not found with ID: " + maintenanceId);
		}
		// Delete the record
		maintenanceRepository.deleteById(maintenanceId);
	}

}
