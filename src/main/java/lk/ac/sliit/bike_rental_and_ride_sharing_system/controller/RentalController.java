package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.controller;

import jakarta.validation.Valid;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.RentalCancelRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.RentalRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.RentalReviewRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.ApiResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.CancellationResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.RentalResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.RentalService;
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
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    // ── Rider endpoints ───────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasAnyRole('RIDER','BIKE_OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<RentalResponse>> createRental(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RentalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Rental created successfully",
                        rentalService.createRental(
                                userDetails.getUsername(), request)));
    }

    @PatchMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('RIDER','BIKE_OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<RentalResponse>> startRental(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Rental started",
                rentalService.startRental(userDetails.getUsername(), id)));
    }

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('BIKE_OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<RentalResponse>> confirmRental(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Rental confirmed",
                rentalService.confirmRental(userDetails.getUsername(), id)));
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('RIDER','BIKE_OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<RentalResponse>> completeRental(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "0")
            BigDecimal distanceKm) {
        return ResponseEntity.ok(ApiResponse.success("Rental completed",
                rentalService.completeRental(
                        userDetails.getUsername(), id, distanceKm)));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('RIDER','BIKE_OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<CancellationResponse>> cancelRental(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody RentalCancelRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Rental cancelled",
                rentalService.cancelRental(
                        userDetails.getUsername(), id, request)));
    }

    @PostMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('RIDER','BIKE_OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<RentalResponse>> submitReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody RentalReviewRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Review submitted",
                rentalService.submitReview(
                        userDetails.getUsername(), id, request)));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('RIDER','BIKE_OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<List<RentalResponse>>> getMyRentals(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("My rentals",
                rentalService.getMyRentals(userDetails.getUsername())));
    }

    @GetMapping("/my/active")
    @PreAuthorize("hasAnyRole('RIDER','BIKE_OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<List<RentalResponse>>> getMyActiveRentals(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Active rentals",
                rentalService.getMyActiveRentals(userDetails.getUsername())));
    }

    @GetMapping("/owner")
    @PreAuthorize("hasAnyRole('BIKE_OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<List<RentalResponse>>> getOwnerRentals(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Owner rentals",
                rentalService.getOwnerRentals(userDetails.getUsername())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RIDER','BIKE_OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<RentalResponse>> getRentalById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Rental details",
                rentalService.getRentalById(userDetails.getUsername(), id)));
    }

    // ── Admin endpoints ───────────────────────────────────────────────────────

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<RentalResponse>>> getAllRentals() {
        return ResponseEntity.ok(ApiResponse.success("All rentals",
                rentalService.getAllRentals()));
    }

    @GetMapping("/admin/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<RentalResponse>>> getRentalsByStatus(
            @PathVariable String status) {
        return ResponseEntity.ok(ApiResponse.success("Rentals by status",
                rentalService.getRentalsByStatus(status)));
    }

    @GetMapping("/admin/bike/{bikeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<RentalResponse>>> getRentalsByBike(
            @PathVariable Long bikeId) {
        return ResponseEntity.ok(ApiResponse.success("Bike rentals",
                rentalService.getRentalsByBike(bikeId)));
    }

    @GetMapping("/admin/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<RentalResponse>>> getRentalsByUser(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success("User rentals",
                rentalService.getRentalsByUser(userId)));
    }

    @GetMapping("/admin/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalRevenue() {
        return ResponseEntity.ok(ApiResponse.success("Total revenue",
                rentalService.getTotalRevenue()));
    }

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
                rentalService.getRevenueBetween(start, end)));
    }
}
