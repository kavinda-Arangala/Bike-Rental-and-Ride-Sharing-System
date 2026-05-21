package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.controller;

import jakarta.validation.Valid;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.SharedRideRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.SharedRideReviewRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.ApiResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.SharedRideBookingResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.SharedRideResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.Rental;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.SharedRide;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.SharedRideBooking;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.User;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.PaymentStatus;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.RentalStatus;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.SharedRideBookingStatus;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.SharedRideStatus;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.exception.ResourceNotFoundException;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.RentalRepository;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.SharedRideBookingRepository;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.SharedRideRepository;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/shared-rides")
@RequiredArgsConstructor
public class SharedRideController {

    private final SharedRideRepository sharedRideRepository;
    private final SharedRideBookingRepository bookingRepository;
    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasAnyRole('RIDER','ADMIN')")
    public ResponseEntity<ApiResponse<SharedRideResponse>> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SharedRideRequest request) {
        User creator = user(userDetails.getUsername());
        Rental rental = rental(request.getRentalId());
        if (!rental.getUser().getId().equals(creator.getId())) {
            throw new IllegalStateException("You can create shared rides only for your own rental");
        }
        if (rental.getStatus() != RentalStatus.ACTIVE) {
            throw new IllegalStateException("Start the rental before creating a shared ride");
        }
        if (request.getRideTime().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Ride time must be in the future");
        }

        SharedRide saved = sharedRideRepository.save(SharedRide.builder()
                .rental(rental)
                .creator(creator)
                .pickupAddress(request.getPickupAddress())
                .dropoffAddress(request.getDropoffAddress())
                .rideTime(request.getRideTime())
                .price(request.getPrice())
                .availableSeats(request.getAvailableSeats())
                .status(SharedRideStatus.OPEN)
                .build());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Shared ride created", rideResponse(saved)));
    }

    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('USER','RIDER','ADMIN','BIKE_OWNER')")
    public ResponseEntity<ApiResponse<List<SharedRideResponse>>> available() {
        return ResponseEntity.ok(ApiResponse.success("Available shared rides",
                sharedRideRepository.findByStatusOrderByRideTimeAsc(SharedRideStatus.OPEN)
                        .stream().filter(r -> r.getAvailableSeats() > 0)
                        .map(this::rideResponse).toList()));
    }

    @GetMapping("/my-created")
    @PreAuthorize("hasAnyRole('RIDER','ADMIN')")
    public ResponseEntity<ApiResponse<List<SharedRideResponse>>> myCreated(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = user(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("My shared rides",
                sharedRideRepository.findByCreatorIdOrderByCreatedAtDesc(user.getId())
                        .stream().map(this::rideResponse).toList()));
    }

    @GetMapping("/my-bookings")
    @PreAuthorize("hasAnyRole('USER','RIDER','ADMIN','BIKE_OWNER')")
    public ResponseEntity<ApiResponse<List<SharedRideBookingResponse>>> myBookings(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = user(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("My shared ride bookings",
                bookingRepository.findByPassengerIdOrderByCreatedAtDesc(user.getId())
                        .stream().map(this::bookingResponse).toList()));
    }

    @GetMapping("/created-bookings")
    @PreAuthorize("hasAnyRole('RIDER','ADMIN')")
    public ResponseEntity<ApiResponse<List<SharedRideBookingResponse>>> createdBookings(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = user(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Bookings for my shared rides",
                bookingRepository.findBySharedRideCreatorIdOrderByCreatedAtDesc(user.getId())
                        .stream().map(this::bookingResponse).toList()));
    }

    @PostMapping("/{id}/join")
    @PreAuthorize("hasAnyRole('USER','RIDER','ADMIN','BIKE_OWNER')")
    public ResponseEntity<ApiResponse<SharedRideBookingResponse>> join(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        User passenger = user(userDetails.getUsername());
        SharedRide ride = sharedRide(id);
        if (ride.getCreator().getId().equals(passenger.getId())) {
            throw new IllegalStateException("You cannot join your own shared ride");
        }
        if (ride.getStatus() != SharedRideStatus.OPEN || ride.getAvailableSeats() <= 0) {
            throw new IllegalStateException("This shared ride is not available");
        }
        if (bookingRepository.existsBySharedRideIdAndPassengerId(id, passenger.getId())) {
            throw new IllegalStateException("You already joined this shared ride");
        }
        ride.setAvailableSeats(ride.getAvailableSeats() - 1);
        if (ride.getAvailableSeats() == 0) {
            ride.setStatus(SharedRideStatus.FULL);
        }
        sharedRideRepository.save(ride);
        SharedRideBooking booking = bookingRepository.save(SharedRideBooking.builder()
                .sharedRide(ride)
                .passenger(passenger)
                .status(SharedRideBookingStatus.JOINED)
                .paymentStatus(PaymentStatus.PENDING)
                .build());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Shared ride joined", bookingResponse(booking)));
    }

    @PatchMapping("/bookings/{id}/pay")
    @PreAuthorize("hasAnyRole('USER','RIDER','ADMIN','BIKE_OWNER')")
    public ResponseEntity<ApiResponse<SharedRideBookingResponse>> pay(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        throw new IllegalStateException("Use the Payments page to pay for shared rides");
    }

    @PatchMapping("/bookings/{id}/complete")
    @PreAuthorize("hasAnyRole('USER','RIDER','ADMIN','BIKE_OWNER')")
    public ResponseEntity<ApiResponse<SharedRideBookingResponse>> completeBooking(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        SharedRideBooking booking = booking(id);
        requirePassenger(booking, userDetails.getUsername());
        if (booking.getPaymentStatus() != PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Complete shared ride payment before completing the ride");
        }
        booking.setStatus(SharedRideBookingStatus.COMPLETED);
        booking.setCompletedAt(LocalDateTime.now());
        return ResponseEntity.ok(ApiResponse.success("Shared ride completed",
                bookingResponse(bookingRepository.save(booking))));
    }

    @PostMapping("/bookings/{id}/review")
    @PreAuthorize("hasAnyRole('USER','RIDER','ADMIN','BIKE_OWNER')")
    public ResponseEntity<ApiResponse<SharedRideBookingResponse>> review(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody SharedRideReviewRequest request) {
        SharedRideBooking booking = booking(id);
        requirePassenger(booking, userDetails.getUsername());
        if (booking.getStatus() != SharedRideBookingStatus.COMPLETED) {
            throw new IllegalStateException("Only completed shared rides can be reviewed");
        }
        booking.setRating(request.getRating());
        booking.setReview(request.getReview());
        booking.setReviewedAt(LocalDateTime.now());
        return ResponseEntity.ok(ApiResponse.success("Shared ride review submitted",
                bookingResponse(bookingRepository.save(booking))));
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('RIDER','ADMIN')")
    public ResponseEntity<ApiResponse<SharedRideResponse>> completeRide(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        SharedRide ride = sharedRide(id);
        requireCreator(ride, userDetails.getUsername());
        ride.setStatus(SharedRideStatus.COMPLETED);
        return ResponseEntity.ok(ApiResponse.success("Shared ride closed",
                rideResponse(sharedRideRepository.save(ride))));
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<SharedRideResponse>>> adminAll() {
        return ResponseEntity.ok(ApiResponse.success("All shared rides",
                sharedRideRepository.findAll().stream().map(this::rideResponse).toList()));
    }

    @GetMapping("/admin/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<SharedRideResponse>>> adminByStatus(@PathVariable String status) {
        SharedRideStatus rideStatus = SharedRideStatus.valueOf(status.toUpperCase());
        return ResponseEntity.ok(ApiResponse.success("Shared rides by status",
                sharedRideRepository.findByStatusOrderByCreatedAtDesc(rideStatus)
                        .stream().map(this::rideResponse).toList()));
    }

    private void requireCreator(SharedRide ride, String username) {
        User user = user(username);
        if (!ride.getCreator().getId().equals(user.getId())) {
            throw new IllegalStateException("Only the rider who created this shared ride can do this");
        }
    }

    private void requirePassenger(SharedRideBooking booking, String username) {
        User user = user(username);
        if (!booking.getPassenger().getId().equals(user.getId())) {
            throw new IllegalStateException("Only this passenger can update the booking");
        }
    }

    private User user(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private Rental rental(Long id) {
        return rentalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rental not found: id=" + id));
    }

    private SharedRide sharedRide(Long id) {
        return sharedRideRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shared ride not found: id=" + id));
    }

    private SharedRideBooking booking(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shared ride booking not found: id=" + id));
    }

    private SharedRideResponse rideResponse(SharedRide ride) {
        return SharedRideResponse.builder()
                .id(ride.getId())
                .rentalId(ride.getRental().getId())
                .creatorId(ride.getCreator().getId())
                .creatorName(ride.getCreator().getUsername())
                .bikeTitle(ride.getRental().getBike().getTitle())
                .pickupAddress(ride.getPickupAddress())
                .dropoffAddress(ride.getDropoffAddress())
                .rideTime(ride.getRideTime())
                .price(ride.getPrice())
                .availableSeats(ride.getAvailableSeats())
                .status(ride.getStatus().name())
                .createdAt(ride.getCreatedAt())
                .build();
    }

    private SharedRideBookingResponse bookingResponse(SharedRideBooking booking) {
        SharedRide ride = booking.getSharedRide();
        return SharedRideBookingResponse.builder()
                .id(booking.getId())
                .sharedRideId(ride.getId())
                .bikeTitle(ride.getRental().getBike().getTitle())
                .pickupAddress(ride.getPickupAddress())
                .dropoffAddress(ride.getDropoffAddress())
                .rideTime(ride.getRideTime())
                .price(ride.getPrice())
                .passengerId(booking.getPassenger().getId())
                .passengerName(booking.getPassenger().getUsername())
                .status(booking.getStatus().name())
                .paymentStatus(booking.getPaymentStatus().name())
                .rating(booking.getRating())
                .review(booking.getReview())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
