package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.Reservation;
import model.ReservationStore;
import model.Space;
import service.ReservationCreationError;
import service.ReservationCreationResult;

/**
 * Integration coverage for availability and reservation creation through the
 * real controller and its collaborating services.
 */
class ReservationControllerIntegrationTest {

    private static final LocalDate RESERVATION_DATE = LocalDate.of(2026, 10, 1);
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-09-30T12:00:00Z"), ZoneOffset.UTC);

    private ReservationStore store;
    private ReservationController controller;

    @BeforeEach
    void setUp() {
        store = new ReservationStore();
        controller = new ReservationController(store, List.of(
                new Space("room-a", "Room A", "Main Hall", 20, List.of()),
                new Space("room-b", "Room B", "Main Hall", 30, List.of())), FIXED_CLOCK);
    }

    // US-4 AT1: reservations for one space and day are returned in start-time order.
    @Test
    void reservationsAreOrderedForSelectedSpaceAndDay() {
        store.add(reservation("late", "room-a", RESERVATION_DATE, 13, 14));
        store.add(reservation("early", "room-a", RESERVATION_DATE, 9, 10));
        store.add(reservation("other-space", "room-b", RESERVATION_DATE, 8, 9));
        store.add(reservation("other-day", "room-a", RESERVATION_DATE.plusDays(1), 8, 9));

        assertEquals(List.of("early", "late"),
                controller.getReservationsForSpace("room-a", RESERVATION_DATE).stream()
                        .map(Reservation::getReservationId)
                        .toList());
    }

    // US-4 AT2: a day without reservations returns one fully available interval.
    @Test
    void emptyDayIsFullyAvailable() {
var schedule = controller.getDailySchedule("room-a", RESERVATION_DATE);
        assertEquals(1, schedule.size());
        assertFalse(schedule.get(0).reserved());
        assertEquals(LocalTime.MIN, schedule.get(0).startTime());
        assertEquals(LocalTime.MAX, schedule.get(0).endTime());
    }

    // US-6 AT1: a valid reservation is saved in the shared in-memory store.
    @Test
    void validReservationIsSavedAndReturnedByController() {
        ReservationCreationResult result =
                controller.createReservation("room-a", RESERVATION_DATE,
                        LocalTime.of(9, 0), LocalTime.of(10, 0));

        assertTrue(result.isSuccess());
        assertEquals(result.getReservation(),
                store.getReservation(result.getReservation().getReservationId()));
        assertEquals(List.of(result.getReservation()),
                controller.getReservationsForSpace("room-a", RESERVATION_DATE));
    }

    // US-6 AT2: an overlapping reservation is rejected with a conflict error.
    @Test
    void overlappingReservationIsRejected() {
        createReservation(10, 11);

        ReservationCreationResult result = controller.createReservation(
                "room-a", RESERVATION_DATE, LocalTime.of(10, 30), LocalTime.of(11, 30));

        assertFailure(result, ReservationCreationError.RESERVATION_CONFLICT);
        assertEquals(1, store.getReservations().size());
    }

    // US-6 AT3: a reservation beginning at the existing end time is accepted.
    @Test
    void adjacentReservationIsAccepted() {
        createReservation(10, 11);

        ReservationCreationResult result = controller.createReservation(
                "room-a", RESERVATION_DATE, LocalTime.of(11, 0), LocalTime.of(12, 0));

        assertTrue(result.isSuccess());
        assertEquals(2, store.getReservations().size());
    }

    // US-6 AT4: an end time before the start time is rejected.
    @Test
    void invalidTimeRangeIsRejected() {
        ReservationCreationResult result = controller.createReservation(
                "room-a", RESERVATION_DATE, LocalTime.of(12, 0), LocalTime.of(11, 0));

        assertFailure(result, ReservationCreationError.INVALID_TIME_RANGE);
    }

    // US-6 AT5: a reservation whose start is before the current clock time is rejected.
    @Test
    void reservationInPastIsRejected() {
        ReservationCreationResult result = controller.createReservation(
                "room-a", RESERVATION_DATE.minusDays(1),
                LocalTime.of(9, 0), LocalTime.of(10, 0));

        assertFailure(result, ReservationCreationError.TIME_IN_PAST);
    }

    // US-6 AT6: missing required reservation input is rejected without saving data.
    @Test
    void missingRequiredInputIsRejected() {
List<ReservationCreationResult> results = List.of(
                controller.createReservation(null, RESERVATION_DATE,
                        LocalTime.of(9, 0), LocalTime.of(10, 0)),
                controller.createReservation("room-a", null,
                        LocalTime.of(9, 0), LocalTime.of(10, 0)),
                controller.createReservation("room-a", RESERVATION_DATE,
                        null, LocalTime.of(10, 0)),
                controller.createReservation("room-a", RESERVATION_DATE,
                        LocalTime.of(9, 0), null));

        results.forEach(result ->
                assertFailure(result, ReservationCreationError.MISSING_REQUIRED_FIELD));
        assertTrue(store.getReservations().isEmpty());
    }

    private void createReservation(int startHour, int endHour) {
        ReservationCreationResult result = controller.createReservation(
                "room-a", RESERVATION_DATE,
                LocalTime.of(startHour, 0), LocalTime.of(endHour, 0));
        assertTrue(result.isSuccess());
    }

    private static Reservation reservation(
            String id, String spaceId, LocalDate date, int startHour, int endHour) {
        return new Reservation(id, spaceId, ReservationController.LOCAL_USER_ID, date,
                LocalTime.of(startHour, 0), LocalTime.of(endHour, 0));
    }

    private static void assertFailure(
            ReservationCreationResult result, ReservationCreationError expectedError) {
        assertFalse(result.isSuccess());
        assertEquals(expectedError, result.getError());
    }
}
