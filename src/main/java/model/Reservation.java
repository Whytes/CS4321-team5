package model;

import java.time.LocalDate;
import java.time.LocalTime;

public final class Reservation {

    private final String reservationId;
    private final String spaceId;
    private final String ownerId;
    private final LocalDate date;
    private final LocalTime startTime;
    private final LocalTime endTime;

    public Reservation(
            String reservationId,
            String spaceId,
            String ownerId,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime) {
        this.reservationId = requireNonBlank(reservationId, "reservationId");
        this.spaceId = requireNonBlank(spaceId, "spaceId");
        this.ownerId = requireNonBlank(ownerId, "ownerId");

        if (date == null) {
            throw new IllegalArgumentException("date must not be null");
        }
        this.date = date;

        if (startTime == null) {
            throw new IllegalArgumentException("startTime must not be null");
        }
        if (endTime == null) {
            throw new IllegalArgumentException("endTime must not be null");
        }

        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("endTime must be after startTime");
        }

        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getReservationId() {
        return reservationId;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Reservation reservation)) {
            return false;
        }
        return reservationId.equals(reservation.reservationId);
    }

    @Override
    public int hashCode() {
        return reservationId.hashCode();
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
