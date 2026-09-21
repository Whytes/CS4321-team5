package model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Shared in-memory reservation state for one application session.
 */
public final class ReservationStore {

    private final List<Reservation> reservations = new ArrayList<>();

    public List<Reservation> getReservations() {
        return List.copyOf(reservations);
    }

    public List<Reservation> getReservationsForOwner(String ownerId) {
        return reservations.stream()
                .filter(reservation -> reservation.getOwnerId().equals(ownerId))
                .sorted(Comparator.comparing(Reservation::getDate)
                        .thenComparing(Reservation::getStartTime))
                .toList();
    }

    public void add(Reservation reservation) {
        if (reservation == null) {
            throw new IllegalArgumentException("reservation must not be null");
        }
        reservations.add(reservation);
    }
}