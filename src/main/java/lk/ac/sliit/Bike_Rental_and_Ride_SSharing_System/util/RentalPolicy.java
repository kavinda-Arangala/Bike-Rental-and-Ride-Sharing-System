package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.util;

/**
 * Central place for all rental business-rule constants.
 * Change values here to adjust policy across the whole system.
 */
public final class RentalPolicy {

    private RentalPolicy() {} // utility class — no instances

    /** Minimum minutes ahead a booking must be made */
    public static final long MIN_BOOKING_LEAD_MINUTES = 30;

    /** Minimum rental duration in hours */
    public static final long MIN_RENTAL_HOURS = 1;

    /** Maximum rental duration in days */
    public static final long MAX_RENTAL_DAYS = 30;

    /** Maximum days in advance a booking can be made */
    public static final long MAX_ADVANCE_BOOKING_DAYS = 90;

    /** Hours before start time where cancellation is completely free */
    public static final long FREE_CANCEL_HOURS = 24;

    /** Hours before start time where cancellation is blocked entirely */
    public static final long NO_CANCEL_HOURS = 1;

    /** Fraction of estimated fare charged as late cancellation fee (e.g. 0.20 = 20%) */
    public static final double LATE_CANCEL_FEE_PCT = 0.20;
}