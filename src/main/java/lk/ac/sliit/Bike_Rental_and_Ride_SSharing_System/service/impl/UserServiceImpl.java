package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.impl;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.ChangePasswordRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.UpdateProfileRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.UserResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.User;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.UserRole;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.exception.ResourceNotFoundException;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.UserRepository;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ── Profile (logged-in user) ──────────────────────────────────────────────

    /**
     * Get the profile of the currently logged-in user.
     * 'username' comes from JWT subject via UserDetails.getUsername().
     */
    @Override
    public UserResponse getMyProfile(String username) {
        return toResponse(findByUsernameOrThrow(username));
    }

    /**
     * Update name, phone, address, and profile image of the logged-in user.
     */
    @Override
    @Transactional
    public UserResponse updateMyProfile(String username, UpdateProfileRequest request) {
        User user = findByUsernameOrThrow(username);

        user.setName(request.getName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());

        if (request.getProfileImage() != null) {
            user.setProfileImage(request.getProfileImage());
        }

        log.info("Profile updated for username: {}", username);
        return toResponse(userRepository.save(user));
    }

    /**
     * Change the password of the logged-in user.
     * Verifies the current password before updating.
     */
    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = findByUsernameOrThrow(username);

        // Verify current password matches
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalStateException("Current password is incorrect");
        }

        // Confirm new passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalStateException("New password and confirm password do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed for username: {}", username);
    }

    /**
     * Permanently delete the logged-in user's own account.
     */
    @Override
    @Transactional
    public void deleteMyAccount(String username) {
        User user = findByUsernameOrThrow(username);
        userRepository.delete(user);
        log.info("Account deleted for username: {}", username);
    }

    // ── Admin operations ──────────────────────────────────────────────────────

    /**
     * Get all users regardless of role (admin only).
     */
    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all users with role RIDER (admin only).
     */
    @Override
    public List<UserResponse> getAllRiders() {
        return userRepository.findByRole(UserRole.RIDER)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get any user by their database ID (admin only).
     */
    @Override
    public UserResponse getUserById(Long id) {
        return toResponse(findByIdOrThrow(id));
    }

    /**
     * Activate a deactivated user account (admin only).
     */
    @Override
    @Transactional
    public UserResponse activateUser(Long id) {
        User user = findByIdOrThrow(id);
        user.setIsActive(true);
        user.setEnabled(true);
        log.info("User activated: id={}", id);
        return toResponse(userRepository.save(user));
    }

    /**
     * Deactivate a user account (admin only).
     */
    @Override
    @Transactional
    public UserResponse deactivateUser(Long id) {
        User user = findByIdOrThrow(id);
        user.setIsActive(false);
        user.setEnabled(false);
        log.info("User deactivated: id={}", id);
        return toResponse(userRepository.save(user));
    }

    /**
     * Change a user's role. Only admin controllers expose this operation.
     */
    @Override
    @Transactional
    public UserResponse updateUserRole(Long id, String role) {
        User user = findByIdOrThrow(id);
        UserRole newRole;

        try {
            newRole = UserRole.valueOf(role.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new IllegalStateException("Invalid role: " + role);
        }

        user.setRole(newRole);
        log.info("User role updated by admin: id={}, role={}", id, newRole);
        return toResponse(userRepository.save(user));
    }

    /**
     * Permanently delete any user by ID (admin only).
     */
    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = findByIdOrThrow(id);
        userRepository.delete(user);
        log.info("User deleted by admin: id={}", id);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Find user by username (used for JWT-authenticated operations).
     * JWT subject is always the username field, not email.
     */
    private User findByUsernameOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with username: " + username));
    }

    /**
     * Find user by database ID (used for admin operations).
     */
    private User findByIdOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + id));
    }

    /**
     * Map User entity to UserResponse DTO.
     */
    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .profileImage(user.getProfileImage())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
