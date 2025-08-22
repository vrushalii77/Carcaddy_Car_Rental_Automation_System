package com.ccd.controller;

import com.ccd.model.Maintenance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

@Controller
@RequestMapping("/maintenance")
public class MaintenanceController {


    private String backendBaseUrl="http://localhost:9090";

    private final RestTemplate restTemplate = new RestTemplate();

    // Display the form to add a new maintenance record for a specific car
    @GetMapping("/create/{carId}")
    public String showAddMaintenanceForm(@PathVariable Long carId, Model model) {
        model.addAttribute("maintenance", new Maintenance());
        model.addAttribute("carId", carId);
        return "addMaintenance5";
    }

    // Handle the submission of the add maintenance form
    @PostMapping("/create/{carId}")
    public String addMaintenance(@PathVariable Long carId, @ModelAttribute Maintenance maintenance, BindingResult bindingResult, Model model) {
        try {
            String url = backendBaseUrl + "/maintenance/create/" + carId;
            maintenance.setCarId(carId); // Set the car ID for the maintenance record
            String response = restTemplate.postForObject(url, maintenance, String.class);
            model.addAttribute("message", response);
            return "redirect:/maintenance/all/car/" + carId; // Redirect to maintenance list after successful submission
        } catch (HttpClientErrorException e) {
            // Handle backend validation errors
            Map<String, String> errors = extractErrorsFromException(e);
            for (Map.Entry<String, String> entry : errors.entrySet()) {
                if (entry.getKey().equals("error")) {
                    model.addAttribute("error", entry.getValue()); // Global error
                } else {
                    bindingResult.rejectValue(entry.getKey(), "", entry.getValue()); // Field-specific error
                }
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error adding maintenance: " + e.getMessage());
        }
        return "addMaintenance5"; // Return to the form with errors
    }

    // Display the form to edit a maintenance record
    @GetMapping("/edit/{maintenanceId}/car/{carId}")
    public String showEditMaintenanceForm(@PathVariable Long maintenanceId, @PathVariable Long carId, Model model) {
        try {
            String url = backendBaseUrl + "/maintenance/" + maintenanceId;
            Maintenance maintenance = restTemplate.getForObject(url, Maintenance.class);
            model.addAttribute("maintenance", maintenance);
            model.addAttribute("carId", carId);
        } catch (Exception e) {
            model.addAttribute("error", "Error fetching maintenance details: " + e.getMessage());
        }
        return "editMaintenance5";
    }

    // Handle the submission of the edit maintenance form
    @PostMapping("/update/{maintenanceId}/car/{carId}")
    public String updateMaintenance(@PathVariable Long maintenanceId, @PathVariable Long carId, @ModelAttribute Maintenance maintenance, BindingResult bindingResult, Model model) {
        try {
            String url = backendBaseUrl + "/maintenance/update/" + maintenanceId + "/" + carId;
            restTemplate.put(url, maintenance);
            model.addAttribute("message", "Maintenance record updated successfully.");
            return "redirect:/maintenance/all/car/" + carId;
        } catch (HttpClientErrorException e) {
            // Handle backend validation errors
            Map<String, String> errors = extractErrorsFromException(e);
            for (Map.Entry<String, String> entry : errors.entrySet()) {
                if (entry.getKey().equals("error")) {
                    model.addAttribute("error", entry.getValue()); // Global error
                } else {
                    bindingResult.rejectValue(entry.getKey(), "", entry.getValue()); // Field-specific error
                }
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error updating maintenance: " + e.getMessage());
        }
        return "editMaintenance5"; // Return to the form with errors
    }

    private Map<String, String> extractErrorsFromException(HttpClientErrorException e) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(e.getResponseBodyAsString(), new TypeReference<Map<String, String>>() {});
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
            return Map.of("error", e.getMessage()); // Fallback to global error
        }
    }

   
    // Handle the deletion of a maintenance record
    @PostMapping("/delete/{maintenanceId}/car/{carId}")
    public String deleteMaintenance(@PathVariable Long maintenanceId, @PathVariable Long carId, Model model) {
        try {
            String url = backendBaseUrl + "/maintenance/delete/" + maintenanceId;
            restTemplate.delete(url);
            model.addAttribute("message", "Maintenance record deleted successfully.");
        } catch (Exception e) {
            model.addAttribute("error", "Error deleting maintenance: " + e.getMessage());
        }
        return "redirect:/maintenance/all/car/" + carId;
    }

    // Display all maintenance records for a specific car
    @GetMapping("/all/car/{carId}")
    public String getAllMaintenanceByCar(@PathVariable Long carId, Model model) {
        try {
            String url = backendBaseUrl + "/maintenance/car/" + carId;
            Maintenance[] maintenances = restTemplate.getForObject(url, Maintenance[].class);
            model.addAttribute("maintenances", maintenances);
            model.addAttribute("carId", carId);
        } catch (Exception e) {
            model.addAttribute("error", "Error fetching maintenance records: " + e.getMessage());
        }
        return "MaintenanceList5";
    }
    
 // Add this method to your MaintenanceController class

    @GetMapping("/all")
    public String getAllMaintenance(@RequestParam(required = false) String status, Model model) {
        try {
            String url;
            if (status != null && !status.isEmpty()) {
                url = backendBaseUrl + "/maintenance/status/" + status; // Filter by status
            } else {
                url = backendBaseUrl + "/maintenance/all"; // Fetch all records
            }
            Maintenance[] maintenances = restTemplate.getForObject(url, Maintenance[].class);
            model.addAttribute("maintenances", maintenances);
            model.addAttribute("selectedStatus", status); // Pass the selected status to the template
        } catch (Exception e) {
            model.addAttribute("error", "Error fetching maintenance records: " + e.getMessage());
        }
        return "allMaintenanceList5"; // Thymeleaf template to display all maintenance records
    }
    
    
}