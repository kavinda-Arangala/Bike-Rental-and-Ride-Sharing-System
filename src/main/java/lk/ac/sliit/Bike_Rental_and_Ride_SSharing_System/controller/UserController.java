package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.controller;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.ChangePasswordRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.UpdateUserRoleRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.UpdateProfileRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.ApiResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.UserResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ─────────────────────────────────────────────────────────────────────────
    // RIDER — profile endpoints (logged-in user only)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * GET /api/users/profile
     * Get currently logged-in user's profile.
     */
    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('RIDER','BIKE_OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        UserResponse response = userService.getMyProfile(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Profile fetched", response));
    }

    /**
     * PUT /api/users/profile
     * Update name, phone, address, profile image.
     */
    @PutMapping("/profile")
    @PreAuthorize("hasAnyRole('RIDER','BIKE_OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserResponse response = userService.updateMyProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }

    /**
     * PATCH /api/users/profile/change-password
     * Change logged-in user's password.
     */
    @PatchMapping("/profile/change-password")
    @PreAuthorize("hasAnyRole('RIDER','BIKE_OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }

    /**
     * DELETE /api/users/profile
     * Delete own account permanently.
     */
    @DeleteMapping("/profile")
    @PreAuthorize("hasAnyRole('RIDER','BIKE_OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteMyAccount(
            @AuthenticationPrincipal UserDetails userDetails) {
        userService.deleteMyAccount(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Account deleted successfully"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ADMIN only endpoints
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * GET /api/users/admin/all
     * Get all users (admin only).
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success("All users", userService.getAllUsers()));
    }

    /**
     * GET /api/users/admin/riders
     * Get all riders (admin only).
     */
    @GetMapping("/admin/riders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllRiders() {
        return ResponseEntity.ok(ApiResponse.success("All riders", userService.getAllRiders()));
    }

    /**
     * GET /api/users/admin/{id}
     * Get any user by ID (admin only).
     */
    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("User details", userService.getUserById(id)));
    }

    /**
     * PATCH /api/users/admin/{id}/activate
     * Activate a deactivated user account (admin only).
     */
    @PatchMapping("/admin/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> activateUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("User activated", userService.activateUser(id)));
    }

    /**
     * PATCH /api/users/admin/{id}/deactivate
     * Deactivate a user account (admin only).
     */
    @PatchMapping("/admin/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> deactivateUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("User deactivated", userService.deactivateUser(id)));
    }

    /**
     * PATCH /api/users/admin/{id}/role
     * Change a user's role (admin only).
     */
    @PatchMapping("/admin/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRoleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "User role updated",
                userService.updateUserRole(id, request.getRole())));
    }

    /**
     * DELETE /api/users/admin/{id}
     * Permanently delete a user (admin only).
     */
    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }
}
