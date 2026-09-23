package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import model.Reservation;
import model.ReservationStore;

class ReservationControllerTest {

    @Test
    void dailyAvailabilityReturnsOnlySelectedSpaceAndDateInStartTimeOrder() {
        ReservationStore store = new ReservationStore();
        ReservationController controller = new ReservationController(store);
        LocalDate date = LocalDate.of(2026, 10, 1);

        store.add(new Reservation("later", "study-room-a", "local-user",
                date, LocalTime.of(11, 0), LocalTime.of(12, 0)));
        store.add(new Reservation("earlier", "study-room-a", "local-user",
                date, LocalTime.of(9, 0), LocalTime.of(10, 0)));
        store.add(new Reservation("other-space", "study-room-b", "local-user",
                date, LocalTime.of(8, 0), LocalTime.of(9, 0)));
        store.add(new Reservation("other-date", "study-room-a", "local-user",
                date.plusDays(1), LocalTime.of(8, 0), LocalTime.of(9, 0)));

        var results = controller.getReservationsForSpace("study-room-a", date);

        assertEquals(2, results.size());
        assertEquals("earlier", results.get(0).getReservationId());
        assertEquals("later", results.get(1).getReservationId());
    }

    @Test
    void dailyAvailabilityReturnsEmptyListWhenNoReservationsMatch() {
        ReservationController controller =
                new ReservationController(new ReservationStore());

        var results = controller.getReservationsForSpace(
                "study-room-a", LocalDate.of(2026, 10, 1));

        assertTrue(results.isEmpty());
    }

    @Test
    void dailyAvailabilityRejectsBlankSpaceId() {
        ReservationController controller =
                new ReservationController(new ReservationStore());

        assertThrows(IllegalArgumentException.class,
                () -> controller.getReservationsForSpace(
                        " ", LocalDate.of(2026, 10, 1)));
    }

    @Test
    void dailyAvailabilityRejectsNullDate() {
        ReservationController controller =
                new ReservationController(new ReservationStore());

        assertThrows(IllegalArgumentException.class,
                () -> controller.getReservationsForSpace(
                        "study-room-a", null));
    }
}
