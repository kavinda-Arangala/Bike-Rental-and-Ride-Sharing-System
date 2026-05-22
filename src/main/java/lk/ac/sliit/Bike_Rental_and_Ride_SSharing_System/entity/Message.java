package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages", indexes = {
        @Index(name = "idx_message_rideshare", columnList = "ride_share_id"),
        @Index(name = "idx_message_sender",    columnList = "sender_id"),
        @Index(name = "idx_message_sent_at",   columnList = "sent_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Relationships ─────────────────────────────────────────────────────────

    /**
     * The ride-share conversation this message belongs to.
     * Chat is scoped to one ride-share (one rental).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ride_share_id", nullable = false)
    private RideShare rideShare;

    /**
     * The user who sent this message (either the rider or the bike owner).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    // ── Content ───────────────────────────────────────────────────────────────

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    // ── Read status ───────────────────────────────────────────────────────────

    /**
     * True once the OTHER party has fetched/seen this message.
     */
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean read = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    // ── Audit ─────────────────────────────────────────────────────────────────

    @CreationTimestamp
    @Column(name = "sent_at", updatable = false)
    private LocalDateTime sentAt;
}