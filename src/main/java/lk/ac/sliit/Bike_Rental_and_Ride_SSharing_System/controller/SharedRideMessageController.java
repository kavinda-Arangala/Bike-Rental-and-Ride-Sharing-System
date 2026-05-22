package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.controller;

import jakarta.validation.Valid;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.MessageRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.ApiResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.MessageResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.SharedRideBooking;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.SharedRideMessage;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.User;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.exception.ResourceNotFoundException;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.SharedRideBookingRepository;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.SharedRideMessageRepository;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shared-rides/bookings/{bookingId}/messages")
@RequiredArgsConstructor
public class SharedRideMessageController {

    private final SharedRideBookingRepository bookingRepository;
    private final SharedRideMessageRepository messageRepository;
    private final UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER','RIDER','ADMIN','BIKE_OWNER')")
    public ResponseEntity<ApiResponse<MessageResponse>> send(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long bookingId,
            @Valid @RequestBody MessageRequest request) {
        User sender = user(userDetails.getUsername());
        SharedRideBooking booking = booking(bookingId);
        requireChatMember(booking, sender);

        SharedRideMessage saved = messageRepository.save(SharedRideMessage.builder()
                .booking(booking)
                .sender(sender)
                .content(request.getContent().trim())
                .build());
        return ResponseEntity.ok(ApiResponse.success("Message sent", response(saved, sender.getId())));
    }

    @GetMapping
    @Transactional
    @PreAuthorize("hasAnyRole('USER','RIDER','ADMIN','BIKE_OWNER')")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> messages(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long bookingId) {
        User current = user(userDetails.getUsername());
        SharedRideBooking booking = booking(bookingId);
        requireChatMember(booking, current);
        messageRepository.markAllAsReadForUser(bookingId, current.getId());
        return ResponseEntity.ok(ApiResponse.success("Shared ride messages",
                messageRepository.findByBookingIdOrderBySentAtAsc(bookingId)
                        .stream().map(message -> response(message, current.getId())).toList()));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasAnyRole('USER','RIDER','ADMIN','BIKE_OWNER')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> unread(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long bookingId) {
        User current = user(userDetails.getUsername());
        SharedRideBooking booking = booking(bookingId);
        requireChatMember(booking, current);
        long count = messageRepository.countUnreadByBookingIdForUser(bookingId, current.getId());
        return ResponseEntity.ok(ApiResponse.success("Unread messages", Map.of("count", count)));
    }

    private void requireChatMember(SharedRideBooking booking, User user) {
        boolean passenger = booking.getPassenger().getId().equals(user.getId());
        boolean creator = booking.getSharedRide().getCreator().getId().equals(user.getId());
        if (!passenger && !creator) {
            throw new IllegalStateException("Only the passenger and ride creator can use this chat");
        }
    }

    private User user(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private SharedRideBooking booking(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shared ride booking not found: id=" + id));
    }

    private MessageResponse response(SharedRideMessage message, Long currentUserId) {
        User sender = message.getSender();
        return MessageResponse.builder()
                .id(message.getId())
                .rideShareId(message.getBooking().getId())
                .senderId(sender.getId())
                .senderName(sender.getName())
                .senderUsername(sender.getUsername())
                .senderRole(sender.getRole().name())
                .senderProfileImage(sender.getProfileImage())
                .content(message.getContent())
                .read(message.isRead())
                .readAt(message.getReadAt())
                .sentAt(message.getSentAt())
                .mine(sender.getId().equals(currentUserId))
                .build();
    }
}
