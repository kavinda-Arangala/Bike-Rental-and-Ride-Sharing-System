package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.impl;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.PaymentRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.RefundRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.PaymentResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.Payment;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.Rental;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.SharedRideBooking;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.User;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.PaymentMethod;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.PaymentStatus;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.RentalStatus;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.SharedRideBookingStatus;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.exception.ResourceNotFoundException;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.PaymentRepository;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.RentalRepository;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.SharedRideBookingRepository;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.UserRepository;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.PaymentService;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final RentalRepository  rentalRepository;
    private final SharedRideBookingRepository sharedRideBookingRepository;
    private final UserRepository    userRepository;
    private final SecurityUtil      securityUtil;

    // ── Make Payment ──────────────────────────────────────────────────────────

    @Override
    @Transactional
    public PaymentResponse makePayment(String username, PaymentRequest request) {
        User user = findUserOrThrow(username);
        if ((request.getRentalId() == null && request.getSharedRideBookingId() == null) ||
                (request.getRentalId() != null && request.getSharedRideBookingId() != null)) {
            throw new IllegalStateException("Select either a rental payment or a shared ride payment");
        }
        PaymentMethod method = parsePaymentMethod(request.getPaymentMethod());
        if (request.getSharedRideBookingId() != null) {
            return makeSharedRidePayment(user, request, method);
        }

        Rental rental = findRentalOrThrow(request.getRentalId());

        // Only the rental owner can pay
        if (!rental.getUser().getUsername().equals(username)) {
            throw new IllegalStateException(
                    "You are not authorized to pay for this rental");
        }

        // Payment is required before a rental can be started.
        if (rental.getStatus() != RentalStatus.PENDING) {
            throw new IllegalStateException(
                    "Payment can only be made before the ride starts. " +
                            "Current status: " + rental.getStatus());
        }

        // Check if already paid
        boolean alreadyPaid = paymentRepository
                .findByRentalIdAndStatus(rental.getId(), PaymentStatus.SUCCESS)
                .isPresent();
        if (alreadyPaid) {
            throw new IllegalStateException(
                    "Payment already made for this rental");
        }

        // Amount = finalFare from rental
        BigDecimal amount = rental.getFinalFare() != null
                ? rental.getFinalFare()
                : rental.getEstimatedFare();

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Invalid payment amount for this rental");
        }

        // Generate unique transaction ID
        String transactionId = "TXN-" + UUID.randomUUID().toString().toUpperCase();

        Payment payment = Payment.builder()
                .rental(rental)
                .user(user)
                .amount(amount)
                .status(PaymentStatus.SUCCESS)
                .paymentMethod(method)
                .transactionId(transactionId)
                .paymentNote(request.getPaymentNote())
                .build();

        Payment saved = paymentRepository.save(payment);
        rental.setStatus(RentalStatus.PAID);
        rentalRepository.save(rental);
        log.info("Payment successful: id={}, rentalId={}, amount={}, txn={}",
                saved.getId(), rental.getId(), amount, transactionId);

        return toResponse(saved);
    }

    private PaymentResponse makeSharedRidePayment(User user, PaymentRequest request, PaymentMethod method) {
        SharedRideBooking booking = findSharedRideBookingOrThrow(request.getSharedRideBookingId());
        if (!booking.getPassenger().getId().equals(user.getId())) {
            throw new IllegalStateException("You are not authorized to pay for this shared ride");
        }
        if (booking.getPaymentStatus() == PaymentStatus.SUCCESS ||
                paymentRepository.existsBySharedRideBookingIdAndStatus(booking.getId(), PaymentStatus.SUCCESS)) {
            throw new IllegalStateException("Payment already made for this shared ride");
        }
        if (booking.getStatus() == SharedRideBookingStatus.CANCELLED ||
                booking.getStatus() == SharedRideBookingStatus.COMPLETED) {
            throw new IllegalStateException("Payment can only be made before the shared ride is completed");
        }

        BigDecimal amount = booking.getSharedRide().getPrice();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Invalid payment amount for this shared ride");
        }

        Payment payment = Payment.builder()
                .sharedRideBooking(booking)
                .user(user)
                .amount(amount)
                .status(PaymentStatus.SUCCESS)
                .paymentMethod(method)
                .transactionId("TXN-" + UUID.randomUUID().toString().toUpperCase())
                .paymentNote(request.getPaymentNote())
                .build();

        Payment saved = paymentRepository.save(payment);
        booking.setStatus(SharedRideBookingStatus.PAID);
        booking.setPaymentStatus(PaymentStatus.SUCCESS);
        booking.setPaidAt(LocalDateTime.now());
        sharedRideBookingRepository.save(booking);
        log.info("Shared ride payment successful: id={}, bookingId={}, amount={}",
                saved.getId(), booking.getId(), amount);
        return toResponse(saved);
    }

    // ── Get My Payments ───────────────────────────────────────────────────────

    @Override
    public List<PaymentResponse> getMyPayments(String username) {
        User user = findUserOrThrow(username);
        return paymentRepository.findByUserId(user.getId())
                .stream().map(this::toResponse).toList();
    }

    @Override
    public List<PaymentResponse> getOwnerPayments(String username) {
        User owner = findUserOrThrow(username);
        return paymentRepository.findByRentalBikeOwnerId(owner.getId())
                .stream().map(this::toResponse).toList();
    }

    // ── Get Payment By ID ─────────────────────────────────────────────────────

    @Override
    public PaymentResponse getPaymentById(String username, Long paymentId) {
        Payment payment = findPaymentOrThrow(paymentId);
        validatePaymentAccess(payment, username);
        return toResponse(payment);
    }

    // ── Get Payments By Rental ────────────────────────────────────────────────

    @Override
    public List<PaymentResponse> getPaymentsByRental(String username, Long rentalId) {
        Rental rental = findRentalOrThrow(rentalId);
        if (!securityUtil.isAdmin() &&
                !rental.getUser().getUsername().equals(username) &&
                !isBikeOwner(rental, username)) {
            throw new IllegalStateException(
                    "You are not authorized to view payments for this rental");
        }
        return paymentRepository.findByRentalId(rentalId)
                .stream().map(this::toResponse).toList();
    }

    // ── Admin: Get All Payments ───────────────────────────────────────────────

    @Override
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll()
                .stream().map(this::toResponse).toList();
    }

    // ── Admin: Get Payments By Status ─────────────────────────────────────────

    @Override
    public List<PaymentResponse> getPaymentsByStatus(String status) {
        PaymentStatus paymentStatus;
        try {
            paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid status. Must be: PENDING, SUCCESS, FAILED, or REFUNDED");
        }
        return paymentRepository.findByStatus(paymentStatus)
                .stream().map(this::toResponse).toList();
    }

    // ── Admin: Get Payments By User ───────────────────────────────────────────

    @Override
    public List<PaymentResponse> getPaymentsByUser(Long userId) {
        return paymentRepository.findByUserId(userId)
                .stream().map(this::toResponse).toList();
    }

    // ── Admin: Process Refund ─────────────────────────────────────────────────

    @Override
    @Transactional
    public PaymentResponse processRefund(String username, RefundRequest request) {
        Payment payment = findPaymentOrThrow(request.getPaymentId());

        // Only SUCCESS payments can be refunded
        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new IllegalStateException(
                    "Only successful payments can be refunded. " +
                            "Current status: " + payment.getStatus());
        }

        // Use refund amount from rental cancellation if available
        BigDecimal refundAmount = payment.getRental() != null && payment.getRental().getRefundAmount() != null
                ? payment.getRental().getRefundAmount()
                : payment.getAmount();

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundAmount(refundAmount);
        payment.setRefundReason(request.getRefundReason());
        payment.setRefundedAt(LocalDateTime.now());

        Payment saved = paymentRepository.save(payment);
        log.info("Refund processed: paymentId={}, refundAmount={}", saved.getId(), refundAmount);

        return toResponse(saved);
    }

    // ── Admin: Total Revenue ──────────────────────────────────────────────────

    @Override
    public BigDecimal getTotalRevenue() {
        return paymentRepository.sumAmountByStatus(PaymentStatus.SUCCESS);
    }

    // ── Admin: Revenue Between Dates ──────────────────────────────────────────

    @Override
    public BigDecimal getRevenueBetween(LocalDateTime start, LocalDateTime end) {
        return paymentRepository.sumAmountByStatusBetween(PaymentStatus.SUCCESS, start, end);
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    private void validatePaymentAccess(Payment payment, String username) {
        if (securityUtil.isAdmin() || payment.getUser().getUsername().equals(username)) {
            return;
        }
        if (payment.getRental() != null && isBikeOwner(payment.getRental(), username)) {
            return;
        }
        if (payment.getSharedRideBooking() != null &&
                payment.getSharedRideBooking().getSharedRide().getCreator().getUsername().equals(username)) {
            return;
        }
        {
            throw new IllegalStateException(
                    "You are not authorized to access this payment");
        }
    }

    private boolean isBikeOwner(Rental rental, String username) {
        User user = findUserOrThrow(username);
        return rental.getBike().getOwnerId() != null &&
                rental.getBike().getOwnerId().equals(user.getId());
    }

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

    private Payment findPaymentOrThrow(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found: id=" + id));
    }

    private SharedRideBooking findSharedRideBookingOrThrow(Long id) {
        return sharedRideBookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Shared ride booking not found: id=" + id));
    }

    private PaymentMethod parsePaymentMethod(String value) {
        try {
            return PaymentMethod.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid payment method. Must be: CASH, CARD, BANK_TRANSFER, or WALLET");
        }
    }

    private PaymentResponse toResponse(Payment payment) {
        Rental rental = payment.getRental();
        SharedRideBooking booking = payment.getSharedRideBooking();
        return PaymentResponse.builder()
                .id(payment.getId())
                .rentalId(rental != null ? rental.getId() : null)
                .sharedRideBookingId(booking != null ? booking.getId() : null)
                .paymentFor(booking != null ? "SHARED_RIDE" : "RENTAL")
                .bikeTitle(rental != null ? rental.getBike().getTitle() : booking.getSharedRide().getRental().getBike().getTitle())
                .rentalStatus(rental != null ? rental.getStatus().name() : null)
                .bikeOwnerId(rental != null ? rental.getBike().getOwnerId() : booking.getSharedRide().getRental().getBike().getOwnerId())
                .bikeOwnerName(rental != null ? rental.getBike().getOwnerName() : booking.getSharedRide().getRental().getBike().getOwnerName())
                .userId(payment.getUser().getId())
                .username(payment.getUser().getUsername())
                .amount(payment.getAmount())
                .status(payment.getStatus().name())
                .paymentMethod(payment.getPaymentMethod().name())
                .transactionId(payment.getTransactionId())
                .paymentNote(payment.getPaymentNote())
                .refundAmount(payment.getRefundAmount())
                .refundReason(payment.getRefundReason())
                .refundedAt(payment.getRefundedAt())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
