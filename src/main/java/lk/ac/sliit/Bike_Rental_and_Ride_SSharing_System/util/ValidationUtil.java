package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.util;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Utility helpers for extra validation logic beyond Bean Validation annotations.
 */
@Component
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[a-zA-Z0-9_]{3,50}$");

    private static final Pattern PASSWORD_STRENGTH_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");

    public boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }

    public boolean isStrongPassword(String password) {
        return password != null && PASSWORD_STRENGTH_PATTERN.matcher(password).matches();
    }

    /**
     * Masks email for safe logging/display: john@example.com → j***@example.com
     */
    public String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        String[] parts = email.split("@");
        String local = parts[0];
        String masked = local.charAt(0) + "*".repeat(Math.max(local.length() - 1, 3));
        return masked + "@" + parts[1];
    }

    /**
     * Returns true if the given string looks like an email address.
     * Used in the login flow to decide whether to search by email or username.
     */
    public boolean looksLikeEmail(String value) {
        return value != null && value.contains("@");
    }
}