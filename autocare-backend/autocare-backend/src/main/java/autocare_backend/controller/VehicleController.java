
package autocare_backend.controller;

import autocare_backend.model.Vehicle;
import autocare_backend.repository.VehicleRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleRepository vehicleRepository;

    public VehicleController(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    // Get all vehicles
    @GetMapping
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    // Add a new vehicle
    @PostMapping
    public Vehicle addVehicle(@RequestBody Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }

    // Update an existing vehicle
    @PutMapping("/{id}")
    public ResponseEntity<Vehicle> updateVehicle(
            @PathVariable Long id,
            @RequestBody Vehicle updatedVehicle) {

        return vehicleRepository.findById(id)
                .map(vehicle -> {
                    vehicle.setName(updatedVehicle.getName());
                    vehicle.setModel(updatedVehicle.getModel());
                    vehicle.setRegistration(updatedVehicle.getRegistration());
                    vehicle.setYear(updatedVehicle.getYear());

                    Vehicle savedVehicle = vehicleRepository.save(vehicle);
                    return ResponseEntity.ok(savedVehicle);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Delete a vehicle
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {

        if (!vehicleRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        vehicleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}