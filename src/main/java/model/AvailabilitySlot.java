package model;

import java.time.LocalTime;

/**
 * One visible interval in a space's daily schedule.
 */
public record AvailabilitySlot(LocalTime startTime, LocalTime endTime, boolean reserved,
                               String reservationId) {

    public AvailabilitySlot {
        if (startTime == null || endTime == null || !endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("availability interval must have a positive duration");
        }
        if (reserved && (reservationId == null || reservationId.isBlank())) {
            throw new IllegalArgumentException("reserved slots require a reservation ID");
        }
        if (!reserved && reservationId != null) {
            throw new IllegalArgumentException("available slots cannot have a reservation ID");
        }
    }

    public static AvailabilitySlot available(LocalTime startTime, LocalTime endTime) {
        return new AvailabilitySlot(startTime, endTime, false, null);
    }

    public static AvailabilitySlot reserved(Reservation reservation) {
        if (reservation == null) {
            throw new IllegalArgumentException("reservation must not be null");
        }
        return new AvailabilitySlot(reservation.getStartTime(), reservation.getEndTime(),
                true, reservation.getReservationId());
    }

    public String getStatusLabel() {
        return reserved ? "Reserved" : "Available";
    }
}
