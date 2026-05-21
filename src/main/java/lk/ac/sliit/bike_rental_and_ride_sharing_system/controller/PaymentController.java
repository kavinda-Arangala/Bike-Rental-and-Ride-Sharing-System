package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.controller;

import jakarta.validation.Valid;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.PaymentRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.RefundRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.ApiResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.PaymentResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // ── Rider endpoints ───────────────────────────────────────────────────────

    /**
     * POST /api/payments
     * Make a payment for a completed rental.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER','RIDER','BIKE_OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<PaymentResponse>> makePayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.makePayment(
                userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment successful", response));
    }

    /**
     * GET /api/payments/my
     * Get all payments of the logged-in user.
     */
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('USER','RIDER','BIKE_OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getMyPayments(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("My payments",
                paymentService.getMyPayments(userDetails.getUsername())));
    }

    @GetMapping("/owner")
    @PreAuthorize("hasAnyRole('BIKE_OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getOwnerPayments(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Owner payments",
                paymentService.getOwnerPayments(userDetails.getUsername())));
    }

    /**
     * GET /api/payments/{id}
     * Get a specific payment by ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','RIDER','BIKE_OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Payment details",
                paymentService.getPaymentById(userDetails.getUsername(), id)));
    }

    /**
     * GET /api/payments/rental/{rentalId}
     * Get all payments for a specific rental.
     */
    @GetMapping("/rental/{rentalId}")
    @PreAuthorize("hasAnyRole('USER','RIDER','BIKE_OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByRental(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long rentalId) {
        return ResponseEntity.ok(ApiResponse.success("Rental payments",
                paymentService.getPaymentsByRental(
                        userDetails.getUsername(), rentalId)));
    }

    // ── Admin endpoints ───────────────────────────────────────────────────────

    /**
     * GET /api/payments/admin/all
     * Get all payments (admin only).
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getAllPayments() {
        return ResponseEntity.ok(ApiResponse.success("All payments",
                paymentService.getAllPayments()));
    }

    /**
     * GET /api/payments/admin/status/{status}
     * Get payments filtered by status (admin only).
     */
    @GetMapping("/admin/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByStatus(
            @PathVariable String status) {
        return ResponseEntity.ok(ApiResponse.success("Payments by status",
                paymentService.getPaymentsByStatus(status)));
    }

    /**
     * GET /api/payments/admin/user/{userId}
     * Get all payments by a specific user (admin only).
     */
    @GetMapping("/admin/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByUser(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success("User payments",
                paymentService.getPaymentsByUser(userId)));
    }

    /**
     * POST /api/payments/admin/refund
     * Process a refund for a payment (admin only).
     */
    @PostMapping("/admin/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentResponse>> processRefund(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RefundRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Refund processed",
                paymentService.processRefund(
                        userDetails.getUsername(), request)));
    }

    /**
     * GET /api/payments/admin/revenue
     * Get total revenue from all successful payments (admin only).
     */
    @GetMapping("/admin/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalRevenue() {
        return ResponseEntity.ok(ApiResponse.success("Total revenue",
                paymentService.getTotalRevenue()));
    }

    /**
     * GET /api/payments/admin/revenue/range
     * Get revenue between two dates (admin only).
     */
    @GetMapping("/admin/revenue/range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BigDecimal>> getRevenueBetween(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime start,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime end) {
        return ResponseEntity.ok(ApiResponse.success("Revenue in range",
                paymentService.getRevenueBetween(start, end)));
    }
}
