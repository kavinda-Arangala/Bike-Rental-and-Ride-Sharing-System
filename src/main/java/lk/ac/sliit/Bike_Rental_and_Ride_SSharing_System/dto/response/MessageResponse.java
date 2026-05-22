package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MessageResponse {

    private Long id;
    private Long rideShareId;

    // ── Sender info ───────────────────────────────────────────────────────────
    private Long senderId;
    private String senderName;
    private String senderUsername;
    private String senderRole;         // "RIDER" or "ADMIN" (owner)
    private String senderProfileImage;

    // ── Message content ───────────────────────────────────────────────────────
    private String content;

    // ── Read status ───────────────────────────────────────────────────────────
    private boolean read;
    private LocalDateTime readAt;

    // ── Timestamp ─────────────────────────────────────────────────────────────
    private LocalDateTime sentAt;

    /**
     * Convenience flag — true when the message was sent by the currently
     * authenticated user. Useful for frontend to align messages left/right.
     * Set by the service layer based on the current principal.
     */
    private boolean mine;
}