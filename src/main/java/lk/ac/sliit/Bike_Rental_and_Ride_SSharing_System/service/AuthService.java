package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.LoginRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.RegisterRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.AuthResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.exception.UserAlreadyExistsException;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(String refreshToken);
}