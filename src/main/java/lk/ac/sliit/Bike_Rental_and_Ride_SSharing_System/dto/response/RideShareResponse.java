package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class RideShareResponse {

    private Long id;

    // ── Rental info ───────────────────────────────────────────────────────────
    private Long rentalId;
    private String rentalStatus;
    private LocalDateTime plannedStartTime;
    private LocalDateTime plannedEndTime;
    private BigDecimal estimatedFare;
    private BigDecimal finalFare;

    // ── Bike info ─────────────────────────────────────────────────────────────
    private Long bikeId;
    private String bikeTitle;
    private String bikeType;
    private String bikeLocation;
    private String bikePhotoUrl;

    // ── Rider details (visible to the owner) ─────────────────────────────────
    private Long riderId;
    private String riderName;
    private String riderUsername;
    private String riderEmail;
    private String riderPhone;
    private String riderProfileImage;

    // ── Owner info ────────────────────────────────────────────────────────────
    private Long ownerId;
    private String ownerName;
    private String ownerPhone;

    // ── Ride-share details ────────────────────────────────────────────────────
    private String pickupAddress;
    private String dropoffAddress;
    private String status;
    private String rejectionReason;
    private LocalDateTime decidedAt;

    // ── Unread message count (for notification badge) ─────────────────────────
    private long unreadMessageCount;

    // ── Audit ─────────────────────────────────────────────────────────────────
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}