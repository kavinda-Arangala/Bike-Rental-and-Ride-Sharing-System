package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}