package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.MessageRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.MessageResponse;

import java.util.List;

public interface MessageService {

    /**
     * Send a message in a ride-share chat.
     * Only the rider and bike owner (participants) can send messages.
     * Chat is only open when ride-share status is APPROVED.
     */
    MessageResponse sendMessage(String username, Long rideShareId, MessageRequest request);

    /**
     * Get all messages in a ride-share chat (oldest first).
     * Also marks all unread messages from the other party as read.
     * Only participants can access this.
     */
    List<MessageResponse> getMessages(String username, Long rideShareId);

    /**
     * Count unread messages in a single chat for the current user.
     * Used to show an unread badge on a specific conversation.
     */
    long getUnreadCount(String username, Long rideShareId);

    /**
     * Count total unread messages across ALL of a user's chats.
     * Used for the global chat notification badge in the nav bar.
     */
    long getTotalUnreadCount(String username);
}