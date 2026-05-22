package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.controller;

import jakarta.validation.Valid;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.MessageRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.ApiResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.MessageResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ride-shares")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    // ── Chat endpoints (scoped to a ride-share) ───────────────────────────────

    /**
     * POST /api/ride-shares/{rideShareId}/messages
     * Send a message in a ride-share chat.
     *
     * Rules enforced by the service:
     *   - Caller must be a participant (rider or bike owner)
     *   - Ride-share status must be APPROVED
     *
     * Request body:  { content: "Hello!" }
     * Returns:       the saved MessageResponse
     */
    @PostMapping("/{rideShareId}/messages")
    @PreAuthorize("hasAnyRole('RIDER', 'BIKE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long rideShareId,
            @Valid @RequestBody MessageRequest request) {

        MessageResponse response =
                messageService.sendMessage(userDetails.getUsername(), rideShareId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Message sent", response));
    }

    /**
     * GET /api/ride-shares/{rideShareId}/messages
     * Fetch all messages in a ride-share chat (oldest first).
     *
     * Side effect: automatically marks all unread messages from the other
     * party as read, so the unread badge resets after opening the chat.
     *
     * Only participants (rider + owner) can access this.
     */
    @GetMapping("/{rideShareId}/messages")
    @PreAuthorize("hasAnyRole('RIDER', 'BIKE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getMessages(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long rideShareId) {

        return ResponseEntity.ok(ApiResponse.success(
                "Chat messages",
                messageService.getMessages(userDetails.getUsername(), rideShareId)));
    }

    /**
     * GET /api/ride-shares/{rideShareId}/messages/unread-count
     * Get the number of unread messages in a specific chat for the current user.
     *
     * Use this to show a badge on a single conversation card
     * without loading all messages.
     */
    @GetMapping("/{rideShareId}/messages/unread-count")
    @PreAuthorize("hasAnyRole('RIDER', 'BIKE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long rideShareId) {

        long count = messageService.getUnreadCount(
                userDetails.getUsername(), rideShareId);

        return ResponseEntity.ok(ApiResponse.success("Unread message count", count));
    }

    // ── Global unread count (nav-bar badge) ───────────────────────────────────

    /**
     * GET /api/ride-shares/messages/unread-total
     * Total unread messages across ALL of the current user's ride-share chats.
     *
     * Call this on page load to show the global chat badge in the nav bar.
     * Re-poll every 15–30 seconds from the frontend for a near-real-time feel.
     */
    @GetMapping("/messages/unread-total")
    @PreAuthorize("hasAnyRole('RIDER', 'BIKE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Long>> getTotalUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails) {

        long count = messageService.getTotalUnreadCount(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Total unread messages", count));
    }
}