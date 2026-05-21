package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity;

import jakarta.persistence.*;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.NotificationType;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.NotificationChannel;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Who receives this notification
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    // Optional reference to related entity
    private Long referenceId;       // e.g. rentalId, paymentId
    private String referenceType;   // e.g. "RENTAL", "PAYMENT"

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean read = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean emailSent = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

}