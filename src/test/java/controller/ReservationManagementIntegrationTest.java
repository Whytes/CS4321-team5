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
import service.ReservationUpdateError;
import service.ReservationUpdateResult;


class ReservationManagementIntegrationTest {

    private static final LocalDate RESERVATION_DATE = LocalDate.of(2026, 10, 1);
    private static final Clock FIXED_CLOCK = Clock.fixed(
        Instant.parse("2026-09-30T12:00:00Z"), ZoneOffset.UTC);

    private ReservationStore store;
    private ReservationController controller;

    @BeforeEach
    void setUp() {
        store = new ReservationStore();

        controller = new ReservationController(
            store, 
            List.of(
                new Space("room-a", "Room A", "Main Hall", 20, List.of()),
                new Space("room-b", "Room B", "Main Hall", 30, List.of())),
            FIXED_CLOCK);
    }

    // US-7 AT1: My Reservations displays the local user's reservations
    // sorted by date and start time.
    @Test
    void myReservationsAreSortedByDateAndStartTime() {
        store.add(new Reservation(
                "later-date", "room-a", ReservationController.LOCAL_USER_ID,
                RESERVATION_DATE.plusDays(1),
            LocalTime.of(9,0), LocalTime.of(10,0)));

        store.add(new Reservation(
                "later-time", "room-b", ReservationController.LOCAL_USER_ID,
                RESERVATION_DATE,
                LocalTime.of(13,0), LocalTime.of(14,0)));

        store.add(new Reservation(
                "earlier-time", "room-a", ReservationController.LOCAL_USER_ID,
                RESERVATION_DATE,
                LocalTime.of(9,0), LocalTime.of(10,0)));

        List<Reservation> results = controller.getMyReservations();

        assertEquals(
                List.of("earlier-time", "later-time", "later-date"),
                results.stream()
                        .map(Reservation::getReservationId)
                        .toList());
    }

    // US-7 AT2: With no reservations, My Reservations returns an empty list.
    // The helpful empty-state message is verified at the view level.
    @Test
    void myReservationsReturnsEmptyListWhenNoReservations() {
        List<Reservation> results = controller.getMyReservations();

        assertTrue(results.isEmpty());
    }

    // US-8 AT1: Changing an existing reservation to an available time
    // updates the reservation
    @Test 
    void updateReservationSucceedsForAvailableTime() {
        Reservation original = new Reservation(
                "reservation-1", 
                "room-a", 
                ReservationController.LOCAL_USER_ID, 
                RESERVATION_DATE,
                LocalTime.of(9,0), 
                LocalTime.of(10,0));

        store.add(original);

        ReservationUpdateResult result = controller.updateReservation(
                "reservation-1", 
                "room-a", 
                RESERVATION_DATE,
                LocalTime.of(10,0), 
                LocalTime.of(11,0));

        assertTrue(result.isSuccess());

        Reservation updated = store.getReservation("reservation-1");

        assertEquals("reservation-1", updated.getReservationId());
        assertEquals(LocalTime.of(10,0), updated.getStartTime());
        assertEquals(LocalTime.of(11,0), updated.getEndTime());
        assertEquals(1, store.getReservations().size());
    }

    // US-8 AT2: Changing a reservation to a time occupied by another
    // reservation is rejected with a conflict error. 
    @Test 
    void updateReservationRejectsConflictingTime() {
        Reservation original = new Reservation(
                "reservation-1", 
                "room-a", 
                ReservationController.LOCAL_USER_ID, 
                RESERVATION_DATE,
                LocalTime.of(9,0), 
                LocalTime.of(10,0));

        Reservation conflicting = new Reservation(
                "reservation-2", 
                "room-a", 
                ReservationController.LOCAL_USER_ID, 
                RESERVATION_DATE,
                LocalTime.of(10,0), 
                LocalTime.of(12,0));

        store.add(original);
        store.add(conflicting);

        ReservationUpdateResult result = controller.updateReservation(
                "reservation-1", 
                "room-a", 
                RESERVATION_DATE,
                LocalTime.of(11, 30), 
                LocalTime.of(12, 30));

        assertFalse(result.isSuccess());
        assertEquals(ReservationUpdateError.RESERVATION_CONFLICT, result.getError());

        Reservation unchanged = store.getReservation("reservation-1");

        assertEquals(LocalTime.of(9,0), unchanged.getStartTime());
        assertEquals(LocalTime.of(10,0), unchanged.getEndTime());
        assertEquals(2, store.getReservations().size());
    }

    // US-8 AT3: Changing a reservation so the end time if before
    // the start time is rejected with a validation error. 
    @Test
    void updateReservationRejectsEndBeforeStart() {
        Reservation original = new Reservation(
                "reservation-1", 
                "room-a", 
                ReservationController.LOCAL_USER_ID, 
                RESERVATION_DATE,
                LocalTime.of(9,0), 
                LocalTime.of(10,0));

        store.add(original);

        ReservationUpdateResult result = controller.updateReservation(
                "reservation-1", 
                "room-a", 
                RESERVATION_DATE,
                LocalTime.of(11,0), 
                LocalTime.of(10,0));

        assertFalse(result.isSuccess());
        assertEquals(ReservationUpdateError.INVALID_TIME_RANGE, result.getError());

        Reservation unchanged = store.getReservation("reservation-1");

        assertEquals(LocalTime.of(9,0), unchanged.getStartTime());
        assertEquals(LocalTime.of(10,0), unchanged.getEndTime());
    }

    // US-8 AT4: Changing a reservation to a past time is rejected
    // with an explanatory error.
    @Test 
    void updateReservationRejectsPastTime() {
        Reservation original = new Reservation(
                "reservation-1", 
                "room-a", 
                ReservationController.LOCAL_USER_ID, 
                RESERVATION_DATE,
                LocalTime.of(9,0), 
                LocalTime.of(10,0));

        store.add(original);

        ReservationUpdateResult result = controller.updateReservation(
                "reservation-1", 
                "room-a", 
                LocalDate.of(2026, 9, 29),
                LocalTime.of(9,0), 
                LocalTime.of(10,0));

        assertFalse(result.isSuccess());
        assertEquals(ReservationUpdateError.TIME_IN_PAST, result.getError());

        Reservation unchanged = store.getReservation("reservation-1");

        assertEquals(RESERVATION_DATE, unchanged.getDate());
        assertEquals(LocalTime.of(9,0), unchanged.getStartTime());
        assertEquals(LocalTime.of(10,0), unchanged.getEndTime());
    }

    // US-9 AT1: Cancelling an existing reservation remove it
    // and makes the reserved time available again.
    @Test 
    void cancellationRemovesReservationAndReleasesTime() {
        Reservation original = new Reservation(
                "reservation-1", 
                "room-a", 
                ReservationController.LOCAL_USER_ID, 
                RESERVATION_DATE,
                LocalTime.of(9,0), 
                LocalTime.of(10,0));

        store.add(original);


        Reservation cancelled = controller.cancelReservation("reservation-1");

        assertEquals(original, cancelled);
        assertTrue(controller.getMyReservations().isEmpty());

        var replacement = controller.createReservation(
                "room-a", 
                RESERVATION_DATE, 
                LocalTime.of(9, 0),
                LocalTime.of(10, 0));

        assertTrue(replacement.isSuccess());
        assertEquals(1, store.getReservations().size());
    }
}
