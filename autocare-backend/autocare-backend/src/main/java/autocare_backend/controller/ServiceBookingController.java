
package autocare_backend.controller;

import autocare_backend.model.ServiceBooking;
import autocare_backend.repository.ServiceBookingRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class ServiceBookingController {

    private final ServiceBookingRepository bookingRepository;

    public ServiceBookingController(
            ServiceBookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @GetMapping
    public List<ServiceBooking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @PostMapping
    public ServiceBooking addBooking(
            @RequestBody ServiceBooking booking) {
        return bookingRepository.save(booking);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ServiceBooking> updateBookingStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        String status = request.get("status");

        if (status == null ||
                !Arrays.asList(
                        "Pending",
                        "Confirmed",
                        "In Progress",
                        "Completed"
                ).contains(status)) {
            return ResponseEntity.badRequest().build();
        }

        return bookingRepository.findById(id)
                .map(booking -> {
                    booking.setStatus(status);
                    ServiceBooking savedBooking =
                            bookingRepository.save(booking);
                    return ResponseEntity.ok(savedBooking);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(
            @PathVariable Long id) {

        if (!bookingRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        bookingRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}