package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.BikeDTO;

import java.math.BigDecimal;
import java.util.List;

public interface BikeService {

    BikeDTO addBike(String username, BikeDTO bikeDTO);
    List<BikeDTO> getAllBikes();
    BikeDTO getBikeById(Long id);
    BikeDTO updateBike(String username, Long id, BikeDTO bikeDTO);
    void deleteBike(String username, Long id);

    List<BikeDTO> getAvailableBikes();
    List<BikeDTO> searchBikes(String location, String bikeType,
                              String status, BigDecimal minPrice, BigDecimal maxPrice);

    List<BikeDTO> getBikesByOwner(Long ownerId);
    long countBikesByOwner(Long ownerId);
    long countRentedBikesByOwner(Long ownerId);

    void updateBikeStatus(String username, Long bikeId, String newStatus);
    void updateBikeRating(Long bikeId, Double newRating);
}
