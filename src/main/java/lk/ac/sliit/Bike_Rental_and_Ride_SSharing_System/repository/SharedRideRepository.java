package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.SharedRide;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.SharedRideStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SharedRideRepository extends JpaRepository<SharedRide, Long> {
    List<SharedRide> findByStatusOrderByRideTimeAsc(SharedRideStatus status);
    List<SharedRide> findByCreatorIdOrderByCreatedAtDesc(Long creatorId);
    List<SharedRide> findByStatusOrderByCreatedAtDesc(SharedRideStatus status);
}
