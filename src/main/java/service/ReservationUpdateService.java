package service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import model.Reservation;
import model.ReservationStore;
import model.Space;

public final class ReservationUpdateService {

    private final ReservationStore reservationStore;
    private final List<Space> spaces;
    private final Clock clock;

    public ReservationUpdateService(
            ReservationStore reservationStore,
            List<Space> spaces,
            Clock clock) {
        if (reservationStore == null) {
            throw new IllegalArgumentException("reservationStore must not be null");
        }
        if (spaces == null) {
            throw new IllegalArgumentException("spaces must not be null");
        }
        if (clock == null) {
            throw new IllegalArgumentException("clock must not be null");
        }

        this.reservationStore = reservationStore;
        this.spaces = List.copyOf(spaces);
        this.clock = clock;
    }

    public ReservationUpdateResult updateReservation(
            String reservationId,
            String spaceId,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime) {

        if (reservationId == null || reservationId.isBlank()) {
            return ReservationUpdateResult.failure(
                    ReservationUpdateError.RESERVATION_NOT_FOUND,
                    "Reservation does not exist");
        }

        Reservation existing = reservationStore.getReservation(reservationId);
        if (existing == null) {
            return ReservationUpdateResult.failure(
                    ReservationUpdateError.RESERVATION_NOT_FOUND,
                    "Reservation does not exist");
        }

        if (!"local-user".equals(existing.getOwnerId())) {
            return ReservationUpdateResult.failure(
                    ReservationUpdateError.NOT_RESERVATION_OWNER,
                    "Reservation does not belong to local-user");
        }

        if (spaceId == null || spaceId.isBlank()
                || date == null || startTime == null || endTime == null) {
            return ReservationUpdateResult.failure(
                    ReservationUpdateError.MISSING_REQUIRED_FIELD,
                    "Space, date, start time, and end time are required");
        }

        boolean knownSpace = spaces.stream()
                .anyMatch(space -> space.getSpaceId().equals(spaceId));
        if (!knownSpace) {
            return ReservationUpdateResult.failure(
                    ReservationUpdateError.UNKNOWN_SPACE,
                    "The selected space does not exist");
        }

        if (!endTime.isAfter(startTime)) {
            return ReservationUpdateResult.failure(
                    ReservationUpdateError.INVALID_TIME_RANGE,
                    "End time must be after start time");
        }

        LocalDateTime start = LocalDateTime.of(date, startTime);
        if (!start.isAfter(LocalDateTime.now(clock))) {
            return ReservationUpdateResult.failure(
                    ReservationUpdateError.TIME_IN_PAST,
                    "Reservation start must be in the future");
        }

        Reservation candidate = new Reservation(
                existing.getReservationId(),
                spaceId,
                existing.getOwnerId(),
                date,
                startTime,
                endTime);

        if (reservationStore.hasOverlap(candidate, existing.getReservationId())) {
            return ReservationUpdateResult.failure(
                    ReservationUpdateError.RESERVATION_CONFLICT,
                    "The selected space is already reserved for that time");
        }

        reservationStore.replace(candidate);
        return ReservationUpdateResult.success(candidate);
    }
}
