package model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReservationStoreTest {

    private final LocalDate date = LocalDate.of(2026, 10, 1);

    private Reservation reservation(
            String id,
            String spaceId,
            LocalDate reservationDate,
            int startHour,
            int startMinute,
            int endHour,
            int endMinute) {

        return new Reservation(
                id,
                spaceId,
                "local-user",
                reservationDate,
                LocalTime.of(startHour, startMinute),
                LocalTime.of(endHour, endMinute));
    }

    @Test
    void detectsPartialOverlapAtBeginning() {
        ReservationStore store = new ReservationStore();

        store.add(reservation(
                "existing", "study-room-a", date,
                10, 0, 11, 0));

        Reservation proposed = reservation(
                "proposed", "study-room-a", date,
                9, 30, 10, 30);

        assertTrue(store.hasOverlap(proposed));
    }

    @Test
    void detectsPartialOverlapAtEnd() {
        ReservationStore store = new ReservationStore();

        store.add(reservation(
                "existing", "study-room-a", date,
                10, 0, 11, 0));

        Reservation proposed = reservation(
                "proposed", "study-room-a", date,
                10, 30, 11, 30);

        assertTrue(store.hasOverlap(proposed));
    }

    @Test
    void detectsProposedReservationInsideExistingReservation() {
        ReservationStore store = new ReservationStore();

        store.add(reservation(
                "existing", "study-room-a", date,
                10, 0, 12, 0));

        Reservation proposed = reservation(
                "proposed", "study-room-a", date,
                10, 30, 11, 0);

        assertTrue(store.hasOverlap(proposed));
    }

    @Test
    void detectsExistingReservationInsideProposedReservation() {
        ReservationStore store = new ReservationStore();

        store.add(reservation(
                "existing", "study-room-a", date,
                10, 30, 11, 0));

        Reservation proposed = reservation(
                "proposed", "study-room-a", date,
                10, 0, 12, 0);

        assertTrue(store.hasOverlap(proposed));
    }

    @Test
    void detectsIdenticalIntervals() {
        ReservationStore store = new ReservationStore();

        store.add(reservation(
                "existing", "study-room-a", date,
                10, 0, 11, 0));

        Reservation proposed = reservation(
                "proposed", "study-room-a", date,
                10, 0, 11, 0);

        assertTrue(store.hasOverlap(proposed));
    }

    @Test
    void allowsReservationEndingWhenExistingReservationStarts() {
        ReservationStore store = new ReservationStore();

        store.add(reservation(
                "existing", "study-room-a", date,
                10, 0, 11, 0));

        Reservation proposed = reservation(
                "proposed", "study-room-a", date,
                9, 0, 10, 0);

        assertFalse(store.hasOverlap(proposed));
    }

    @Test
    void allowsReservationStartingWhenExistingReservationEnds() {
        ReservationStore store = new ReservationStore();

        store.add(reservation(
                "existing", "study-room-a", date,
                10, 0, 11, 0));

        Reservation proposed = reservation(
                "proposed", "study-room-a", date,
                11, 0, 12, 0);

        assertFalse(store.hasOverlap(proposed));
    }

    @Test
    void allowsSameTimeForDifferentSpace() {
        ReservationStore store = new ReservationStore();

        store.add(reservation(
                "existing", "study-room-a", date,
                10, 0, 11, 0));

        Reservation proposed = reservation(
                "proposed", "study-room-b", date,
                10, 0, 11, 0);

        assertFalse(store.hasOverlap(proposed));
    }

    @Test
    void allowsSameTimeForDifferentDate() {
        ReservationStore store = new ReservationStore();

        store.add(reservation(
                "existing", "study-room-a", date,
                10, 0, 11, 0));

        Reservation proposed = reservation(
                "proposed", "study-room-a", date.plusDays(1),
                10, 0, 11, 0);

        assertFalse(store.hasOverlap(proposed));
    }

    @Test
    void excludesReservationByIdWhenCheckingOverlap() {
        ReservationStore store = new ReservationStore();

        store.add(reservation(
                "reservation-1", "study-room-a", date,
                10, 0, 11, 0));

        Reservation edited = reservation(
                "reservation-1", "study-room-a", date,
                10, 0, 11, 0);

        assertFalse(store.hasOverlap(edited, "reservation-1"));
    }
}
