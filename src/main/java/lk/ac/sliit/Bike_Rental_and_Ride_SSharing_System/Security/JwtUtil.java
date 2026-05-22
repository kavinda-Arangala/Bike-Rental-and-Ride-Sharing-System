package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.Security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Component
public class JwtUtil {

    public String generateToken(UserDetails userDetails) {
        String payload = userDetails.getUsername() + ":" + Instant.now().toEpochMilli();
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }
}
