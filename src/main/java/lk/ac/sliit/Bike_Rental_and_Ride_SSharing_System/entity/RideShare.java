package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity;

import jakarta.persistence.*;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.RideShareStatus;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ride_shares", indexes = {
        @Index(name = "idx_rideshare_rental",  columnList = "rental_id"),
        @Index(name = "idx_rideshare_rider",   columnList = "rider_id"),
        @Index(name = "idx_rideshare_owner",   columnList = "owner_id"),
        @Index(name = "idx_rideshare_status",  columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Relationships ─────────────────────────────────────────────────────────

    /**
     * The rental this ride-share request is linked to.
     * One rental can have at most one ride-share request.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_id", nullable = false, unique = true)
    private Rental rental;

    /**
     * The rider (RIDER role user) who submitted this request.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rider_id", nullable = false)
    private User rider;

    /**
     * The bike owner's user ID (copied from bike.getOwnerId() at request time).
     * Stored as a plain Long so we don't need a separate join every time.
     */
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    // ── Location ──────────────────────────────────────────────────────────────

    @Column(name = "pickup_address", nullable = false)
    private String pickupAddress;

    @Column(name = "dropoff_address", nullable = false)
    private String dropoffAddress;

    // ── Status ────────────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RideShareStatus status = RideShareStatus.PENDING;

    // ── Owner decision ────────────────────────────────────────────────────────

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    // ── Audit ─────────────────────────────────────────────────────────────────

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}