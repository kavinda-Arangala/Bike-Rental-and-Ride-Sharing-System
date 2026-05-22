package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SharedRideReviewRequest {
    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    private String review;
}
