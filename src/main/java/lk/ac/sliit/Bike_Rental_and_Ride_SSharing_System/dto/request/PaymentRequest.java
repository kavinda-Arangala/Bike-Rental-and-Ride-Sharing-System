package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentRequest {

    private Long rentalId;

    private Long sharedRideBookingId;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;  // CASH, CARD, BANK_TRANSFER, WALLET

    private String paymentNote;    // optional note from user
}
