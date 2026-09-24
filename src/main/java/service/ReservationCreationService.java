package service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import model.Reservation;
import model.ReservationStore;
import model.Space;

public final class ReservationCreationService {

    private static final String LOCAL_USER_ID = "local-user";

    private final ReservationStore reservationStore;
    private final List<Space> spaces;
    private final Clock clock;

    public ReservationCreationService(
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

    public ReservationCreationResult createReservation(
            String spaceId,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime) {
        if (spaceId == null || spaceId.isBlank()
                || date == null || startTime == null || endTime == null) {
            return ReservationCreationResult.failure(
                    ReservationCreationError.MISSING_REQUIRED_FIELD,
                    "Space, date, start time, and end time are required");
        }

        boolean knownSpace = spaces.stream()
                .anyMatch(space -> space.getSpaceId().equals(spaceId));
        if (!knownSpace) {
            return ReservationCreationResult.failure(
                    ReservationCreationError.UNKNOWN_SPACE,
                    "The selected space does not exist");
        }

        if (!endTime.isAfter(startTime)) {
            return ReservationCreationResult.failure(
                    ReservationCreationError.INVALID_TIME_RANGE,
                    "End time must be after start time");
        }

        LocalDateTime start = LocalDateTime.of(date, startTime);
        if (!start.isAfter(LocalDateTime.now(clock))) {
            return ReservationCreationResult.failure(
                    ReservationCreationError.TIME_IN_PAST,
                    "Reservation start must be in the future");
        }

        Reservation reservation = new Reservation(
                UUID.randomUUID().toString(),
                spaceId,
                LOCAL_USER_ID,
                date,
                startTime,
                endTime);

        if (reservationStore.hasOverlap(reservation)) {
            return ReservationCreationResult.failure(
                    ReservationCreationError.RESERVATION_CONFLICT,
                    "The selected space is already reserved for that time");
        }

        reservationStore.put(reservation);
        return ReservationCreationResult.success(reservation);
    }
}