package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.SharedRideMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SharedRideMessageRepository extends JpaRepository<SharedRideMessage, Long> {
    List<SharedRideMessage> findByBookingIdOrderBySentAtAsc(Long bookingId);

    @Modifying
    @Query("UPDATE SharedRideMessage m SET m.read = true, m.readAt = CURRENT_TIMESTAMP " +
            "WHERE m.booking.id = :bookingId AND m.sender.id != :userId AND m.read = false")
    int markAllAsReadForUser(@Param("bookingId") Long bookingId, @Param("userId") Long userId);

    @Query("SELECT COUNT(m) FROM SharedRideMessage m WHERE m.booking.id = :bookingId " +
            "AND m.sender.id != :userId AND m.read = false")
    long countUnreadByBookingIdForUser(@Param("bookingId") Long bookingId, @Param("userId") Long userId);
}
