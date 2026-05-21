package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // ── Fetch messages ────────────────────────────────────────────────────────

    /**
     * All messages in a ride-share chat, oldest first (chronological order).
     */
    List<Message> findByRideShareIdOrderBySentAtAsc(Long rideShareId);

    /**
     * Only unread messages in a chat — sent by the OTHER party (not by userId).
     * Used to mark messages as read when a user opens the chat.
     */
    @Query("SELECT m FROM Message m WHERE m.rideShare.id = :rideShareId " +
            "AND m.sender.id != :userId AND m.read = false " +
            "ORDER BY m.sentAt ASC")
    List<Message> findUnreadByRideShareIdForUser(
            @Param("rideShareId") Long rideShareId,
            @Param("userId") Long userId);

    // ── Unread counts ─────────────────────────────────────────────────────────

    /**
     * Count unread messages in a chat that were NOT sent by userId.
     * Powers the notification badge on the chat list.
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.rideShare.id = :rideShareId " +
            "AND m.sender.id != :userId AND m.read = false")
    long countUnreadByRideShareIdForUser(
            @Param("rideShareId") Long rideShareId,
            @Param("userId") Long userId);

    /**
     * Total unread messages across ALL of a user's ride-share chats.
     * Used for a global unread badge in the navigation bar.
     */
    @Query("SELECT COUNT(m) FROM Message m " +
            "WHERE (m.rideShare.rider.id = :userId OR m.rideShare.ownerId = :userId) " +
            "AND m.sender.id != :userId AND m.read = false")
    long countTotalUnreadForUser(@Param("userId") Long userId);

    // ── Mark as read ──────────────────────────────────────────────────────────

    /**
     * Mark all unread messages in a chat as read for a user (when they open the chat).
     * Only marks messages sent by the OTHER party.
     */
    @Modifying
    @Query("UPDATE Message m SET m.read = true, m.readAt = CURRENT_TIMESTAMP " +
            "WHERE m.rideShare.id = :rideShareId " +
            "AND m.sender.id != :userId AND m.read = false")
    int markAllAsReadForUser(
            @Param("rideShareId") Long rideShareId,
            @Param("userId") Long userId);

    // ── Admin / stats ─────────────────────────────────────────────────────────

    /**
     * Total message count for a ride-share — admin detail view.
     */
    long countByRideShareId(Long rideShareId);
}