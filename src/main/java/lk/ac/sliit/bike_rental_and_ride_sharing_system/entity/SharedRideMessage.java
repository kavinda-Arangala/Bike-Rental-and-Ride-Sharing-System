package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "shared_ride_messages", indexes = {
        @Index(name = "idx_shared_message_booking", columnList = "shared_ride_booking_id"),
        @Index(name = "idx_shared_message_sender", columnList = "sender_id"),
        @Index(name = "idx_shared_message_sent_at", columnList = "sent_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedRideMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_ride_booking_id", nullable = false)
    private SharedRideBooking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean read = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @CreationTimestamp
    @Column(name = "sent_at", updatable = false)
    private LocalDateTime sentAt;
}
