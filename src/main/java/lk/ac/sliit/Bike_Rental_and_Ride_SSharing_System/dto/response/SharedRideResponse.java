package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class SharedRideResponse {
    private Long id;
    private Long rentalId;
    private Long creatorId;
    private String creatorName;
    private String bikeTitle;
    private String pickupAddress;
    private String dropoffAddress;
    private LocalDateTime rideTime;
    private BigDecimal price;
    private Integer availableSeats;
    private String status;
    private LocalDateTime createdAt;
}
