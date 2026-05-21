package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class RentalResponse {

    private Long id;

    // User info
    private Long userId;
    private String userName;
    private String userEmail;

    // Bike info
    private Long bikeId;
    private String bikeTitle;
    private String bikeType;
    private String bikeLocation;

    // Rental period
    private LocalDateTime plannedStartTime;
    private LocalDateTime plannedEndTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // Pricing
    private BigDecimal dailyRate;
    private BigDecimal estimatedFare;
    private BigDecimal finalFare;
    private BigDecimal distanceKm;

    // Status
    private String status;
    private String paymentStatus;
    private Boolean paid;
    private Long chatId;
    private Long ownerId;
    private String ownerName;
    private String ownerPhone;

    // Review
    private Integer rating;
    private String review;
    private Integer ownerServiceRating;
    private String ownerServiceReview;
    private LocalDateTime reviewedAt;

    // Cancellation
    private String cancellationReason;
    private BigDecimal cancellationFee;
    private BigDecimal refundAmount;
    private LocalDateTime cancelledAt;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
