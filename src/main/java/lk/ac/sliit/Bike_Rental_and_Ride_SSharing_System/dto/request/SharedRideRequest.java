package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SharedRideRequest {
    @NotNull
    private Long rentalId;

    @NotBlank
    private String pickupAddress;

    @NotBlank
    private String dropoffAddress;

    @NotNull
    private LocalDateTime rideTime;

    @NotNull
    @DecimalMin(value = "1.00")
    private BigDecimal price;

    @NotNull
    @Min(1)
    @Max(10)
    private Integer availableSeats;
}
