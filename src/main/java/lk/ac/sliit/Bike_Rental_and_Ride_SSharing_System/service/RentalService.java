package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.RentalCancelRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.RentalRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.RentalReviewRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.CancellationResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.RentalResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface RentalService {

    // ── Rider operations ──────────────────────────────────────────────────────
    RentalResponse createRental(String email, RentalRequest request);
    RentalResponse confirmRental(String email, Long rentalId);
    RentalResponse startRental(String email, Long rentalId);
    RentalResponse completeRental(String email, Long rentalId, BigDecimal distanceKm);
    CancellationResponse cancelRental(String email, Long rentalId, RentalCancelRequest request);
    RentalResponse submitReview(String email, Long rentalId, RentalReviewRequest request);

    // ── Query operations ──────────────────────────────────────────────────────
    RentalResponse getRentalById(String email, Long rentalId);
    List<RentalResponse> getMyRentals(String email);
    List<RentalResponse> getMyActiveRentals(String email);
    List<RentalResponse> getOwnerRentals(String email);

    // ── Admin operations ──────────────────────────────────────────────────────
    List<RentalResponse> getAllRentals();
    List<RentalResponse> getRentalsByStatus(String status);
    List<RentalResponse> getRentalsByBike(Long bikeId);
    List<RentalResponse> getRentalsByUser(Long userId);
    BigDecimal getTotalRevenue();
    BigDecimal getRevenueBetween(LocalDateTime start, LocalDateTime end);
}
