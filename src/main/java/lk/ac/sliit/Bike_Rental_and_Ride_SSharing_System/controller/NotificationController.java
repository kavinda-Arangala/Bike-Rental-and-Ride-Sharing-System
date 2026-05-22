package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.controller;

import jakarta.validation.Valid;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.AdminNotificationRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.ApiResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.NotificationResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // â”€â”€ Rider + Admin endpoints â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * GET /api/notifications/my
     * Get all my notifications (newest first).
     */
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('RIDER', 'BIKE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<NotificationResponse> notifications =
                notificationService.getMyNotifications(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Notifications fetched", notifications));
    }

    /**
     * GET /api/notifications/my/unread
     * Get only unread notifications.
     */
    @GetMapping("/my/unread")
    @PreAuthorize("hasAnyRole('RIDER', 'BIKE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyUnreadNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<NotificationResponse> notifications =
                notificationService.getMyUnreadNotifications(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Unread notifications", notifications));
    }

    /**
     * GET /api/notifications/my/unread-count
     * Get unread notification count (used by the bell badge in frontend).
     */
    @GetMapping("/my/unread-count")
    @PreAuthorize("hasAnyRole('RIDER', 'BIKE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        long count = notificationService.getUnreadCount(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Unread count", count));
    }

    /**
     * PATCH /api/notifications/{id}/read
     * Mark a single notification as read.
     */
    @PatchMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('RIDER', 'BIKE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        NotificationResponse notification =
                notificationService.markAsRead(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", notification));
    }

    /**
     * PATCH /api/notifications/read-all
     * Mark all my notifications as read.
     */
    @PatchMapping("/read-all")
    @PreAuthorize("hasAnyRole('RIDER', 'BIKE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Integer>> markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails) {
        int count = notificationService.markAllAsRead(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(count + " notifications marked as read", count));
    }

    /**
     * DELETE /api/notifications/{id}
     * Delete my notification.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('RIDER', 'BIKE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteMyNotification(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        notificationService.deleteMyNotification(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted", null));
    }

    // â”€â”€ Admin-only endpoints â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * GET /api/notifications/admin/all
     * Get ALL notifications across all users.
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getAllNotifications() {
        List<NotificationResponse> notifications = notificationService.getAllNotifications();
        return ResponseEntity.ok(ApiResponse.success("All notifications", notifications));
    }

    /**
     * POST /api/notifications/admin/send
     * Send a manual notification to one user or broadcast to all.
     * If userId is null â†’ sends to ALL users.
     */
    @PostMapping("/admin/send")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> adminSendNotification(
            @Valid @RequestBody AdminNotificationRequest request) {
        notificationService.adminSendNotification(request);
        String msg = request.getUserId() != null
                ? "Notification sent to user " + request.getUserId()
                : "Notification broadcast to all users";
        return ResponseEntity.ok(ApiResponse.success(msg, null));
    }
}
