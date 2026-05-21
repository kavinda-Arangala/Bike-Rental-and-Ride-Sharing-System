package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class SharedRideBookingResponse {
    private Long id;
    private Long sharedRideId;
    private String bikeTitle;
    private String pickupAddress;
    private String dropoffAddress;
    private LocalDateTime rideTime;
    private BigDecimal price;
    private Long passengerId;
    private String passengerName;
    private String status;
    private String paymentStatus;
    private Integer rating;
    private String review;
    private LocalDateTime createdAt;
}
