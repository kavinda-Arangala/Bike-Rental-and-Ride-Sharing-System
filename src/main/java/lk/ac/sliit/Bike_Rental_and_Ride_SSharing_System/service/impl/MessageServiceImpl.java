package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.impl;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.MessageRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.MessageResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.Message;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.RideShare;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.User;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.NotificationChannel;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.NotificationType;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.RideShareStatus;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.exception.ResourceNotFoundException;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.MessageRepository;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.RideShareRepository;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.UserRepository;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.MessageService;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository   messageRepository;
    private final RideShareRepository rideShareRepository;
    private final UserRepository      userRepository;
    private final NotificationService notificationService;

    // ── Send Message ──────────────────────────────────────────────────────────

    @Override
    @Transactional
    public MessageResponse sendMessage(String username, Long rideShareId,
                                       MessageRequest request) {
        User      sender   = findUserOrThrow(username);
        RideShare rideShare = findRideShareOrThrow(rideShareId);

        // Validate sender is a participant (rider or owner)
        validateParticipant(rideShare, sender);

        // Chat opens as soon as a rental request exists, before payment.
        if (rideShare.getStatus() == RideShareStatus.REJECTED ||
                rideShare.getStatus() == RideShareStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Chat is not available for closed ride-share requests. " +
                            "Current status: " + rideShare.getStatus());
        }

        Message message = Message.builder()
                .rideShare(rideShare)
                .sender(sender)
                .content(request.getContent().trim())
                .build();

        Message saved = messageRepository.save(message);
        log.info("Message sent: id={}, rideShareId={}, senderId={}",
                saved.getId(), rideShareId, sender.getId());

        // Notify the other party about the new message
        notifyOtherParty(rideShare, sender, request.getContent());

        return toResponse(saved, sender.getId());
    }

    // ── Get Messages (and mark as read) ──────────────────────────────────────

    @Override
    @Transactional
    public List<MessageResponse> getMessages(String username, Long rideShareId) {
        User      user      = findUserOrThrow(username);
        RideShare rideShare = findRideShareOrThrow(rideShareId);

        // Validate the caller is a participant
        validateParticipant(rideShare, user);

        // Mark all messages from the other party as read
        int markedRead = messageRepository.markAllAsReadForUser(rideShareId, user.getId());
        if (markedRead > 0) {
            log.info("Marked {} messages as read: rideShareId={}, userId={}",
                    markedRead, rideShareId, user.getId());
        }

        return messageRepository
                .findByRideShareIdOrderBySentAtAsc(rideShareId)
                .stream()
                .map(m -> toResponse(m, user.getId()))
                .toList();
    }

    // ── Unread Count for One Chat ─────────────────────────────────────────────

    @Override
    public long getUnreadCount(String username, Long rideShareId) {
        User      user      = findUserOrThrow(username);
        RideShare rideShare = findRideShareOrThrow(rideShareId);
        validateParticipant(rideShare, user);
        return messageRepository.countUnreadByRideShareIdForUser(rideShareId, user.getId());
    }

    // ── Total Unread Across All Chats ─────────────────────────────────────────

    @Override
    public long getTotalUnreadCount(String username) {
        User user = findUserOrThrow(username);
        return messageRepository.countTotalUnreadForUser(user.getId());
    }

    // ── Private: Participant validation ───────────────────────────────────────

    /**
     * Checks that the given user is either the rider or the bike owner
     * for this ride-share. Throws if not.
     */
    private void validateParticipant(RideShare rideShare, User user) {
        boolean isRider = rideShare.getRider().getId().equals(user.getId());
        boolean isOwner = rideShare.getOwnerId().equals(user.getId());

        if (!isRider && !isOwner) {
            throw new IllegalStateException(
                    "You are not a participant in this ride-share conversation");
        }
    }

    // ── Private: Notification helper ──────────────────────────────────────────

    /**
     * Sends an IN_APP notification to the other party when a new message arrives.
     * Uses IN_APP only (not email) to avoid flooding inboxes for chat messages.
     */
    private void notifyOtherParty(RideShare rideShare, User sender, String content) {
        try {
            // Determine the recipient — whoever is NOT the sender
            User recipient;
            boolean senderIsRider = rideShare.getRider().getId().equals(sender.getId());

            if (senderIsRider) {
                // Sender is the rider → notify the owner
                recipient = userRepository.findById(rideShare.getOwnerId()).orElse(null);
            } else {
                // Sender is the owner → notify the rider
                recipient = rideShare.getRider();
            }

            if (recipient == null) return;

            String senderName  = sender.getName() != null ? sender.getName() : sender.getUsername();
            String bikeName    = rideShare.getRental().getBike().getTitle();

            // Truncate long messages in the notification preview
            String preview = content.length() > 80
                    ? content.substring(0, 80) + "…"
                    : content;

            notificationService.sendNotification(
                    recipient,
                    NotificationType.NEW_MESSAGE,
                    NotificationChannel.IN_APP,
                    "New message from " + senderName,
                    "<strong>" + senderName + "</strong> sent you a message about " +
                            "<strong>" + bikeName + "</strong>:<br/>" +
                            "\"" + preview + "\"",
                    rideShare.getId(), "RIDE_SHARE"
            );
        } catch (Exception e) {
            log.error("Failed to send new-message notification: {}", e.getMessage());
        }
    }

    // ── Private: Entity finders ───────────────────────────────────────────────

    private User findUserOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + username));
    }

    private RideShare findRideShareOrThrow(Long id) {
        return rideShareRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ride-share not found: id=" + id));
    }

    // ── Private: Response mapper ──────────────────────────────────────────────

    /**
     * Maps a Message entity to MessageResponse.
     *
     * @param message      the entity
     * @param currentUserId the ID of the authenticated user (to set the "mine" flag)
     */
    private MessageResponse toResponse(Message message, Long currentUserId) {
        User sender = message.getSender();

        return MessageResponse.builder()
                .id(message.getId())
                .rideShareId(message.getRideShare().getId())
                // Sender details
                .senderId(sender.getId())
                .senderName(sender.getName())
                .senderUsername(sender.getUsername())
                .senderRole(sender.getRole().name())
                .senderProfileImage(sender.getProfileImage())
                // Content
                .content(message.getContent())
                // Read status
                .read(message.isRead())
                .readAt(message.getReadAt())
                // Timestamp
                .sentAt(message.getSentAt())
                // Convenience flag — true when this message was sent by the current user
                .mine(sender.getId().equals(currentUserId))
                .build();
    }
}
