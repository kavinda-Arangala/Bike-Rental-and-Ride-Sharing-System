package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.event;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.Payment;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.Rental;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.User;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

/**
 * Call these methods from your RentalServiceImpl and PaymentServiceImpl
 * at the appropriate points in the flow.
 *
 * Example in RentalServiceImpl.createRental():
 *   notificationEventListener.onRentalConfirmed(savedRental);
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    // ── Rental events ─────────────────────────────────────────────────────────

    public void onRentalConfirmed(Rental rental) {
        try {
            notificationService.notifyRentalConfirmed(
                    rental.getUser(),
                    rental.getId(),
                    rental.getBike().getTitle(),
                    rental.getPlannedStartTime().format(DATE_FMT),
                    rental.getPlannedEndTime().format(DATE_FMT),
                    rental.getEstimatedFare() != null
                            ? rental.getEstimatedFare().toPlainString() : "TBD"
            );
        } catch (Exception e) {
            log.error("Failed to send rental confirmed notification: {}", e.getMessage());
        }
    }

    public void onRentalStarted(Rental rental) {
        try {
            notificationService.notifyRentalStarted(
                    rental.getUser(),
                    rental.getId(),
                    rental.getBike().getTitle()
            );
        } catch (Exception e) {
            log.error("Failed to send rental started notification: {}", e.getMessage());
        }
    }

    public void onRentalCompleted(Rental rental) {
        try {
            notificationService.notifyRentalCompleted(
                    rental.getUser(),
                    rental.getId(),
                    rental.getBike().getTitle(),
                    rental.getFinalFare() != null
                            ? rental.getFinalFare().toPlainString() : "0"
            );
        } catch (Exception e) {
            log.error("Failed to send rental completed notification: {}", e.getMessage());
        }
    }

    public void onRentalCancelled(Rental rental, String reason) {
        try {
            notificationService.notifyRentalCancelled(
                    rental.getUser(),
                    rental.getId(),
                    rental.getBike().getTitle(),
                    reason != null ? reason : "No reason provided"
            );
        } catch (Exception e) {
            log.error("Failed to send rental cancelled notification: {}", e.getMessage());
        }
    }

    // ── Payment events ────────────────────────────────────────────────────────

    public void onPaymentSuccess(Payment payment) {
        try {
            notificationService.notifyPaymentSuccess(
                    payment.getUser(),
                    payment.getId(),
                    payment.getAmount().toPlainString(),
                    payment.getRental().getBike().getTitle()
            );
        } catch (Exception e) {
            log.error("Failed to send payment success notification: {}", e.getMessage());
        }
    }

    public void onPaymentRefunded(Payment payment) {
        try {
            notificationService.notifyPaymentRefunded(
                    payment.getUser(),
                    payment.getId(),
                    payment.getRefundAmount() != null
                            ? payment.getRefundAmount().toPlainString() : "0"
            );
        } catch (Exception e) {
            log.error("Failed to send payment refunded notification: {}", e.getMessage());
        }
    }

    // ── User events ───────────────────────────────────────────────────────────

    public void onUserRegistered(User user) {
        try {
            notificationService.notifyWelcome(user);
        } catch (Exception e) {
            log.error("Failed to send welcome notification: {}", e.getMessage());
        }
    }

    public void onPasswordChanged(User user) {
        try {
            notificationService.notifyPasswordChanged(user);
        } catch (Exception e) {
            log.error("Failed to send password changed notification: {}", e.getMessage());
        }
    }
}