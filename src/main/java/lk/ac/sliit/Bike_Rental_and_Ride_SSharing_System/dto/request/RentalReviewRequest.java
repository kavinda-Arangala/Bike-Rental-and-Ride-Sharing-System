package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RentalReviewRequest {

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    private String review;

    @Min(value = 1, message = "Owner service rating must be at least 1")
    @Max(value = 5, message = "Owner service rating must be at most 5")
    private Integer ownerServiceRating;

    private String ownerServiceReview;
}
