package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.controller;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.BikeDTO;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.ApiResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.BikeService;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bikes")
@RequiredArgsConstructor
public class BikeController {

    private final BikeService bikeService;
    private final SecurityUtil securityUtil;

    // ── Public endpoints (no token required) ──────────────────────────────────

    /**
     * GET /api/bikes
     * Get all bikes — public.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BikeDTO>>> getAllBikes() {
        return ResponseEntity.ok(ApiResponse.success("All bikes", bikeService.getAllBikes()));
    }

    /**
     * GET /api/bikes/available
     * Get all available bikes — public.
     */
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<BikeDTO>>> getAvailableBikes() {
        return ResponseEntity.ok(ApiResponse.success("Available bikes", bikeService.getAvailableBikes()));
    }

    /**
     * GET /api/bikes/{id}
     * Get a single bike by ID — public.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BikeDTO>> getBikeById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Bike details", bikeService.getBikeById(id)));
    }

    /**
     * GET /api/bikes/search
     * Search bikes by location, type, status, and price range — public.
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<BikeDTO>>> searchBikes(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String bikeType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {

        List<BikeDTO> results = bikeService.searchBikes(location, bikeType, status, minPrice, maxPrice);
        return ResponseEntity.ok(ApiResponse.success("Search results", results));
    }

    // ── Authenticated endpoints (RIDER or ADMIN) ──────────────────────────────

    /**
     * POST /api/bikes
     * Add a new bike — ADMIN only.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','BIKE_OWNER')")
    public ResponseEntity<ApiResponse<BikeDTO>> addBike(@Valid @RequestBody BikeDTO bikeDTO) {
        BikeDTO saved = bikeService.addBike(currentUsername(), bikeDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bike added successfully", saved));
    }

    /**
     * PUT /api/bikes/{id}
     * Update a bike — ADMIN only.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BikeDTO>> updateBike(
            @PathVariable Long id,
            @Valid @RequestBody BikeDTO bikeDTO) {
        return ResponseEntity.ok(ApiResponse.success("Bike updated successfully",
                bikeService.updateBike(currentUsername(), id, bikeDTO)));
    }

    /**
     * DELETE /api/bikes/{id}
     * Delete a bike — ADMIN only.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','BIKE_OWNER')")
    public ResponseEntity<ApiResponse<Void>> deleteBike(@PathVariable Long id) {
        bikeService.deleteBike(currentUsername(), id);
        return ResponseEntity.ok(ApiResponse.success("Bike deleted successfully"));
    }

    /**
     * PATCH /api/bikes/{id}/status
     * Update bike status — ADMIN only.
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','BIKE_OWNER')")
    public ResponseEntity<ApiResponse<Void>> updateBikeStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        bikeService.updateBikeStatus(currentUsername(), id, body.get("status"));
        return ResponseEntity.ok(ApiResponse.success("Bike status updated successfully"));
    }

    /**
     * PATCH /api/bikes/{id}/rating
     * Update bike rating — RIDER or ADMIN.
     */
    @PatchMapping("/{id}/rating")
    @PreAuthorize("hasAnyRole('RIDER','ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateBikeRating(
            @PathVariable Long id,
            @RequestBody Map<String, Double> body) {
        bikeService.updateBikeRating(id, body.get("rating"));
        return ResponseEntity.ok(ApiResponse.success("Bike rating updated successfully"));
    }

    // ── Owner endpoints ───────────────────────────────────────────────────────

    /**
     * GET /api/bikes/owner/{ownerId}
     * Get all bikes by a specific owner — ADMIN only.
     */
    @GetMapping("/owner/{ownerId}")
    @PreAuthorize("hasAnyRole('ADMIN','BIKE_OWNER')")
    public ResponseEntity<ApiResponse<List<BikeDTO>>> getBikesByOwner(@PathVariable Long ownerId) {
        return ResponseEntity.ok(ApiResponse.success("Owner bikes",
                bikeService.getBikesByOwner(ownerId)));
    }

    /**
     * GET /api/bikes/owner/{ownerId}/stats
     * Get owner bike stats — ADMIN only.
     */
    @GetMapping("/owner/{ownerId}/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getOwnerStats(@PathVariable Long ownerId) {
        Map<String, Long> stats = Map.of(
                "totalBikes",  bikeService.countBikesByOwner(ownerId),
                "rentedBikes", bikeService.countRentedBikesByOwner(ownerId)
        );
        return ResponseEntity.ok(ApiResponse.success("Owner stats", stats));
    }

    private String currentUsername() {
        String username = securityUtil.getCurrentUsername();
        if (username == null || username.isBlank()) {
            throw new IllegalStateException("Authenticated username is required.");
        }
        return username;
    }
}
