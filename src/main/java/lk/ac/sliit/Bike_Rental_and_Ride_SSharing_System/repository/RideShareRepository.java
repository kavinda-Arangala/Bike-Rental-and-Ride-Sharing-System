package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.RideShare;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.RideShareStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RideShareRepository extends JpaRepository<RideShare, Long> {

    // ── Existence checks ──────────────────────────────────────────────────────

    /**
     * Prevent duplicate ride-share requests for the same rental.
     */
    boolean existsByRentalId(Long rentalId);

    /**
     * Check if a ride-share already exists for a rental with a given status.
     */
    boolean existsByRentalIdAndStatus(Long rentalId, RideShareStatus status);

    // ── Rider queries ─────────────────────────────────────────────────────────

    /**
     * All ride-share requests submitted by a specific rider.
     */
    List<RideShare> findByRiderIdOrderByCreatedAtDesc(Long riderId);

    /**
     * Rider's requests filtered by status.
     */
    List<RideShare> findByRiderIdAndStatusOrderByCreatedAtDesc(Long riderId, RideShareStatus status);

    /**
     * Find the single ride-share linked to a rental (used by rider to check their request).
     */
    Optional<RideShare> findByRentalId(Long rentalId);

    // ── Owner queries ─────────────────────────────────────────────────────────

    /**
     * All ride-share requests for bikes owned by this owner.
     */
    List<RideShare> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    /**
     * Owner's requests filtered by status — e.g. only PENDING ones needing action.
     */
    List<RideShare> findByOwnerIdAndStatusOrderByCreatedAtDesc(Long ownerId, RideShareStatus status);

    /**
     * Count pending requests for an owner — useful for dashboard badge.
     */
    long countByOwnerIdAndStatus(Long ownerId, RideShareStatus status);

    // ── Admin queries ─────────────────────────────────────────────────────────

    /**
     * All ride-shares filtered by status — admin overview.
     */
    List<RideShare> findByStatusOrderByCreatedAtDesc(RideShareStatus status);

    /**
     * Count all ride-shares by status — admin stats.
     */
    long countByStatus(RideShareStatus status);

    // ── Access validation ─────────────────────────────────────────────────────

    /**
     * Verify a ride-share belongs to a given rider OR owner.
     * Used to ensure only the two parties can access the chat.
     */
    @Query("SELECT rs FROM RideShare rs WHERE rs.id = :id " +
            "AND (rs.rider.id = :userId OR rs.ownerId = :userId)")
    Optional<RideShare> findByIdAndParticipant(
            @Param("id") Long id,
            @Param("userId") Long userId);

    /**
     * All active (APPROVED) ride-shares for a rider — useful for dashboard.
     */
    @Query("SELECT rs FROM RideShare rs WHERE rs.rider.id = :riderId " +
            "AND rs.status = 'APPROVED' ORDER BY rs.createdAt DESC")
    List<RideShare> findActiveByRiderId(@Param("riderId") Long riderId);
}