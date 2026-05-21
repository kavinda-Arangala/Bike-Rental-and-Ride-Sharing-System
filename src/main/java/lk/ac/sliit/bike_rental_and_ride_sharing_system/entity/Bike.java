package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity;

import jakarta.persistence.*;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.BikeStatus;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.BikeType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "bikes")
public class Bike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BikeType bikeType;

    @Column(nullable = false)
    private String location;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private BigDecimal dailyPrice;

    private BigDecimal weeklyPrice;
    private BigDecimal monthlyPrice;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BikeStatus status;

    @Column(length = 2048)
    private String photoUrl;

    @Column(columnDefinition = "TEXT")
    private String photoUrls;

    @Column(nullable = false)
    private Long ownerId;

    private String ownerName;
    private String ownerPhone;

    private Double latitude;
    private Double longitude;

    private Integer yearOfManufacture;
    private String brand;
    private String model;
    private String engineCC;
    private String color;

    private Double averageRating;
    private Integer totalRentals;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null)   status = BikeStatus.AVAILABLE;
        if (totalRentals == null) totalRentals = 0;
        if (averageRating == null) averageRating = 0.0;
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

}
