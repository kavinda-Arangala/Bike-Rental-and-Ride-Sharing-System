package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.PaymentRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.RefundRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.PaymentResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PaymentService {

    // ── Rider operations ──────────────────────────────────────────────────────
    PaymentResponse makePayment(String username, PaymentRequest request);
    List<PaymentResponse> getMyPayments(String username);
    List<PaymentResponse> getOwnerPayments(String username);
    PaymentResponse getPaymentById(String username, Long paymentId);
    List<PaymentResponse> getPaymentsByRental(String username, Long rentalId);

    // ── Admin operations ──────────────────────────────────────────────────────
    List<PaymentResponse> getAllPayments();
    List<PaymentResponse> getPaymentsByStatus(String status);
    List<PaymentResponse> getPaymentsByUser(Long userId);
    PaymentResponse processRefund(String username, RefundRequest request);
    BigDecimal getTotalRevenue();
    BigDecimal getRevenueBetween(LocalDateTime start, LocalDateTime end);
}
