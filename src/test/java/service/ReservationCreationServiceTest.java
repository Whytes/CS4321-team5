package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import model.Reservation;
import model.ReservationStore;
import persistence.InitialSpaceCatalog;

class ReservationCreationServiceTest {

    private static final LocalDate DATE = LocalDate.of(2026, 10, 1);
    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-09-30T12:00:00Z"), ZoneOffset.UTC);

    @Test
    void createsReservationAndAddsExactlyOneReservation() {
        ReservationStore store = new ReservationStore();
        ReservationCreationResult result = service(store).createReservation(
                "study-room-a", DATE, LocalTime.of(9, 0), LocalTime.of(10, 0));

        assertTrue(result.isSuccess());
        assertNotNull(result.getReservation());
        assertEquals("local-user", result.getReservation().getOwnerId());
        assertEquals(1, store.getReservations().size());
    }

    @Test
    void rejectsMissingRequiredInformationWithoutChangingStore() {
        ReservationStore store = new ReservationStore();
        ReservationCreationResult result = service(store).createReservation(
                null, DATE, LocalTime.of(9, 0), LocalTime.of(10, 0));

        assertFailure(result, ReservationCreationError.MISSING_REQUIRED_FIELD);
        assertTrue(store.getReservations().isEmpty());
    }

    @Test
    void rejectsUnknownSpaceWithoutChangingStore() {
        ReservationStore store = new ReservationStore();
        ReservationCreationResult result = service(store).createReservation(
                "unknown-space", DATE, LocalTime.of(9, 0), LocalTime.of(10, 0));

        assertFailure(result, ReservationCreationError.UNKNOWN_SPACE);
        assertTrue(store.getReservations().isEmpty());
    }

    @Test
    void rejectsInvalidDurationBoundariesWithoutChangingStore() {
        ReservationStore store = new ReservationStore();

        ReservationCreationResult reversed = service(store).createReservation(
                "study-room-a", DATE, LocalTime.of(10, 0), LocalTime.of(9, 0));
        ReservationCreationResult equal = service(store).createReservation(
                "study-room-a", DATE, LocalTime.of(10, 0), LocalTime.of(10, 0));

        assertFailure(reversed, ReservationCreationError.INVALID_TIME_RANGE);
        assertFailure(equal, ReservationCreationError.INVALID_TIME_RANGE);
        assertTrue(store.getReservations().isEmpty());
    }

    @Test
    void rejectsPastAndSameDayCurrentStartsWithoutChangingStore() {
        ReservationStore store = new ReservationStore();

        ReservationCreationResult past = service(store).createReservation(
                "study-room-a", LocalDate.of(2026, 9, 30),
                LocalTime.of(11, 59), LocalTime.of(13, 0));
        ReservationCreationResult current = service(store).createReservation(
                "study-room-a", LocalDate.of(2026, 9, 30),
                LocalTime.of(12, 0), LocalTime.of(13, 0));

        assertFailure(past, ReservationCreationError.TIME_IN_PAST);
        assertFailure(current, ReservationCreationError.TIME_IN_PAST);
        assertTrue(store.getReservations().isEmpty());
    }

    @Test
    void rejectsConflictButAcceptsAdjacentReservation() {
        ReservationStore store = new ReservationStore();
        Reservation existing = new Reservation(
                "existing", "study-room-a", "local-user", DATE,
                LocalTime.of(9, 0), LocalTime.of(10, 0));
        store.add(existing);

        ReservationCreationResult conflict = service(store).createReservation(
                "study-room-a", DATE, LocalTime.of(9, 30), LocalTime.of(10, 30));
        ReservationCreationResult adjacent = service(store).createReservation(
                "study-room-a", DATE, LocalTime.of(10, 0), LocalTime.of(11, 0));

        assertFailure(conflict, ReservationCreationError.RESERVATION_CONFLICT);
        assertEquals(existing, store.getReservation("existing"));
        assertTrue(adjacent.isSuccess());
        assertEquals(2, store.getReservations().size());
    }

    private ReservationCreationService service(ReservationStore store) {
        return new ReservationCreationService(
                store, InitialSpaceCatalog.getDefaultSpaces(), CLOCK);
    }

    private void assertFailure(
            ReservationCreationResult result,
            ReservationCreationError expectedError) {
        assertFalse(result.isSuccess());
        assertEquals(expectedError, result.getError());
        assertNotNull(result.getMessage());
    }
}