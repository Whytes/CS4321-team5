package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import model.Reservation;
import model.ReservationStore;

class ReservationControllerTest {

    @Test
    void cancelReservation_removesReservationFromSharedStore() {
        ReservationStore store = new ReservationStore();
        ReservationController controller = new ReservationController(store);

        Reservation reservation = new Reservation(
                "res-1",
                "space-1",
                "local-user",
                LocalDate.of(2026, 9, 23),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0));

        store.add(reservation);

        Reservation cancelled = controller.cancelReservation("res-1");

        assertEquals(reservation, cancelled);
        assertNull(store.getReservation("res-1"));
        assertEquals(0, controller.getMyReservations().size());
    }

    @Test
    void cancelReservation_rejectsReservationOwnedByAnotherUser() {
        ReservationStore store = new ReservationStore();
        ReservationController controller = new ReservationController(store);

        Reservation reservation = new Reservation(
                "res-1",
                "space-1",
                "other-user",
                LocalDate.of(2026, 9, 23),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0));

        store.add(reservation);

        assertThrows(
                IllegalArgumentException.class,
                () -> controller.cancelReservation("res-1"));

        assertEquals(reservation, store.getReservation("res-1"));
    }
}