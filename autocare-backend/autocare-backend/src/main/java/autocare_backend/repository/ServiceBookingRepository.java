
package autocare_backend.repository;

import autocare_backend.model.ServiceBooking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceBookingRepository
        extends JpaRepository<ServiceBooking, Long> {
}