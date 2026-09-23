package model;

import org.junit.jupiter.api.Test;
import java.util.List;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReservationTest {

    @Test
    void createsReservationWithValidValues() {
        Reservation reservation = new Reservation(
                "reservation-1",
                "study-room-a",
                "local-user",
                LocalDate.of(2026, 10, 1),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0));

        assertEquals("reservation-1", reservation.getReservationId());
        assertEquals("study-room-a", reservation.getSpaceId());
        assertEquals("local-user", reservation.getOwnerId());
        assertEquals(LocalDate.of(2026, 10, 1), reservation.getDate());
        assertEquals(LocalTime.of(10, 0), reservation.getStartTime());
        assertEquals(LocalTime.of(11, 0), reservation.getEndTime());
    }

    @Test
    void rejectsMissingRequiredValues() {
        assertThrows(IllegalArgumentException.class,
                () -> new Reservation(null, "space", "owner", LocalDate.now(), LocalTime.NOON, LocalTime.of(13, 0)));
        assertThrows(IllegalArgumentException.class,
                () -> new Reservation(" ", "space", "owner", LocalDate.now(), LocalTime.NOON, LocalTime.of(13, 0)));
        assertThrows(IllegalArgumentException.class,
                () -> new Reservation("id", null, "owner", LocalDate.now(), LocalTime.NOON, LocalTime.of(13, 0)));
        assertThrows(IllegalArgumentException.class,
                () -> new Reservation("id", "space", null, LocalDate.now(), LocalTime.NOON, LocalTime.of(13, 0)));
        assertThrows(IllegalArgumentException.class,
                () -> new Reservation("id", "space", "owner", null, LocalTime.NOON, LocalTime.of(13, 0)));
        assertThrows(IllegalArgumentException.class,
                () -> new Reservation("id", "space", "owner", LocalDate.now(), null, LocalTime.of(13, 0)));
        assertThrows(IllegalArgumentException.class,
                () -> new Reservation("id", "space", "owner", LocalDate.now(), LocalTime.NOON, null));
    }

    @Test
    void rejectsNonPositiveDuration() {
        assertThrows(IllegalArgumentException.class,
                () -> new Reservation("id", "space", "owner",
                        LocalDate.of(2026, 10, 1),
                        LocalTime.of(10, 0),
                        LocalTime.of(10, 0)));

        assertThrows(IllegalArgumentException.class,
                () -> new Reservation("id", "space", "owner",
                        LocalDate.of(2026, 10, 1),
                        LocalTime.of(11, 0),
                        LocalTime.of(10, 0)));
    }

    @Test
    void allowsHistoricalReservationRecordsToLoad() {
        Reservation reservation = new Reservation(
                "old-reservation",
                "study-room-a",
                "local-user",
                LocalDate.of(2020, 1, 1),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0));

        assertEquals("old-reservation", reservation.getReservationId());
    }

    @Test
    void usesReservationIdAsStableIdentity() {
        Reservation first = new Reservation(
                "reservation-1",
                "study-room-a",
                "local-user",
                LocalDate.of(2026, 10, 1),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0));

        Reservation sameIdentity = new Reservation(
                "reservation-1",
                "study-room-b",
                "local-user",
                LocalDate.of(2026, 10, 2),
                LocalTime.of(12, 0),
                LocalTime.of(13, 0));

        Reservation differentIdentity = new Reservation(
                "reservation-2",
                "study-room-a",
                "local-user",
                LocalDate.of(2026, 10, 1),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0));

        assertEquals(first, sameIdentity);
        assertEquals(first.hashCode(), sameIdentity.hashCode());
        assertEquals(false, first.equals(differentIdentity));
    }
@Test
void findsReservationsForSpaceAndDateSortedByStartTime() {
    ReservationStore store = new ReservationStore();

    Reservation later = new Reservation(
            "later",
            "room-a",
            "owner-a",
            LocalDate.of(2026, 10, 1),
            LocalTime.of(13, 0),
            LocalTime.of(14, 0));

    Reservation earlier = new Reservation(
            "earlier",
            "room-a",
            "owner-a",
            LocalDate.of(2026, 10, 1),
            LocalTime.of(9, 0),
            LocalTime.of(10, 0));

    store.add(later);
    store.add(earlier);

    assertEquals(
            List.of(earlier, later),
            store.getReservationsForSpace(
                    "room-a",
                    LocalDate.of(2026, 10, 1)));
}
}