package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.Notification;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // All notifications for a user (newest first)
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Unread notifications for a user
    List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(Long userId);

    // Count unread for a user
    long countByUserIdAndReadFalse(Long userId);

    // By type for a user
    List<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, NotificationType type);

    // Mark all as read for a user
    @Modifying
    @Query("UPDATE Notification n SET n.read = true, n.readAt = CURRENT_TIMESTAMP " +
            "WHERE n.user.id = :userId AND n.read = false")
    int markAllAsRead(@Param("userId") Long userId);

    // Admin: all notifications newest first
    List<Notification> findAllByOrderByCreatedAtDesc();

    // Admin: by reference (e.g. all notifications for rental #5)
    List<Notification> findByReferenceIdAndReferenceType(Long referenceId, String referenceType);
}