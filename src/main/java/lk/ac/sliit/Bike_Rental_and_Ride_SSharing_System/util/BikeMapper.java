package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.util;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.BikeDTO;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.Bike;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.BikeStatus;  // ✅ correct import
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.BikeType;    // ✅ correct import
import org.springframework.stereotype.Component;

@Component
public class BikeMapper {

    /**
     * Convert BikeDTO → Bike entity
     * Used when ADDING a new bike (POST /api/bikes)
     */
    public Bike toEntity(BikeDTO dto) {
        Bike bike = new Bike();
        applyDto(dto, bike);
        return bike;
    }

    /**
     * Update an existing Bike entity from a BikeDTO
     * Used when EDITING a bike (PUT /api/bikes/{id})
     */
    public void updateEntityFromDto(BikeDTO dto, Bike bike) {
        applyDto(dto, bike);
    }

    // ── Private helper: copy DTO fields → entity ─────────────────
    private void applyDto(BikeDTO dto, Bike bike) {
        bike.setTitle(dto.getTitle());


        if (dto.getBikeType() != null)
            bike.setBikeType(BikeType.valueOf(dto.getBikeType().toUpperCase()));

        bike.setLocation(dto.getLocation());
        bike.setDescription(dto.getDescription());
        bike.setDailyPrice(dto.getDailyPrice());
        bike.setWeeklyPrice(dto.getWeeklyPrice());
        bike.setMonthlyPrice(dto.getMonthlyPrice());

        if (dto.getStatus() != null)
            bike.setStatus(BikeStatus.valueOf(dto.getStatus().toUpperCase()));

        bike.setPhotoUrl(dto.getPhotoUrl());
        bike.setPhotoUrls(dto.getPhotoUrls());
        bike.setOwnerId(dto.getOwnerId());
        bike.setOwnerName(dto.getOwnerName());
        bike.setOwnerPhone(dto.getOwnerPhone());
        bike.setLatitude(dto.getLatitude());
        bike.setLongitude(dto.getLongitude());
        bike.setYearOfManufacture(dto.getYearOfManufacture());
        bike.setBrand(dto.getBrand());
        bike.setModel(dto.getModel());
        bike.setEngineCC(dto.getEngineCC());
        bike.setColor(dto.getColor());
    }
}