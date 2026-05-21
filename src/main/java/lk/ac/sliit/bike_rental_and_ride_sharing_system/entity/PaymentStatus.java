package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums;

public enum PaymentStatus {
    PENDING,    // Payment initiated but not confirmed
    SUCCESS,    // Payment completed successfully
    FAILED,     // Payment attempt failed
    REFUNDED    // Payment was refunded (after cancellation)
}