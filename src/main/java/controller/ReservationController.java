package controller;

import java.util.List;

import model.Reservation;
import model.ReservationStore;
import service.CancellationService;

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

    public Reservation cancelReservation(String reservationId) {
        Reservation reservation = reservationStore.getReservation(reservationId);

        if (reservation == null) {
            return null;
        }

        if (!LOCAL_USER_ID.equals(reservation.getOwnerId())) {
            throw new IllegalArgumentException(
                    "reservation does not belong to local-user");
        }

        return new CancellationService(reservationStore)
                .cancelReservation(reservationId);
    }

    public ReservationStore getReservationStore() {
        return reservationStore;
    }
}