package model;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Shared in-memory reservation state for one application session.
 */
public final class ReservationStore {

    private final Map<String, Reservation> reservationsById = new LinkedHashMap<>();

    public List<Reservation> getReservations() {
        return List.copyOf(reservationsById.values());
    }

    public Reservation getReservation(String reservationId) {
        requireNonBlank(reservationId, "reservationId");
        return reservationsById.get(reservationId);
    }

    public List<Reservation> getReservationsForSpace(String spaceId, LocalDate date) {
        requireNonBlank(spaceId, "spaceId");

        if (date == null) {
            throw new IllegalArgumentException("date must not be null");
        }

        return reservationsById.values().stream()
                .filter(reservation -> reservation.getSpaceId().equals(spaceId)
                        && reservation.getDate().equals(date))
                .sorted(Comparator.comparing(Reservation::getStartTime)
                        .thenComparing(Reservation::getReservationId))
                .toList();
    }

    public List<Reservation> getReservationsForOwner(String ownerId) {
        requireNonBlank(ownerId, "ownerId");

        return reservationsById.values().stream()
                .filter(reservation -> reservation.getOwnerId().equals(ownerId))
                .sorted(Comparator.comparing(Reservation::getDate)
                        .thenComparing(Reservation::getStartTime)
                        .thenComparing(Reservation::getReservationId))
                .toList();
    }

    public void add(Reservation reservation) {
        put(reservation);
    }

    public void put(Reservation reservation) {
        if (reservation == null) {
            throw new IllegalArgumentException("reservation must not be null");
        }

        if (hasOverlap(reservation)) {
            throw new IllegalArgumentException("reservation overlaps an existing booking");
        }
        reservationsById.put(reservation.getReservationId(), reservation);
    }

    public void replace(Reservation reservation) {
        if (reservation == null) {
            throw new IllegalArgumentException("reservation must not be null");
        }

        if (!reservationsById.containsKey(reservation.getReservationId())) {
            throw new IllegalArgumentException("reservation does not exist");
        }

        if (hasOverlap(reservation, reservation.getReservationId())) {
            throw new IllegalArgumentException("reservation overlaps an existing booking");
        }

        reservationsById.put(reservation.getReservationId(), reservation);
    }

    public Reservation remove(String reservationId) {
        requireNonBlank(reservationId, "reservationId");
        return reservationsById.remove(reservationId);
    }

    public void loadSnapshot(Collection<Reservation> snapshot) {
        if (snapshot == null) {
            throw new IllegalArgumentException("snapshot must not be null");
        }

        Map<String, Reservation> loaded = new LinkedHashMap<>();

        for (Reservation reservation : snapshot) {
            if (reservation == null) {
                throw new IllegalArgumentException(
                        "snapshot must not contain null reservations");
            }

            if (loaded.put(reservation.getReservationId(), reservation) != null) {
                throw new IllegalArgumentException(
                        "snapshot contains duplicate reservation ID: "
                                + reservation.getReservationId());
            }
        }

        reservationsById.clear();
        reservationsById.putAll(loaded);
    }

    public List<Reservation> exportSnapshot() {
        return getReservations();
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }

    public boolean hasOverlap(Reservation proposed) {
        return hasOverlap(proposed, null);
    }
    
    public boolean hasOverlap(Reservation proposed, String excludedReservationId) {
        for (Reservation existing : reservationsById.values()) {
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