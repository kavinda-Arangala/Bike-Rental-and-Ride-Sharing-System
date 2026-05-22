package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.SharedRideBooking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SharedRideBookingRepository extends JpaRepository<SharedRideBooking, Long> {
    List<SharedRideBooking> findByPassengerIdOrderByCreatedAtDesc(Long passengerId);
    List<SharedRideBooking> findBySharedRideCreatorIdOrderByCreatedAtDesc(Long creatorId);
    boolean existsBySharedRideIdAndPassengerId(Long sharedRideId, Long passengerId);
}
