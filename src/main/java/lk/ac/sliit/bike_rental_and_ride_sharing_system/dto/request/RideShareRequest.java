package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RideShareRequest {

    @NotNull(message = "Rental ID is required")
    private Long rentalId;

    @NotBlank(message = "Pickup address is required")
    @Size(min = 3, max = 300, message = "Pickup address must be between 3 and 300 characters")
    private String pickupAddress;

    @NotBlank(message = "Drop-off address is required")
    @Size(min = 3, max = 300, message = "Drop-off address must be between 3 and 300 characters")
    private String dropoffAddress;
}