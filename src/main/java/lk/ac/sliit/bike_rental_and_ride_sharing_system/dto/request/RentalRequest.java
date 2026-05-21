package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RentalRequest {

    @NotNull(message = "Bike ID is required")
    private Long bikeId;

    @NotNull(message = "Planned start time is required")
    @Future(message = "Start time must be in the future")
    private LocalDateTime plannedStartTime;

    @NotNull(message = "Planned end time is required")
    @Future(message = "End time must be in the future")
    private LocalDateTime plannedEndTime;
}