package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.impl;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.AdminNotificationRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.NotificationResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.Notification;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.User;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.NotificationChannel;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.NotificationType;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.exception.ResourceNotFoundException;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.NotificationRepository;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.UserRepository;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.EmailService;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Override
    public List<NotificationResponse> getMyNotifications(String username) {
        User user = findUserByUsernameOrThrow(username);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<NotificationResponse> getMyUnreadNotifications(String username) {
        User user = findUserByUsernameOrThrow(username);
        return notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public long getUnreadCount(String username) {
        User user = findUserByUsernameOrThrow(username);
        return notificationRepository.countByUserIdAndReadFalse(user.getId());
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(String username, Long notificationId) {
        Notification notification = findOwnedNotificationOrThrow(username, notificationId);
        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notification = notificationRepository.save(notification);
        }
        return toResponse(notification);
    }

    @Override
    @Transactional
    public int markAllAsRead(String username) {
        User user = findUserByUsernameOrThrow(username);
        return notificationRepository.markAllAsRead(user.getId());
    }

    @Override
    @Transactional
    public void deleteMyNotification(String username, Long notificationId) {
        Notification notification = findOwnedNotificationOrThrow(username, notificationId);
        notificationRepository.delete(notification);
    }

    @Override
    @Transactional
    public void sendNotification(User user,
                                 NotificationType type,
                                 NotificationChannel channel,
                                 String title,
                                 String message,
                                 Long referenceId,
                                 String referenceType) {
        if (user == null) {
            log.warn("Skipping notification because user is null. type={}, title={}", type, title);
            return;
        }

        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .channel(channel)
                .title(title)
                .message(message)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .build();

        if (channel == NotificationChannel.EMAIL || channel == NotificationChannel.BOTH) {
            boolean sent = dispatchEmail(user, title, message);
            notification.setEmailSent(sent);
        }

        notificationRepository.save(notification);
    }

    @Override
    public void notifyWelcome(User user) {
        sendNotification(
                user,
                NotificationType.ACCOUNT_WELCOME,
                NotificationChannel.BOTH,
                "Welcome to Bike Rental and Ride Sharing",
                "Your account is ready. Start exploring available bikes and rides.",
                null,
                "USER"
        );
    }

    @Override
    public void notifyPasswordChanged(User user) {
        sendNotification(
                user,
                NotificationType.PASSWORD_CHANGED,
                NotificationChannel.BOTH,
                "Password changed",
                "Your account password was changed successfully.",
                null,
                "USER"
        );
    }

    @Override
    public void notifyRentalConfirmed(User user,
                                      Long rentalId,
                                      String bikeTitle,
                                      String startDate,
                                      String endDate,
                                      String fare) {
        sendNotification(
                user,
                NotificationType.RENTAL_CONFIRMED,
                NotificationChannel.BOTH,
                "Rental confirmed",
                "Your rental for " + bikeTitle + " is confirmed from " + startDate + " to " + endDate
                        + ". Estimated fare: " + fare + ".",
                rentalId,
                "RENTAL"
        );
    }

    @Override
    public void notifyRentalStarted(User user, Long rentalId, String bikeTitle) {
        sendNotification(
                user,
                NotificationType.RENTAL_STARTED,
                NotificationChannel.IN_APP,
                "Rental started",
                "Your rental for " + bikeTitle + " has started.",
                rentalId,
                "RENTAL"
        );
    }

    @Override
    public void notifyRentalCompleted(User user, Long rentalId, String bikeTitle, String finalFare) {
        sendNotification(
                user,
                NotificationType.RENTAL_COMPLETED,
                NotificationChannel.BOTH,
                "Rental completed",
                "Your rental for " + bikeTitle + " is complete. Final fare: " + finalFare + ".",
                rentalId,
                "RENTAL"
        );
    }

    @Override
    public void notifyRentalCancelled(User user, Long rentalId, String bikeTitle, String reason) {
        sendNotification(
                user,
                NotificationType.RENTAL_CANCELLED,
                NotificationChannel.BOTH,
                "Rental cancelled",
                "Your rental for " + bikeTitle + " was cancelled. Reason: " + reason + ".",
                rentalId,
                "RENTAL"
        );
    }

    @Override
    public void notifyPaymentSuccess(User user, Long paymentId, String amount, String bikeTitle) {
        sendNotification(
                user,
                NotificationType.PAYMENT_SUCCESS,
                NotificationChannel.BOTH,
                "Payment successful",
                "Payment of " + amount + " for " + bikeTitle + " was successful.",
                paymentId,
                "PAYMENT"
        );
    }

    @Override
    public void notifyPaymentRefunded(User user, Long paymentId, String refundAmount) {
        sendNotification(
                user,
                NotificationType.PAYMENT_REFUNDED,
                NotificationChannel.BOTH,
                "Payment refunded",
                "A refund of " + refundAmount + " has been processed.",
                paymentId,
                "PAYMENT"
        );
    }

    @Override
    public void notifyReviewReceived(User bikeOwner, String reviewerName, String bikeTitle, int rating) {
        sendNotification(
                bikeOwner,
                NotificationType.REVIEW_RECEIVED,
                NotificationChannel.IN_APP,
                "New review received",
                reviewerName + " left a " + rating + "-star review for " + bikeTitle + ".",
                null,
                "REVIEW"
        );
    }

    @Override
    public List<NotificationResponse> getAllNotifications() {
        return notificationRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void adminSendNotification(AdminNotificationRequest request) {
        NotificationChannel channel = parseChannel(request.getChannel());

        if (request.getUserId() != null) {
            User user = findUserByIdOrThrow(request.getUserId());
            sendNotification(
                    user,
                    NotificationType.ADMIN_BROADCAST,
                    channel,
                    request.getTitle(),
                    request.getMessage(),
                    null,
                    "ADMIN"
            );
            return;
        }

        userRepository.findAll().forEach(user -> sendNotification(
                user,
                NotificationType.ADMIN_BROADCAST,
                channel,
                request.getTitle(),
                request.getMessage(),
                null,
                "ADMIN"
        ));
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUser().getId())
                .username(notification.getUser().getUsername())
                .type(notification.getType().name())
                .channel(notification.getChannel().name())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .referenceId(notification.getReferenceId())
                .referenceType(notification.getReferenceType())
                .read(notification.isRead())
                .emailSent(notification.isEmailSent())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }

    private User findUserByUsernameOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private User findUserByIdOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: id=" + userId));
    }

    private Notification findOwnedNotificationOrThrow(String username, Long notificationId) {
        User user = findUserByUsernameOrThrow(username);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: id=" + notificationId));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Notification not found: id=" + notificationId);
        }

        return notification;
    }

    private NotificationChannel parseChannel(String rawChannel) {
        if (!StringUtils.hasText(rawChannel)) {
            throw new IllegalArgumentException("Channel is required. Use EMAIL, IN_APP, or BOTH.");
        }

        try {
            return NotificationChannel.valueOf(rawChannel.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid channel. Use EMAIL, IN_APP, or BOTH.");
        }
    }

    private boolean dispatchEmail(User user, String title, String message) {
        if (!StringUtils.hasText(user.getEmail())) {
            return false;
        }

        String displayName = StringUtils.hasText(user.getName())
                ? user.getName()
                : user.getUsername();

        String html = emailService.buildEmailHtml(
                displayName,
                title,
                message.replace("\n", "<br/>")
        );

        emailService.sendHtmlEmail(user.getEmail(), title, html);
        return true;
    }
}
