package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.impl;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.RentalCancelRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.RentalRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.RentalReviewRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.CancellationResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.RentalResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.Bike;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.Rental;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.RideShare;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.User;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.BikeStatus;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.PaymentStatus;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.RentalStatus;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.RideShareStatus;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.exception.BikeNotAvailableException;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.exception.ResourceNotFoundException;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.BikeRepository;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.PaymentRepository;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.RentalRepository;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.RideShareRepository;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.UserRepository;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.RentalService;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.util.RentalPolicy;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RentalServiceImpl implements RentalService {

    private final RentalRepository rentalRepository;
    private final BikeRepository   bikeRepository;
    private final PaymentRepository paymentRepository;
    private final RideShareRepository rideShareRepository;
    private final UserRepository   userRepository;
    private final SecurityUtil     securityUtil;

    // ── Create Rental ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public RentalResponse createRental(String username, RentalRequest request) {

        LocalDateTime now   = LocalDateTime.now();
        LocalDateTime start = request.getPlannedStartTime();
        LocalDateTime end   = request.getPlannedEndTime();

        // Must book at least 30 minutes ahead
        if (ChronoUnit.MINUTES.between(now, start) < RentalPolicy.MIN_BOOKING_LEAD_MINUTES) {
            throw new IllegalStateException(
                    "Booking must be made at least " +
                            RentalPolicy.MIN_BOOKING_LEAD_MINUTES + " minutes in advance");
        }

        if (!end.isAfter(start)) {
            throw new IllegalStateException("End time must be after start time");
        }

        long hours = ChronoUnit.HOURS.between(start, end);
        if (hours < RentalPolicy.MIN_RENTAL_HOURS) {
            throw new IllegalStateException(
                    "Minimum rental duration is " + RentalPolicy.MIN_RENTAL_HOURS + " hour(s)");
        }

        long days = ChronoUnit.DAYS.between(start, end);
        if (days > RentalPolicy.MAX_RENTAL_DAYS) {
            throw new IllegalStateException(
                    "Maximum rental duration is " + RentalPolicy.MAX_RENTAL_DAYS + " days");
        }

        long advanceDays = ChronoUnit.DAYS.between(now, start);
        if (advanceDays > RentalPolicy.MAX_ADVANCE_BOOKING_DAYS) {
            throw new IllegalStateException(
                    "Cannot book more than " +
                            RentalPolicy.MAX_ADVANCE_BOOKING_DAYS + " days in advance");
        }

        // ── Fixed: lookup by username, not email ──────────────────────────────
        User user = findUserOrThrow(username);
        Bike bike = findBikeOrThrow(request.getBikeId());

        if (bike.getStatus() != BikeStatus.AVAILABLE) {
            throw new BikeNotAvailableException("Bike is not available: " + bike.getTitle());
        }

        // ── Fixed: pass enum params to overlap query ───────────────────────────
        if (rentalRepository.existsOverlappingRental(
                bike.getId(), start, end,
                EnumSet.of(RentalStatus.PENDING, RentalStatus.PAID, RentalStatus.CONFIRMED, RentalStatus.ACTIVE))) {
            throw new BikeNotAvailableException("Bike already booked for the selected period");
        }

        BigDecimal estimatedFare = calculateFare(bike, start, end);

        Rental rental = Rental.builder()
                .user(user)
                .bike(bike)
                .plannedStartTime(start)
                .plannedEndTime(end)
                .dailyRate(bike.getDailyPrice())
                .estimatedFare(estimatedFare)
                .status(RentalStatus.PENDING)
                .build();

        Rental saved = rentalRepository.save(rental);
        ensureRentalChat(saved);
        log.info("Rental created: id={}, user={}, bike={}", saved.getId(), username, bike.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public RentalResponse confirmRental(String username, Long rentalId) {
        Rental rental = findRentalOrThrow(rentalId);
        validateOwnerOrAdminAccess(rental, username);

        if (rental.getStatus() != RentalStatus.PAID) {
            throw new IllegalStateException(
                    "Only PAID rentals can be confirmed. Current: " + rental.getStatus());
        }
        if (!paymentRepository.existsByRentalIdAndStatus(rental.getId(), PaymentStatus.SUCCESS)) {
            throw new IllegalStateException("Successful payment is required before owner confirmation");
        }

        rental.setStatus(RentalStatus.CONFIRMED);
        log.info("Rental confirmed by owner/admin: id={}, username={}", rentalId, username);
        return toResponse(rentalRepository.save(rental));
    }

    // ── Start Rental ──────────────────────────────────────────────────────────

    @Override
    @Transactional
    public RentalResponse startRental(String username, Long rentalId) {
        Rental rental = findRentalOrThrow(rentalId);
        validateRentalAccess(rental, username);

        if (rental.getStatus() != RentalStatus.CONFIRMED) {
            throw new IllegalStateException(
                    "Bike owner must confirm the paid rental before it can be started. Current: " + rental.getStatus());
        }
        if (!paymentRepository.existsByRentalIdAndStatus(rental.getId(), PaymentStatus.SUCCESS)) {
            throw new IllegalStateException("Payment must be completed before starting the ride");
        }

        rental.setStatus(RentalStatus.ACTIVE);
        rental.setStartTime(LocalDateTime.now());
        rental.getBike().setStatus(BikeStatus.RENTED);

        bikeRepository.save(rental.getBike());
        log.info("Rental started: id={}", rentalId);
        return toResponse(rentalRepository.save(rental));
    }

    // ── Complete Rental ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public RentalResponse completeRental(String username, Long rentalId,
                                         BigDecimal distanceKm) {
        Rental rental = findRentalOrThrow(rentalId);
        validateRentalAccess(rental, username);

        if (rental.getStatus() != RentalStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Only ACTIVE rentals can be completed. Current: " + rental.getStatus());
        }

        if (distanceKm != null && distanceKm.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Distance cannot be negative");
        }

        LocalDateTime endTime = LocalDateTime.now();
        rental.setEndTime(endTime);
        rental.setDistanceKm(distanceKm != null ? distanceKm : BigDecimal.ZERO);
        rental.setStatus(RentalStatus.COMPLETED);
        rental.setFinalFare(calculateFare(rental.getBike(), rental.getStartTime(), endTime));

        Bike bike = rental.getBike();
        bike.setStatus(BikeStatus.AVAILABLE);
        bike.setTotalRentals(bike.getTotalRentals() != null ? bike.getTotalRentals() + 1 : 1);
        bikeRepository.save(bike);

        log.info("Rental completed: id={}, fare={}", rentalId, rental.getFinalFare());
        return toResponse(rentalRepository.save(rental));
    }

    // ── Cancel Rental ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public CancellationResponse cancelRental(String username, Long rentalId,
                                             RentalCancelRequest request) {
        Rental rental = findRentalOrThrow(rentalId);
        validateRentalAccess(rental, username);

        if (rental.getStatus() == RentalStatus.COMPLETED ||
                rental.getStatus() == RentalStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Cannot cancel a " + rental.getStatus() + " rental");
        }

        LocalDateTime now = LocalDateTime.now();
        long hoursUntilStart = ChronoUnit.HOURS.between(now, rental.getPlannedStartTime());

        if (EnumSet.of(RentalStatus.PENDING, RentalStatus.PAID, RentalStatus.CONFIRMED).contains(rental.getStatus()) &&
                hoursUntilStart < RentalPolicy.NO_CANCEL_HOURS) {
            throw new IllegalStateException(
                    "Cancellation not allowed within " +
                            RentalPolicy.NO_CANCEL_HOURS + " hour(s) of start time.");
        }

        boolean freeCancellation = hoursUntilStart >= RentalPolicy.FREE_CANCEL_HOURS
                || rental.getStatus() == RentalStatus.ACTIVE;

        BigDecimal estimatedFare = rental.getEstimatedFare() != null
                ? rental.getEstimatedFare() : BigDecimal.ZERO;

        BigDecimal cancellationFee;
        BigDecimal refundAmount;
        String policyMessage;

        if (freeCancellation) {
            cancellationFee = BigDecimal.ZERO;
            refundAmount    = estimatedFare;
            policyMessage   = "Free cancellation applied. Full refund will be processed.";
        } else {
            cancellationFee = estimatedFare
                    .multiply(BigDecimal.valueOf(RentalPolicy.LATE_CANCEL_FEE_PCT))
                    .setScale(2, RoundingMode.HALF_UP);
            refundAmount = estimatedFare
                    .subtract(cancellationFee)
                    .setScale(2, RoundingMode.HALF_UP);
            policyMessage = "Late cancellation fee of " +
                    (int)(RentalPolicy.LATE_CANCEL_FEE_PCT * 100) +
                    "% applied. Refund: " + refundAmount;
        }

        if (rental.getStatus() == RentalStatus.ACTIVE) {
            rental.getBike().setStatus(BikeStatus.AVAILABLE);
            bikeRepository.save(rental.getBike());
        }

        rental.setStatus(RentalStatus.CANCELLED);
        rental.setCancellationReason(request.getReason());
        rental.setCancelledAt(now);
        rental.setCancellationFee(cancellationFee);
        rental.setRefundAmount(refundAmount);
        rentalRepository.save(rental);

        log.info("Rental cancelled: id={}, fee={}, refund={}", rentalId, cancellationFee, refundAmount);

        return CancellationResponse.builder()
                .rentalId(rental.getId())
                .status(RentalStatus.CANCELLED.name())
                .freeCancellation(freeCancellation)
                .cancellationFee(cancellationFee)
                .refundAmount(refundAmount)
                .cancellationReason(request.getReason())
                .policyMessage(policyMessage)
                .cancelledAt(now)
                .build();
    }

    // ── Submit Review ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public RentalResponse submitReview(String username, Long rentalId,
                                       RentalReviewRequest request) {
        Rental rental = findRentalOrThrow(rentalId);
        validateRentalAccess(rental, username);

        if (rental.getStatus() != RentalStatus.COMPLETED) {
            throw new IllegalStateException("Can only review completed rentals");
        }
        if (rental.getRating() != null) {
            throw new IllegalStateException("Review already submitted for this rental");
        }

        rental.setRating(request.getRating());
        rental.setReview(request.getReview());
        rental.setOwnerServiceRating(request.getOwnerServiceRating());
        rental.setOwnerServiceReview(request.getOwnerServiceReview());
        rental.setReviewedAt(LocalDateTime.now());

        updateBikeRating(rental.getBike(), request.getRating());

        log.info("Review submitted: rentalId={}, rating={}", rentalId, request.getRating());
        return toResponse(rentalRepository.save(rental));
    }

    // ── Query Operations ──────────────────────────────────────────────────────

    @Override
    public RentalResponse getRentalById(String username, Long rentalId) {
        Rental rental = findRentalOrThrow(rentalId);
        validateRentalAccess(rental, username);
        return toResponse(rental);
    }

    @Override
    public List<RentalResponse> getMyRentals(String username) {
        User user = findUserOrThrow(username);
        return rentalRepository.findByUserId(user.getId())
                .stream().map(this::toResponse).toList();
    }

    @Override
    public List<RentalResponse> getMyActiveRentals(String username) {
        User user = findUserOrThrow(username);
        return rentalRepository.findByUserIdAndStatus(user.getId(), RentalStatus.ACTIVE)
                .stream().map(this::toResponse).toList();
    }

    @Override
    public List<RentalResponse> getOwnerRentals(String username) {
        User owner = findUserOrThrow(username);
        return rentalRepository.findByBikeOwnerId(owner.getId())
                .stream().map(this::toResponse).toList();
    }

    @Override
    public List<RentalResponse> getAllRentals() {
        return rentalRepository.findAll()
                .stream().map(this::toResponse).toList();
    }

    @Override
    public List<RentalResponse> getRentalsByStatus(String status) {
        RentalStatus rentalStatus;
        try {
            rentalStatus = RentalStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid rental status: " + status);
        }
        return rentalRepository.findByStatus(rentalStatus)
                .stream().map(this::toResponse).toList();
    }

    @Override
    public List<RentalResponse> getRentalsByBike(Long bikeId) {
        return rentalRepository.findByBikeId(bikeId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    public List<RentalResponse> getRentalsByUser(Long userId) {
        return rentalRepository.findByUserId(userId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    public BigDecimal getTotalRevenue() {
        // ── Fixed: pass enum param instead of relying on string literal ───────
        return rentalRepository.calculateTotalRevenue(RentalStatus.COMPLETED);
    }

    @Override
    public BigDecimal getRevenueBetween(LocalDateTime start, LocalDateTime end) {
        return rentalRepository.calculateRevenueBetween(start, end, RentalStatus.COMPLETED);
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    /**
     * Fixed: compare username (from JWT) not email.
     */
    private void validateRentalAccess(Rental rental, String username) {
        if (securityUtil.isAdmin() || rental.getUser().getUsername().equals(username)) {
            return;
        }
        User user = findUserOrThrow(username);
        if (!isBikeOwner(rental, user)) {
            throw new IllegalStateException("You are not authorized to access this rental");
        }
    }

    private void validateOwnerOrAdminAccess(Rental rental, String username) {
        if (securityUtil.isAdmin()) {
            return;
        }
        User user = findUserOrThrow(username);
        if (!isBikeOwner(rental, user)) {
            throw new IllegalStateException("Only this bike owner can confirm the rental");
        }
    }

    private boolean isBikeOwner(Rental rental, User user) {
        return rental.getBike().getOwnerId() != null &&
                rental.getBike().getOwnerId().equals(user.getId());
    }

    /**
     * Fixed: lookup by username, not email.
     */
    private User findUserOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + username));
    }

    private Bike findBikeOrThrow(Long id) {
        return bikeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bike not found: id=" + id));
    }

    private Rental findRentalOrThrow(Long id) {
        return rentalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rental not found: id=" + id));
    }

    private BigDecimal calculateFare(Bike bike, LocalDateTime start, LocalDateTime end) {
        long minutes = ChronoUnit.MINUTES.between(start, end);
        BigDecimal days = BigDecimal.valueOf(minutes)
                .divide(BigDecimal.valueOf(1440), 4, RoundingMode.HALF_UP);
        return bike.getDailyPrice()
                .multiply(days)
                .setScale(2, RoundingMode.HALF_UP)
                .max(BigDecimal.valueOf(50));
    }

    private void updateBikeRating(Bike bike, int newRating) {
        int total    = bike.getTotalRentals() != null ? bike.getTotalRentals() : 1;
        double current = bike.getAverageRating() != null ? bike.getAverageRating() : 0.0;
        double updated = ((current * (total - 1)) + newRating) / total;
        bike.setAverageRating(Math.round(updated * 10.0) / 10.0);
        bikeRepository.save(bike);
    }

    private RentalResponse toResponse(Rental rental) {
        boolean paid = paymentRepository.existsByRentalIdAndStatus(rental.getId(), PaymentStatus.SUCCESS);
        String paymentStatus = paymentRepository.findByRentalIdAndStatus(rental.getId(), PaymentStatus.SUCCESS)
                .map(payment -> payment.getStatus().name())
                .orElse("UNPAID");
        Long chatId = rideShareRepository.findByRentalId(rental.getId())
                .map(RideShare::getId)
                .orElse(null);
        return RentalResponse.builder()
                .id(rental.getId())
                .userId(rental.getUser().getId())
                .userName(rental.getUser().getUsername())
                .userEmail(rental.getUser().getEmail())
                .bikeId(rental.getBike().getId())
                .bikeTitle(rental.getBike().getTitle())
                .bikeType(rental.getBike().getBikeType().name())
                .bikeLocation(rental.getBike().getLocation())
                .plannedStartTime(rental.getPlannedStartTime())
                .plannedEndTime(rental.getPlannedEndTime())
                .startTime(rental.getStartTime())
                .endTime(rental.getEndTime())
                .dailyRate(rental.getDailyRate())
                .estimatedFare(rental.getEstimatedFare())
                .finalFare(rental.getFinalFare())
                .distanceKm(rental.getDistanceKm())
                .status(rental.getStatus().name())
                .paymentStatus(paymentStatus)
                .paid(paid)
                .chatId(chatId)
                .ownerId(rental.getBike().getOwnerId())
                .ownerName(rental.getBike().getOwnerName())
                .ownerPhone(rental.getBike().getOwnerPhone())
                .rating(rental.getRating())
                .review(rental.getReview())
                .ownerServiceRating(rental.getOwnerServiceRating())
                .ownerServiceReview(rental.getOwnerServiceReview())
                .reviewedAt(rental.getReviewedAt())
                .cancellationReason(rental.getCancellationReason())
                .cancellationFee(rental.getCancellationFee())
                .refundAmount(rental.getRefundAmount())
                .cancelledAt(rental.getCancelledAt())
                .createdAt(rental.getCreatedAt())
                .updatedAt(rental.getUpdatedAt())
                .build();
    }

    private void ensureRentalChat(Rental rental) {
        if (rideShareRepository.existsByRentalId(rental.getId())) {
            return;
        }
        Bike bike = rental.getBike();
        if (bike.getOwnerId() == null) {
            return;
        }
        RideShare chat = RideShare.builder()
                .rental(rental)
                .rider(rental.getUser())
                .ownerId(bike.getOwnerId())
                .pickupAddress(bike.getLocation() != null ? bike.getLocation() : "To be discussed")
                .dropoffAddress("To be discussed in chat")
                .status(RideShareStatus.PENDING)
                .build();
        rideShareRepository.save(chat);
    }
}
