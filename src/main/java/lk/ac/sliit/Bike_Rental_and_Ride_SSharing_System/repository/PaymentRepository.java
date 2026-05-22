package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.Payment;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // ── User queries ──────────────────────────────────────────────────────────
    List<Payment> findByUserId(Long userId);
    List<Payment> findByUserIdAndStatus(Long userId, PaymentStatus status);
    List<Payment> findByRentalBikeOwnerId(Long ownerId);

    // ── Rental queries ────────────────────────────────────────────────────────
    List<Payment> findByRentalId(Long rentalId);
    Optional<Payment> findByRentalIdAndStatus(Long rentalId, PaymentStatus status);
    boolean existsByRentalIdAndStatus(Long rentalId, PaymentStatus status);
    Optional<Payment> findBySharedRideBookingIdAndStatus(Long sharedRideBookingId, PaymentStatus status);
    boolean existsBySharedRideBookingIdAndStatus(Long sharedRideBookingId, PaymentStatus status);

    // ── Status queries ────────────────────────────────────────────────────────
    List<Payment> findByStatus(PaymentStatus status);

    // ── Transaction ID lookup ─────────────────────────────────────────────────
    Optional<Payment> findByTransactionId(String transactionId);

    // ── Revenue queries ───────────────────────────────────────────────────────
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
            "WHERE p.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
            "WHERE p.status = :status " +
            "AND p.createdAt BETWEEN :start AND :end")
    BigDecimal sumAmountByStatusBetween(
            @Param("status") PaymentStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // ── Count queries ─────────────────────────────────────────────────────────
    long countByStatus(PaymentStatus status);
    long countByUserId(Long userId);
}
