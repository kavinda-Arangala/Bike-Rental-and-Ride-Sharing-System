package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CancellationResponse {

    private Long rentalId;
    private String status;

    // Fee breakdown
    private boolean freeCancellation;
    private BigDecimal cancellationFee;
    private BigDecimal refundAmount;
    private String cancellationReason;

    // Policy message shown to user
    private String policyMessage;

    private LocalDateTime cancelledAt;
}