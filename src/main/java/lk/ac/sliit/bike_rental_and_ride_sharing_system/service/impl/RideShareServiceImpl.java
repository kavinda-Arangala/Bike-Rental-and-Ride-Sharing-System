package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.impl;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.RideShareRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.RideShareStatusRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.RideShareResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.Bike;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.Rental;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.RideShare;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.User;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.NotificationChannel;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.NotificationType;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.RentalStatus;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.RideShareStatus;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.exception.ResourceNotFoundException;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.MessageRepository;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.RentalRepository;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.RideShareRepository;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.UserRepository;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.NotificationService;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.RideShareService;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RideShareServiceImpl implements RideShareService {

    private final RideShareRepository rideShareRepository;
    private final RentalRepository    rentalRepository;
    private final UserRepository      userRepository;
    private final MessageRepository   messageRepository;
    private final NotificationService notificationService;
    private final SecurityUtil        securityUtil;

    // ── Rider: Create Ride-Share Request ──────────────────────────────────────

    @Override
    @Transactional
    public RideShareResponse createRideShare(String username, RideShareRequest request) {
        User rider  = findUserOrThrow(username);
        Rental rental = findRentalOrThrow(request.getRentalId());

        // Must be the rental owner
        if (!rental.getUser().getUsername().equals(username)) {
            throw new IllegalStateException(
                    "You can only create a ride-share request for your own rental");
        }

        // Rental must be PENDING or ACTIVE
        if (rental.getStatus() != RentalStatus.PENDING &&
                rental.getStatus() != RentalStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Ride-share requests can only be made for PENDING or ACTIVE rentals. " +
                            "Current status: " + rental.getStatus());
        }

        // One ride-share per rental
        if (rideShareRepository.existsByRentalId(rental.getId())) {
            throw new IllegalStateException(
                    "A ride-share request already exists for this rental");
        }

        Bike bike = rental.getBike();

        RideShare rideShare = RideShare.builder()
                .rental(rental)
                .rider(rider)
                .ownerId(bike.getOwnerId())
                .pickupAddress(request.getPickupAddress())
                .dropoffAddress(request.getDropoffAddress())
                .status(RideShareStatus.PENDING)
                .build();

        RideShare saved = rideShareRepository.save(rideShare);
        log.info("RideShare created: id={}, rentalId={}, riderId={}",
                saved.getId(), rental.getId(), rider.getId());

        // Notify the bike owner that a new request arrived
        notifyOwnerOfNewRequest(saved, bike);

        return toResponse(saved, rider.getId());
    }

    // ── Rider: Get My Ride-Shares ─────────────────────────────────────────────

    @Override
    public List<RideShareResponse> getMyRideShares(String username) {
        User rider = findUserOrThrow(username);
        return rideShareRepository
                .findByRiderIdOrderByCreatedAtDesc(rider.getId())
                .stream()
                .map(rs -> toResponse(rs, rider.getId()))
                .toList();
    }

    // ── Rider / Owner: Get Single Ride-Share ──────────────────────────────────

    @Override
    public RideShareResponse getRideShareById(String username, Long rideShareId) {
        User user     = findUserOrThrow(username);
        RideShare rs  = findRideShareAsParticipantOrThrow(rideShareId, user.getId());
        return toResponse(rs, user.getId());
    }

    // ── Rider: Cancel Pending Request ─────────────────────────────────────────

    @Override
    @Transactional
    public RideShareResponse cancelRideShare(String username, Long rideShareId) {
        User rider   = findUserOrThrow(username);
        RideShare rs = findRideShareOrThrow(rideShareId);

        // Only the rider who created it can cancel
        if (!rs.getRider().getId().equals(rider.getId())) {
            throw new IllegalStateException(
                    "You can only cancel your own ride-share requests");
        }

        // Can only cancel if still PENDING
        if (rs.getStatus() != RideShareStatus.PENDING) {
            throw new IllegalStateException(
                    "Only PENDING ride-share requests can be cancelled. " +
                            "Current status: " + rs.getStatus());
        }

        rs.setStatus(RideShareStatus.CANCELLED);
        rs.setDecidedAt(LocalDateTime.now());
        RideShare saved = rideShareRepository.save(rs);

        log.info("RideShare cancelled by rider: id={}, riderId={}", rideShareId, rider.getId());

        // Notify owner that rider cancelled
        notifyOwnerOfCancellation(saved);

        return toResponse(saved, rider.getId());
    }

    // ── Owner: Get All Ride-Shares For My Bikes ───────────────────────────────

    @Override
    public List<RideShareResponse> getOwnerRideShares(String username) {
        User owner = findUserOrThrow(username);
        return rideShareRepository
                .findByOwnerIdOrderByCreatedAtDesc(owner.getId())
                .stream()
                .map(rs -> toResponse(rs, owner.getId()))
                .toList();
    }

    // ── Owner: Get Only Pending Requests ──────────────────────────────────────

    @Override
    public List<RideShareResponse> getOwnerPendingRideShares(String username) {
        User owner = findUserOrThrow(username);
        return rideShareRepository
                .findByOwnerIdAndStatusOrderByCreatedAtDesc(owner.getId(), RideShareStatus.PENDING)
                .stream()
                .map(rs -> toResponse(rs, owner.getId()))
                .toList();
    }

    // ── Owner: Approve or Reject ──────────────────────────────────────────────

    @Override
    @Transactional
    public RideShareResponse decideRideShare(String username, Long rideShareId,
                                             RideShareStatusRequest request) {
        User owner   = findUserOrThrow(username);
        RideShare rs = findRideShareOrThrow(rideShareId);

        // Only the bike owner can decide
        if (!rs.getOwnerId().equals(owner.getId())) {
            throw new IllegalStateException(
                    "You are not the owner of the bike in this ride-share request");
        }

        // Can only decide on PENDING requests
        if (rs.getStatus() != RideShareStatus.PENDING) {
            throw new IllegalStateException(
                    "Only PENDING ride-share requests can be approved or rejected. " +
                            "Current status: " + rs.getStatus());
        }

        String action = request.getAction().toUpperCase();

        if ("APPROVE".equals(action)) {
            rs.setStatus(RideShareStatus.APPROVED);
            rs.setDecidedAt(LocalDateTime.now());
            log.info("RideShare APPROVED: id={}, ownerId={}", rideShareId, owner.getId());
            notifyRiderOfApproval(rs);

        } else if ("REJECT".equals(action)) {
            if (request.getRejectionReason() == null ||
                    request.getRejectionReason().isBlank()) {
                throw new IllegalArgumentException(
                        "Rejection reason is required when rejecting a request");
            }
            rs.setStatus(RideShareStatus.REJECTED);
            rs.setRejectionReason(request.getRejectionReason());
            rs.setDecidedAt(LocalDateTime.now());
            log.info("RideShare REJECTED: id={}, ownerId={}", rideShareId, owner.getId());
            notifyRiderOfRejection(rs);
        }

        return toResponse(rideShareRepository.save(rs), owner.getId());
    }

    // ── Admin: Get All ────────────────────────────────────────────────────────

    @Override
    public List<RideShareResponse> getAllRideShares() {
        return rideShareRepository.findAll()
                .stream()
                .map(rs -> toResponse(rs, null))
                .toList();
    }

    // ── Admin: Filter By Status ───────────────────────────────────────────────

    @Override
    public List<RideShareResponse> getRideSharesByStatus(String status) {
        RideShareStatus rideShareStatus;
        try {
            rideShareStatus = RideShareStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid status. Must be: PENDING, APPROVED, REJECTED, CANCELLED, or COMPLETED");
        }
        return rideShareRepository
                .findByStatusOrderByCreatedAtDesc(rideShareStatus)
                .stream()
                .map(rs -> toResponse(rs, null))
                .toList();
    }

    // ── Private: Notification helpers ─────────────────────────────────────────

    private void notifyOwnerOfNewRequest(RideShare rs, Bike bike) {
        try {
            User ownerUser = userRepository.findById(rs.getOwnerId()).orElse(null);
            if (ownerUser == null) return;

            String riderName = rs.getRider().getName() != null
                    ? rs.getRider().getName() : rs.getRider().getUsername();

            notificationService.sendNotification(
                    ownerUser,
                    NotificationType.RIDE_SHARE_REQUESTED,
                    NotificationChannel.BOTH,
                    "New Ride-Share Request 🚲",
                    "<strong>" + riderName + "</strong> has requested to ride your bike " +
                            "<strong>" + bike.getTitle() + "</strong>.<br/>" +
                            "📍 Pickup: " + rs.getPickupAddress() + "<br/>" +
                            "📍 Drop-off: " + rs.getDropoffAddress() + "<br/><br/>" +
                            "Please review and approve or reject the request.",
                    rs.getId(), "RIDE_SHARE"
            );
        } catch (Exception e) {
            log.error("Failed to notify owner of new ride-share request: {}", e.getMessage());
        }
    }

    private void notifyRiderOfApproval(RideShare rs) {
        try {
            String bikeName = rs.getRental().getBike().getTitle();
            notificationService.sendNotification(
                    rs.getRider(),
                    NotificationType.RIDE_SHARE_APPROVED,
                    NotificationChannel.BOTH,
                    "Ride-Share Request Approved ✅",
                    "Your ride-share request for <strong>" + bikeName + "</strong> " +
                            "has been <strong>approved</strong> by the owner!<br/><br/>" +
                            "You can now chat with the owner directly in the system.",
                    rs.getId(), "RIDE_SHARE"
            );
        } catch (Exception e) {
            log.error("Failed to notify rider of approval: {}", e.getMessage());
        }
    }

    private void notifyRiderOfRejection(RideShare rs) {
        try {
            String bikeName = rs.getRental().getBike().getTitle();
            notificationService.sendNotification(
                    rs.getRider(),
                    NotificationType.RIDE_SHARE_REJECTED,
                    NotificationChannel.BOTH,
                    "Ride-Share Request Rejected ❌",
                    "Your ride-share request for <strong>" + bikeName + "</strong> " +
                            "was <strong>rejected</strong> by the owner.<br/>" +
                            "📝 Reason: " + rs.getRejectionReason() + "<br/><br/>" +
                            "You may browse other available bikes.",
                    rs.getId(), "RIDE_SHARE"
            );
        } catch (Exception e) {
            log.error("Failed to notify rider of rejection: {}", e.getMessage());
        }
    }

    private void notifyOwnerOfCancellation(RideShare rs) {
        try {
            User ownerUser = userRepository.findById(rs.getOwnerId()).orElse(null);
            if (ownerUser == null) return;

            String riderName = rs.getRider().getName() != null
                    ? rs.getRider().getName() : rs.getRider().getUsername();
            String bikeName  = rs.getRental().getBike().getTitle();

            notificationService.sendNotification(
                    ownerUser,
                    NotificationType.RIDE_SHARE_CANCELLED,
                    NotificationChannel.IN_APP,
                    "Ride-Share Request Cancelled",
                    "<strong>" + riderName + "</strong> has cancelled their ride-share " +
                            "request for your bike <strong>" + bikeName + "</strong>.",
                    rs.getId(), "RIDE_SHARE"
            );
        } catch (Exception e) {
            log.error("Failed to notify owner of cancellation: {}", e.getMessage());
        }
    }

    // ── Private: Entity finders ───────────────────────────────────────────────

    private User findUserOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + username));
    }

    private Rental findRentalOrThrow(Long id) {
        return rentalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Rental not found: id=" + id));
    }

    private RideShare findRideShareOrThrow(Long id) {
        return rideShareRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ride-share not found: id=" + id));
    }

    private RideShare findRideShareAsParticipantOrThrow(Long rideShareId, Long userId) {
        return rideShareRepository.findByIdAndParticipant(rideShareId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ride-share not found or you are not a participant: id=" + rideShareId));
    }

    // ── Private: Response mapper ──────────────────────────────────────────────

    /**
     * Maps a RideShare entity to RideShareResponse.
     *
     * @param rs     the entity
     * @param userId the current user's ID — used to calculate unread message count.
     *               Pass null for admin views where unread count is not relevant.
     */
    private RideShareResponse toResponse(RideShare rs, Long userId) {
        Rental rental = rs.getRental();
        Bike   bike   = rental.getBike();
        User   rider  = rs.getRider();

        long unread = (userId != null)
                ? messageRepository.countUnreadByRideShareIdForUser(rs.getId(), userId)
                : 0L;

        return RideShareResponse.builder()
                // Ride-share core
                .id(rs.getId())
                .status(rs.getStatus().name())
                .pickupAddress(rs.getPickupAddress())
                .dropoffAddress(rs.getDropoffAddress())
                .rejectionReason(rs.getRejectionReason())
                .decidedAt(rs.getDecidedAt())
                .createdAt(rs.getCreatedAt())
                .updatedAt(rs.getUpdatedAt())
                // Rental
                .rentalId(rental.getId())
                .rentalStatus(rental.getStatus().name())
                .plannedStartTime(rental.getPlannedStartTime())
                .plannedEndTime(rental.getPlannedEndTime())
                .estimatedFare(rental.getEstimatedFare())
                .finalFare(rental.getFinalFare())
                // Bike
                .bikeId(bike.getId())
                .bikeTitle(bike.getTitle())
                .bikeType(bike.getBikeType().name())
                .bikeLocation(bike.getLocation())
                .bikePhotoUrl(bike.getPhotoUrl())
                // Rider details
                .riderId(rider.getId())
                .riderName(rider.getName())
                .riderUsername(rider.getUsername())
                .riderEmail(rider.getEmail())
                .riderPhone(rider.getPhoneNumber())
                .riderProfileImage(rider.getProfileImage())
                // Owner info (from bike)
                .ownerId(bike.getOwnerId())
                .ownerName(bike.getOwnerName())
                .ownerPhone(bike.getOwnerPhone())
                // Unread message badge
                .unreadMessageCount(unread)
                .build();
    }
}