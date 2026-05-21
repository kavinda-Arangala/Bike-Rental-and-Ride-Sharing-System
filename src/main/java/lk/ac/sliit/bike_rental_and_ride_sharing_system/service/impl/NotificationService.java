package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.AdminNotificationRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.NotificationResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.User;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.NotificationChannel;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.NotificationType;

import java.util.List;

public interface NotificationService {

    // ── User operations ───────────────────────────────────────────────────────
    List<NotificationResponse> getMyNotifications(String username);
    List<NotificationResponse> getMyUnreadNotifications(String username);
    long getUnreadCount(String username);
    NotificationResponse markAsRead(String username, Long notificationId);
    int markAllAsRead(String username);
    void deleteMyNotification(String username, Long notificationId);

    // ── Internal — called by event listeners ──────────────────────────────────
    void sendNotification(User user,
                          NotificationType type,
                          NotificationChannel channel,
                          String title,
                          String message,
                          Long referenceId,
                          String referenceType);

    // ── Shortcut helpers used by event listeners ──────────────────────────────
    void notifyWelcome(User user);
    void notifyPasswordChanged(User user);
    void notifyRentalConfirmed(User user, Long rentalId, String bikeTitle, String startDate, String endDate, String fare);
    void notifyRentalStarted(User user, Long rentalId, String bikeTitle);
    void notifyRentalCompleted(User user, Long rentalId, String bikeTitle, String finalFare);
    void notifyRentalCancelled(User user, Long rentalId, String bikeTitle, String reason);
    void notifyPaymentSuccess(User user, Long paymentId, String amount, String bikeTitle);
    void notifyPaymentRefunded(User user, Long paymentId, String refundAmount);
    void notifyReviewReceived(User bikeOwner, String reviewerName, String bikeTitle, int rating);

    // ── Admin operations ──────────────────────────────────────────────────────
    List<NotificationResponse> getAllNotifications();
    void adminSendNotification(AdminNotificationRequest request);
}