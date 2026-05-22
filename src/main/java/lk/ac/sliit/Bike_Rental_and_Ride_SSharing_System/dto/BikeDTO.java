package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.Bike;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BikeDTO {

    private Long id;
    private String title;
    private String bikeType;
    private String location;
    private String description;
    private BigDecimal dailyPrice;
    private BigDecimal weeklyPrice;
    private BigDecimal monthlyPrice;
    private String status;
    private String photoUrl;
    private String photoUrls;
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public static BikeDTO fromEntity(Bike bike) {
        BikeDTO dto = new BikeDTO();
        dto.id = bike.getId();
        dto.title = bike.getTitle();
        dto.bikeType = bike.getBikeType().name();
        dto.location = bike.getLocation();
        dto.description = bike.getDescription();
        dto.dailyPrice = bike.getDailyPrice();
        dto.weeklyPrice = bike.getWeeklyPrice();
        dto.monthlyPrice = bike.getMonthlyPrice();
        dto.status = bike.getStatus().name();
        dto.photoUrl = bike.getPhotoUrl();
        dto.photoUrls = bike.getPhotoUrls();
        dto.ownerId = bike.getOwnerId();
        dto.ownerName = bike.getOwnerName();
        dto.ownerPhone = bike.getOwnerPhone();
        dto.latitude = bike.getLatitude();
        dto.longitude = bike.getLongitude();
        dto.yearOfManufacture = bike.getYearOfManufacture();
        dto.brand = bike.getBrand();
        dto.model = bike.getModel();
        dto.engineCC = bike.getEngineCC();
        dto.color = bike.getColor();
        dto.averageRating = bike.getAverageRating();
        dto.totalRentals = bike.getTotalRentals();
        dto.createdAt = bike.getCreatedAt();
        dto.updatedAt = bike.getUpdatedAt();
        return dto;
    }
}
