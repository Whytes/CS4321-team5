package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.Reservation;
import model.ReservationStore;

class CancellationServiceTest {

    private ReservationStore reservationStore;
    private CancellationService cancellationService;

    @BeforeEach
    void setUp() {
        reservationStore = new ReservationStore();
        cancellationService = new CancellationService(reservationStore);
    }

    @Test
    void cancelReservation_removesExistingReservation() {
        Reservation reservation = new Reservation(
                "res-1",
                "space-1",
                "user-1",
                LocalDate.of(2026, 9, 23),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0));

        reservationStore.add(reservation);

        Reservation cancelled = cancellationService.cancelReservation("res-1");

        assertEquals(reservation, cancelled);
        assertNull(reservationStore.getReservation("res-1"));
    }

    @Test
    void cancelReservation_returnsNullForMissingReservation() {
        Reservation cancelled =
                cancellationService.cancelReservation("missing-id");

        assertNull(cancelled);
    }

    @Test
    void cancelReservation_rejectsBlankId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> cancellationService.cancelReservation(""));
    }

    @Test
    void cancelReservation_releasesTimeForAnotherBooking() {
        Reservation original = new Reservation(
                "res-1",
                "space-1",
                "user-1",
                LocalDate.of(2026, 9, 23),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0));

        reservationStore.add(original);
        cancellationService.cancelReservation("res-1");

        Reservation replacement = new Reservation(
                "res-2",
                "space-1",
                "user-2",
                LocalDate.of(2026, 9, 23),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0));

        reservationStore.add(replacement);

        assertEquals(replacement, reservationStore.getReservation("res-2"));
    }
}