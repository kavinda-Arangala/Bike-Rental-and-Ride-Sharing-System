package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums;

public enum RideShareStatus {
    PENDING,    // Rider submitted request, waiting for owner approval
    APPROVED,   // Owner approved — chat is now unlocked
    REJECTED,   // Owner rejected the request
    CANCELLED,  // Rider cancelled before owner acted
    COMPLETED   // Ride share ended (rental completed)
}