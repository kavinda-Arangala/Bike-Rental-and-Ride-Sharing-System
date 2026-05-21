package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.UserRole;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String username;
    private String email;
    private UserRole role;
}