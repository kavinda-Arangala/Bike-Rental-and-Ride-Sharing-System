package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.ChangePasswordRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.UpdateProfileRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.UserResponse;

import java.util.List;

public interface UserService {

    // ── Profile — for the logged-in user (identified by username from JWT) ────
    UserResponse getMyProfile(String username);
    UserResponse updateMyProfile(String username, UpdateProfileRequest request);
    void changePassword(String username, ChangePasswordRequest request);
    void deleteMyAccount(String username);

    // ── Admin operations ──────────────────────────────────────────────────────
    List<UserResponse> getAllUsers();
    List<UserResponse> getAllRiders();
    UserResponse getUserById(Long id);
    UserResponse activateUser(Long id);
    UserResponse deactivateUser(Long id);
    UserResponse updateUserRole(Long id, String role);
    void deleteUser(Long id);
}
