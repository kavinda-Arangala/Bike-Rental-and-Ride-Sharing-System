package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums;

public enum RentalStatus {
    PENDING,      // Booking created, waiting for rider payment
    PAID,         // Rider paid, waiting for bike owner confirmation
    CONFIRMED,    // Bike owner confirmed, rider can start
    ACTIVE,       // Bike picked up, rental in progress
    COMPLETED,    // Bike returned, fare calculated
    CANCELLED     // Booking cancelled before pickup
}
