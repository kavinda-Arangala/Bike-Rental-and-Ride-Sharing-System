package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.controller;

import jakarta.validation.Valid;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.RideShareRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.RideShareStatusRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.ApiResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.RideShareResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.RideShareService;
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
public class RideShareController {

    private final RideShareService rideShareService;

    // ── Rider endpoints ───────────────────────────────────────────────────────

    /**
     * POST /api/ride-shares
     * Rider submits a ride-share request for one of their own rentals.
     *
     * Request body:  { rentalId, pickupAddress, dropoffAddress }
     * Returns:       the created RideShareResponse with status PENDING
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('RIDER', 'BIKE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<RideShareResponse>> createRideShare(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RideShareRequest request) {

        RideShareResponse response =
                rideShareService.createRideShare(userDetails.getUsername(), request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Ride-share request submitted successfully", response));
    }

    /**
     * GET /api/ride-shares/my
     * Rider sees all their own ride-share requests (all statuses, newest first).
     */
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('RIDER', 'BIKE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<RideShareResponse>>> getMyRideShares(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(ApiResponse.success(
                "My ride-share requests",
                rideShareService.getMyRideShares(userDetails.getUsername())));
    }

    /**
     * GET /api/ride-shares/{id}
     * Get a single ride-share by ID.
     * Only the rider or the bike owner (participants) can access this.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RIDER', 'BIKE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<RideShareResponse>> getRideShareById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        return ResponseEntity.ok(ApiResponse.success(
                "Ride-share details",
                rideShareService.getRideShareById(userDetails.getUsername(), id)));
    }

    /**
     * PATCH /api/ride-shares/{id}/cancel
     * Rider cancels their own PENDING ride-share request.
     */
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('RIDER', 'BIKE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<RideShareResponse>> cancelRideShare(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        return ResponseEntity.ok(ApiResponse.success(
                "Ride-share request cancelled",
                rideShareService.cancelRideShare(userDetails.getUsername(), id)));
    }

    // ── Owner endpoints ───────────────────────────────────────────────────────

    /**
     * GET /api/ride-shares/owner
     * Bike owner sees ALL ride-share requests for their bikes (all statuses).
     * The owner is identified by their userId matching bike.ownerId.
     */
    @GetMapping("/owner")
    @PreAuthorize("hasAnyRole('RIDER', 'BIKE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<RideShareResponse>>> getOwnerRideShares(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(ApiResponse.success(
                "Ride-share requests for my bikes",
                rideShareService.getOwnerRideShares(userDetails.getUsername())));
    }

    /**
     * GET /api/ride-shares/owner/pending
     * Bike owner sees only PENDING requests that need their decision.
     */
    @GetMapping("/owner/pending")
    @PreAuthorize("hasAnyRole('RIDER', 'BIKE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<RideShareResponse>>> getOwnerPendingRideShares(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(ApiResponse.success(
                "Pending ride-share requests",
                rideShareService.getOwnerPendingRideShares(userDetails.getUsername())));
    }

    /**
     * PATCH /api/ride-shares/{id}/decide
     * Bike owner approves or rejects a PENDING ride-share request.
     *
     * Request body:  { action: "APPROVE" | "REJECT", rejectionReason?: "..." }
     * Returns:       updated RideShareResponse
     */
    @PatchMapping("/{id}/decide")
    @PreAuthorize("hasAnyRole('RIDER', 'BIKE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<RideShareResponse>> decideRideShare(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody RideShareStatusRequest request) {

        RideShareResponse response =
                rideShareService.decideRideShare(userDetails.getUsername(), id, request);

        String message = "APPROVE".equalsIgnoreCase(request.getAction())
                ? "Ride-share request approved successfully"
                : "Ride-share request rejected";

        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    // ── Admin endpoints ───────────────────────────────────────────────────────

    /**
     * GET /api/ride-shares/admin/all
     * Admin sees all ride-share requests in the system.
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<RideShareResponse>>> getAllRideShares() {

        return ResponseEntity.ok(ApiResponse.success(
                "All ride-share requests",
                rideShareService.getAllRideShares()));
    }

    /**
     * GET /api/ride-shares/admin/status/{status}
     * Admin filters ride-shares by status.
     * Valid values: PENDING, APPROVED, REJECTED, CANCELLED, COMPLETED
     */
    @GetMapping("/admin/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<RideShareResponse>>> getRideSharesByStatus(
            @PathVariable String status) {

        return ResponseEntity.ok(ApiResponse.success(
                "Ride-shares with status: " + status.toUpperCase(),
                rideShareService.getRideSharesByStatus(status)));
    }
}