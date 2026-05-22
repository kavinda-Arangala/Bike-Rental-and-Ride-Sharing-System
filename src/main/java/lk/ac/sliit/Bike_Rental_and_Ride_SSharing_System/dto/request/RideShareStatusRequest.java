package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RideShareStatusRequest {

    /**
     * Must be either "APPROVE" or "REJECT".
     */
    @NotBlank(message = "Action is required")
    @Pattern(regexp = "^(APPROVE|REJECT)$",
            message = "Action must be either APPROVE or REJECT")
    private String action;

    /**
     * Required only when action = REJECT.
     */
    private String rejectionReason;
}