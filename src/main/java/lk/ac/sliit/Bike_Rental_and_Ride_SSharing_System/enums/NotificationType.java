package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums;

public enum NotificationType {

    // Account
    ACCOUNT_WELCOME,
    PASSWORD_CHANGED,

    // Rental
    RENTAL_CONFIRMED,
    RENTAL_STARTED,
    RENTAL_COMPLETED,
    RENTAL_CANCELLED,

    // Payment
    PAYMENT_SUCCESS,
    PAYMENT_REFUNDED,

    // Review
    REVIEW_RECEIVED,

    // Ride Sharing  ← NEW
    RIDE_SHARE_REQUESTED,   // Owner notified when rider sends a request
    RIDE_SHARE_APPROVED,    // Rider notified when owner approves
    RIDE_SHARE_REJECTED,    // Rider notified when owner rejects
    RIDE_SHARE_CANCELLED,   // Owner notified when rider cancels
    NEW_MESSAGE,            // Either party notified of a new chat message

    // Admin
    ADMIN_BROADCAST
}