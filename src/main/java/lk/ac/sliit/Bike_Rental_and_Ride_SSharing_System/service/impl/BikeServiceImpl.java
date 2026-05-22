package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.impl;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.BikeDTO;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.Bike;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.User;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.BikeStatus;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.BikeType;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.UserRole;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.exception.ResourceNotFoundException;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.BikeRepository;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.UserRepository;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.BikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BikeServiceImpl implements BikeService {

    private final BikeRepository bikeRepository;
    private final UserRepository userRepository;

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public BikeDTO addBike(String username, BikeDTO dto) {
        User actor = findUserOrThrow(username);
        if (actor.getRole() == UserRole.BIKE_OWNER) {
            dto.setOwnerId(actor.getId());
            dto.setOwnerName(actor.getName());
            dto.setOwnerPhone(actor.getPhoneNumber());
        }
        validateBikeDTO(dto);

        Bike bike = new Bike();
        mapDtoToEntity(dto, bike);
        if (actor.getRole() != UserRole.ADMIN && actor.getRole() != UserRole.BIKE_OWNER) {
            throw new IllegalStateException("Only admins and bike owners can add bikes.");
        }

        Bike saved = bikeRepository.save(bike);
        log.info("Bike added: id={}, title={}", saved.getId(), saved.getTitle());
        return BikeDTO.fromEntity(saved);
    }

    @Override
    public List<BikeDTO> getAllBikes() {
        return bikeRepository.findAll()
                .stream()
                .map(BikeDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public BikeDTO getBikeById(Long id) {
        return BikeDTO.fromEntity(findByIdOrThrow(id));
    }

    @Override
    @Transactional
    public BikeDTO updateBike(String username, Long id, BikeDTO dto) {
        Bike bike = findByIdOrThrow(id);
        User actor = findUserOrThrow(username);
        requireBikeManager(actor, bike);
        Long originalOwnerId = bike.getOwnerId();
        mapDtoToEntity(dto, bike);
        if (actor.getRole() == UserRole.BIKE_OWNER) {
            bike.setOwnerId(originalOwnerId);
        }
        Bike updated = bikeRepository.save(bike);
        log.info("Bike updated: id={}", id);
        return BikeDTO.fromEntity(updated);
    }

    @Override
    @Transactional
    public void deleteBike(String username, Long id) {
        Bike bike = findByIdOrThrow(id);
        requireBikeManager(findUserOrThrow(username), bike);
        bikeRepository.delete(bike);
        log.info("Bike deleted: id={}", id);
    }

    // ── Search & Filter ───────────────────────────────────────────────────────

    @Override
    public List<BikeDTO> getAvailableBikes() {
        return bikeRepository.findByStatus(BikeStatus.AVAILABLE)
                .stream()
                .map(BikeDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<BikeDTO> searchBikes(String location, String bikeType,
                                     String status, BigDecimal minPrice,
                                     BigDecimal maxPrice) {
        BikeType type = (bikeType != null && !bikeType.isBlank())
                ? BikeType.valueOf(bikeType.toUpperCase()) : null;

        BikeStatus bikeStatus = (status != null && !status.isBlank())
                ? BikeStatus.valueOf(status.toUpperCase()) : null;

        return bikeRepository.searchBikes(location, type, bikeStatus, minPrice, maxPrice)
                .stream()
                .map(BikeDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // ── Owner ─────────────────────────────────────────────────────────────────

    @Override
    public List<BikeDTO> getBikesByOwner(Long ownerId) {
        return bikeRepository.findByOwnerId(ownerId)
                .stream()
                .map(BikeDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public long countBikesByOwner(Long ownerId) {
        return bikeRepository.countByOwnerId(ownerId);
    }

    @Override
    public long countRentedBikesByOwner(Long ownerId) {
        return bikeRepository.countByOwnerIdAndStatus(ownerId, BikeStatus.RENTED);
    }

    // ── Status & Rating ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public void updateBikeStatus(String username, Long bikeId, String newStatus) {
        Bike bike = findByIdOrThrow(bikeId);
        requireBikeManager(findUserOrThrow(username), bike);
        try {
            bike.setStatus(BikeStatus.valueOf(newStatus.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid status. Must be: AVAILABLE, RENTED, or MAINTENANCE");
        }
        bikeRepository.save(bike);
        log.info("Bike status updated: id={}, status={}", bikeId, newStatus);
    }

    @Override
    @Transactional
    public void updateBikeRating(Long bikeId, Double newRating) {
        if (newRating < 0 || newRating > 5) {
            throw new IllegalArgumentException("Rating must be between 0 and 5");
        }
        Bike bike = findByIdOrThrow(bikeId);
        bike.setAverageRating(newRating);
        bikeRepository.save(bike);
        log.info("Bike rating updated: id={}, rating={}", bikeId, newRating);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Bike findByIdOrThrow(Long id) {
        return bikeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bike not found with ID: " + id));
    }

    private User findUserOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private void requireBikeManager(User actor, Bike bike) {
        if (actor.getRole() == UserRole.ADMIN) {
            return;
        }
        if (actor.getRole() == UserRole.BIKE_OWNER && actor.getId().equals(bike.getOwnerId())) {
            return;
        }
        throw new IllegalStateException("You can only manage bikes that you own.");
    }

    private void mapDtoToEntity(BikeDTO dto, Bike bike) {
        bike.setTitle(dto.getTitle());
        bike.setBikeType(BikeType.valueOf(dto.getBikeType().toUpperCase()));
        bike.setLocation(dto.getLocation());
        bike.setDescription(dto.getDescription());
        bike.setDailyPrice(dto.getDailyPrice());
        bike.setWeeklyPrice(dto.getWeeklyPrice());
        bike.setMonthlyPrice(dto.getMonthlyPrice());
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            bike.setStatus(BikeStatus.valueOf(dto.getStatus().toUpperCase()));
        }
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

    private void validateBikeDTO(BikeDTO dto) {
        if (dto.getTitle() == null || dto.getTitle().isBlank())
            throw new IllegalArgumentException("Bike title is required.");
        if (dto.getBikeType() == null || dto.getBikeType().isBlank())
            throw new IllegalArgumentException("Bike type is required: CYCLE, SCOOTER, or MOTORBIKE.");
        if (dto.getLocation() == null || dto.getLocation().isBlank())
            throw new IllegalArgumentException("Location is required.");
        if (dto.getDailyPrice() == null || dto.getDailyPrice().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Daily price must be greater than 0.");
        if (dto.getOwnerId() == null)
            throw new IllegalArgumentException("Owner ID is required.");

        // Validate bikeType enum value
        try {
            BikeType.valueOf(dto.getBikeType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid bike type. Must be: CYCLE, SCOOTER, or MOTORBIKE.");
        }
    }
}
