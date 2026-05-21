package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.util;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.Bike;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class PricingEngine {

    private static final BigDecimal MIN_FARE = BigDecimal.valueOf(50.0);

    // ── Main Fare Calculator ──────────────────────────────────────────────────

    /**
     * Calculate fare based on actual duration using daily rate.
     */
    public BigDecimal calculateFare(Bike bike, LocalDateTime startTime,
                                    LocalDateTime endTime) {
        validateTimes(startTime, endTime);

        double hours = getHours(startTime, endTime);
        double days = hours / 24.0;

        BigDecimal fare;

        // Use best available pricing tier
        if (days >= 30 && bike.getMonthlyPrice() != null) {
            double months = days / 30.0;
            fare = bike.getMonthlyPrice()
                    .multiply(BigDecimal.valueOf(months));
        } else if (days >= 7 && bike.getWeeklyPrice() != null) {
            double weeks = days / 7.0;
            fare = bike.getWeeklyPrice()
                    .multiply(BigDecimal.valueOf(weeks));
        } else {
            // Default — daily rate prorated by hours
            fare = bike.getDailyPrice()
                    .multiply(BigDecimal.valueOf(days));
        }

        return fare.setScale(2, RoundingMode.HALF_UP)
                .max(MIN_FARE);
    }

    /**
     * Estimate fare for advance booking using planned duration.
     */
    public BigDecimal estimateFare(Bike bike, LocalDateTime plannedStart,
                                   LocalDateTime plannedEnd) {
        return calculateFare(bike, plannedStart, plannedEnd);
    }

    /**
     * Estimate fare using estimated hours (when exact times unknown).
     */
    public BigDecimal estimateFareByHours(Bike bike, double estimatedHours) {
        if (estimatedHours <= 0) {
            throw new IllegalArgumentException("Estimated hours must be greater than 0");
        }
        double days = estimatedHours / 24.0;
        BigDecimal fare = bike.getDailyPrice()
                .multiply(BigDecimal.valueOf(days))
                .setScale(2, RoundingMode.HALF_UP);
        return fare.max(MIN_FARE);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public double getHours(LocalDateTime start, LocalDateTime end) {
        Duration duration = Duration.between(start, end);
        if (duration.isNegative()) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        return duration.toMinutes() / 60.0;
    }

    public BigDecimal getMinFare() {
        return MIN_FARE;
    }

    private void validateTimes(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end times cannot be null");
        }
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
    }
}