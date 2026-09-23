package service;

import model.Reservation;
import model.ReservationStore;

public final class CancellationService {

    private final ReservationStore reservationStore;

    public CancellationService(ReservationStore reservationStore) {
        if (reservationStore == null) {
            throw new IllegalArgumentException("reservationStore must not be null");
        }

        this.reservationStore = reservationStore;
    }

    public Reservation cancelReservation(String reservationId) {
        if (reservationId == null || reservationId.isBlank()) {
            throw new IllegalArgumentException("reservationId must not be blank");
        }

        return reservationStore.remove(reservationId);
    }
}