package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

class ReservationUpdateServiceTest {

    private static final LocalDate DATE = LocalDate.of(2026, 10, 1);
    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-09-30T12:00:00Z"), ZoneOffset.UTC);

    @Test
    void updatesReservationInAvailableSlotAndPreservesId() {
        ReservationStore store = new ReservationStore();
        Reservation original = reservation(
                "reservation-1", "study-room-a",
                LocalTime.of(9, 0), LocalTime.of(10, 0));
        store.add(original);

        ReservationUpdateResult result = service(store).updateReservation(
                "reservation-1",
                "study-room-a",
                DATE,
                LocalTime.of(10, 0),
                LocalTime.of(11, 0));

        assertTrue(result.isSuccess());
        assertEquals("reservation-1", result.getReservation().getReservationId());
        assertEquals(LocalTime.of(10, 0), result.getReservation().getStartTime());
        assertEquals(LocalTime.of(11, 0), result.getReservation().getEndTime());
        assertEquals(result.getReservation(), store.getReservation("reservation-1"));
        assertEquals(1, store.getReservations().size());
    }

    @Test
    void rejectsUpdateThatConflictsWithAnotherReservation() {
        ReservationStore store = new ReservationStore();

        Reservation original = reservation(
                "reservation-1", "study-room-a",
                LocalTime.of(9, 0), LocalTime.of(10, 0));

        Reservation other = reservation(
                "reservation-2", "study-room-a",
                LocalTime.of(11, 0), LocalTime.of(12, 0));

        store.add(original);
        store.add(other);

        ReservationUpdateResult result = service(store).updateReservation(
                "reservation-1",
                "study-room-a",
                DATE,
                LocalTime.of(11, 30),
                LocalTime.of(12, 30));

        assertFalse(result.isSuccess());
        assertEquals(ReservationUpdateError.RESERVATION_CONFLICT, result.getError());
        assertTrue(original == store.getReservation("reservation-1"));
    }

    @Test
    void rejectsEndBeforeOrEqualToStartWithoutChangingReservation() {
        ReservationStore store = new ReservationStore();
        Reservation original = reservation(
                "reservation-1", "study-room-a",
                LocalTime.of(9, 0), LocalTime.of(10, 0));
        store.add(original);

        ReservationUpdateResult reversed = service(store).updateReservation(
                "reservation-1",
                "study-room-a",
                DATE,
                LocalTime.of(11, 0),
                LocalTime.of(10, 0));

        ReservationUpdateResult equal = service(store).updateReservation(
                "reservation-1",
                "study-room-a",
                DATE,
                LocalTime.of(11, 0),
                LocalTime.of(11, 0));

        assertFalse(reversed.isSuccess());
        assertEquals(
                ReservationUpdateError.INVALID_TIME_RANGE,
                reversed.getError());

        assertFalse(equal.isSuccess());
        assertEquals(
                ReservationUpdateError.INVALID_TIME_RANGE,
                equal.getError());

        assertTrue(original == store.getReservation("reservation-1"));
    }

    @Test
    void rejectsPastUpdateWithoutChangingReservation() {
        ReservationStore store = new ReservationStore();
        Reservation original = reservation(
                "reservation-1", "study-room-a",
                LocalTime.of(9, 0), LocalTime.of(10, 0));
        store.add(original);

        ReservationUpdateResult result = service(store).updateReservation(
                "reservation-1",
                "study-room-a",
                LocalDate.of(2026, 9, 30),
                LocalTime.of(11, 0),
                LocalTime.of(12, 0));

        assertFalse(result.isSuccess());
        assertEquals(
                ReservationUpdateError.TIME_IN_PAST,
                result.getError());
        assertTrue(original == store.getReservation("reservation-1"));
    }

    @Test
    void allowsUpdateWhenOnlyOverlapIsItself() {
        ReservationStore store = new ReservationStore();
        Reservation original = reservation(
                "reservation-1", "study-room-a",
                LocalTime.of(9, 0), LocalTime.of(10, 0));
        store.add(original);

        ReservationUpdateResult result = service(store).updateReservation(
                "reservation-1",
                "study-room-a",
                DATE,
                LocalTime.of(9, 30),
                LocalTime.of(10, 30));

        assertTrue(result.isSuccess());
        assertEquals("reservation-1", result.getReservation().getReservationId());
        assertEquals(LocalTime.of(9, 30), result.getReservation().getStartTime());
        assertEquals(LocalTime.of(10, 30), result.getReservation().getEndTime());
        assertEquals(1, store.getReservations().size());
    }

    @Test
    void rejectsUpdateWhenReservationDoesNotExist() {
        ReservationStore store = new ReservationStore();

        ReservationUpdateResult result = service(store).updateReservation(
                "missing-reservation",
                "study-room-a",
                DATE,
                LocalTime.of(9, 0),
                LocalTime.of(10, 0));

        assertFalse(result.isSuccess());
        assertEquals(
                ReservationUpdateError.RESERVATION_NOT_FOUND,
                result.getError());
        assertTrue(store.getReservations().isEmpty());
    }

    @Test
    void rejectsMissingRequiredInformationWithoutChangingReservation() {
        ReservationStore store = new ReservationStore();
        Reservation original = reservation(
                "reservation-1", "study-room-a",
                LocalTime.of(9, 0), LocalTime.of(10, 0));
        store.add(original);

        ReservationUpdateResult result = service(store).updateReservation(
                "reservation-1",
                null,
                DATE,
                LocalTime.of(10, 0),
                LocalTime.of(11, 0));

        assertFalse(result.isSuccess());
        assertEquals(
                ReservationUpdateError.MISSING_REQUIRED_FIELD,
                result.getError());
        assertTrue(original == store.getReservation("reservation-1"));
    }

    @Test
    void rejectsUpdateWhenReservationBelongsToAnotherUser() {
        ReservationStore store = new ReservationStore();
        Reservation original = new Reservation(
                "reservation-1",
                "study-room-a",
                "other-user",
                DATE,
                LocalTime.of(9, 0),
                LocalTime.of(10, 0));
        store.add(original);

        ReservationUpdateResult result = service(store).updateReservation(
                "reservation-1",
                "study-room-a",
                DATE,
                LocalTime.of(10, 0),
                LocalTime.of(11, 0));

        assertFalse(result.isSuccess());
        assertEquals(
                ReservationUpdateError.NOT_RESERVATION_OWNER,
                        result.getError());
        assertTrue(original == store.getReservation("reservation-1"));
    }

    @Test
    void rejectsUnknownSpaceWithoutChangingReservation() {
        ReservationStore store = new ReservationStore();
        Reservation original = reservation(
                "reservation-1", "study-room-a",
                LocalTime.of(9, 0), LocalTime.of(10, 0));
        store.add(original);

        ReservationUpdateResult result = service(store).updateReservation(
                "reservation-1",
                "not-a-real-space",
                DATE,
                LocalTime.of(10, 0),
                LocalTime.of(11, 0));

        assertFalse(result.isSuccess());
        assertEquals(ReservationUpdateError.UNKNOWN_SPACE, result.getError());
        assertTrue(original == store.getReservation("reservation-1"));
    }
    
    private ReservationUpdateService service(ReservationStore store) {
        return new ReservationUpdateService(
            store, InitialSpaceCatalog.getDefaultSpaces(), CLOCK);
    }

    private Reservation reservation(
            String id,
            String spaceId,
            LocalTime startTime,
            LocalTime endTime) {
        return new Reservation(
                id,
                spaceId,
                "local-user",
                DATE,
                startTime,
                endTime);
    }
}