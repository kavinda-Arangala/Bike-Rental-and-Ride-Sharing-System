package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.RideShareRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.RideShareStatusRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.RideShareResponse;

import java.util.List;

public interface RideShareService {

    // ── Rider operations ──────────────────────────────────────────────────────

    /**
     * Rider submits a ride-share request for one of their own rentals.
     */
    RideShareResponse createRideShare(String username, RideShareRequest request);

    /**
     * Rider views all their own ride-share requests.
     */
    List<RideShareResponse> getMyRideShares(String username);

    /**
     * Rider views a single ride-share by ID (must be a participant).
     */
    RideShareResponse getRideShareById(String username, Long rideShareId);

    /**
     * Rider cancels their own PENDING request.
     */
    RideShareResponse cancelRideShare(String username, Long rideShareId);

    // ── Owner operations ──────────────────────────────────────────────────────

    /**
     * Owner sees all ride-share requests for their bikes (all statuses).
     */
    List<RideShareResponse> getOwnerRideShares(String username);

    /**
     * Owner sees only PENDING requests awaiting their decision.
     */
    List<RideShareResponse> getOwnerPendingRideShares(String username);

    /**
     * Owner approves or rejects a ride-share request.
     */
    RideShareResponse decideRideShare(String username, Long rideShareId,
                                      RideShareStatusRequest request);

    // ── Admin operations ──────────────────────────────────────────────────────

    /**
     * Admin sees all ride-share requests in the system.
     */
    List<RideShareResponse> getAllRideShares();

    /**
     * Admin filters ride-shares by status.
     */
    List<RideShareResponse> getRideSharesByStatus(String status);
}