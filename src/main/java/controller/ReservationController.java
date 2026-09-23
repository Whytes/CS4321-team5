package controller;

import java.time.LocalDate;
import java.util.List;

import model.Reservation;
import model.ReservationStore;

/**
 * Coordinates reservation queries against the application's shared store.
 */
public final class ReservationController {

    public static final String LOCAL_USER_ID = "local-user";

    private final ReservationStore reservationStore;

    public ReservationController(ReservationStore reservationStore) {
        if (reservationStore == null) {
            throw new IllegalArgumentException("reservationStore must not be null");
        }
        this.reservationStore = reservationStore;
    }

    public List<Reservation> getMyReservations() {
        return reservationStore.getReservationsForOwner(LOCAL_USER_ID);
    }

    public List<Reservation> getReservationsForSpace(String spaceId, LocalDate date) {
        return reservationStore.getReservationsForSpace(spaceId, date);
    }

    public ReservationStore getReservationStore() {
        return reservationStore;
    }
}