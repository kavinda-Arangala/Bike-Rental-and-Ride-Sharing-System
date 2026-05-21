package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.Rental;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.RentalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {

    // ── User queries ──────────────────────────────────────────────────────────
    List<Rental> findByUserId(Long userId);
    List<Rental> findByUserIdAndStatus(Long userId, RentalStatus status);
    List<Rental> findByBikeOwnerId(Long ownerId);

    // ── Bike queries ──────────────────────────────────────────────────────────
    List<Rental> findByBikeId(Long bikeId);
    List<Rental> findByBikeIdAndStatus(Long bikeId, RentalStatus status);

    // ── Status queries ────────────────────────────────────────────────────────
    List<Rental> findByStatus(RentalStatus status);

    // ── Active rental checks ──────────────────────────────────────────────────
    boolean existsByUserIdAndStatus(Long userId, RentalStatus status);

    // ── Overlap check (fixed: uses enum params instead of string literals) ────
    @Query("SELECT COUNT(r) > 0 FROM Rental r WHERE r.bike.id = :bikeId " +
            "AND r.status IN :blockingStatuses " +
            "AND r.plannedStartTime < :endTime " +
            "AND r.plannedEndTime > :startTime")
    boolean existsOverlappingRental(
            @Param("bikeId") Long bikeId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("blockingStatuses") Collection<RentalStatus> blockingStatuses);

    // ── Admin / stats queries ─────────────────────────────────────────────────
    long countByStatus(RentalStatus status);
    long countByUserId(Long userId);
    List<Rental> findTop10ByOrderByCreatedAtDesc();

    // ── Revenue queries ───────────────────────────────────────────────────────
    @Query("SELECT COALESCE(SUM(r.finalFare), 0) FROM Rental r " +
            "WHERE r.status = :completedStatus")
    BigDecimal calculateTotalRevenue(
            @Param("completedStatus") RentalStatus completedStatus);

    @Query("SELECT COALESCE(SUM(r.finalFare), 0) FROM Rental r " +
            "WHERE r.status = :completedStatus " +
            "AND r.createdAt BETWEEN :start AND :end")
    BigDecimal calculateRevenueBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("completedStatus") RentalStatus completedStatus);
}
