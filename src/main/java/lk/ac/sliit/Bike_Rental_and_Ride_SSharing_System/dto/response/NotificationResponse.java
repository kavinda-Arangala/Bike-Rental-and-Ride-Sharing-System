package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {

    private Long id;
    private Long userId;
    private String username;

    private String type;
    private String channel;
    private String title;
    private String message;

    private Long referenceId;
    private String referenceType;

    private boolean read;
    private boolean emailSent;

    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}