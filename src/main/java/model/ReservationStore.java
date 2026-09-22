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
        if (hasOverlap(reservation)) {
            throw new IllegalArgumentException("reservation overlaps an existing booking");
        }
        reservations.add(reservation);
    }

    public boolean hasOverlap(Reservation proposed) {
        for (Reservation existing : reservations) {
            boolean sameSpace = existing.getSpaceId().equals(proposed.getSpaceId());
            boolean sameDate = existing.getDate().equals(proposed.getDate());
            boolean timesOverlap = existing.getStartTime().isBefore(proposed.getEndTime())
                    && proposed.getStartTime().isBefore(existing.getEndTime());
            if (sameSpace && sameDate && timesOverlap) {
                return true;
            }
        }
        return false;
    }
    
    public boolean hasOverlap(Reservation proposed, String excludedReservationId) {
        for (Reservation existing : reservations) {
            if (existing.getReservationId().equals(excludedReservationId)) {
                continue; // Skip the reservation with the excluded ID
            }
            boolean sameSpace = existing.getSpaceId().equals(proposed.getSpaceId());
            boolean sameDate = existing.getDate().equals(proposed.getDate());
            boolean timesOverlap = existing.getStartTime().isBefore(proposed.getEndTime())
                    && proposed.getStartTime().isBefore(existing.getEndTime());
            if (sameSpace && sameDate && timesOverlap) {
                return true;
            }
        }
        return false;
    }   
}