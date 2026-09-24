package controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import model.AvailabilitySlot;
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

    public List<Reservation> getReservationsForSpace(String spaceId, LocalDate date) {
        return reservationStore.getReservationsForSpace(spaceId, date);
    }

    public Reservation cancelReservation(String reservationId) {
        return new CancellationService(reservationStore).cancelReservation(reservationId);
    }

    /**
     * Returns the complete calendar day, including free intervals before, between,
     * and after reservations. No operating-hour restriction is imposed.
     */
    public List<AvailabilitySlot> getDailySchedule(String spaceId, LocalDate date) {
        List<Reservation> reservations = getReservationsForSpace(spaceId, date);
        List<AvailabilitySlot> schedule = new java.util.ArrayList<>();
        LocalTime cursor = LocalTime.MIN;

        for (Reservation reservation : reservations) {
            if (cursor.isBefore(reservation.getStartTime())) {
                schedule.add(AvailabilitySlot.available(cursor, reservation.getStartTime()));
            }
            cursor = cursor.isAfter(reservation.getEndTime())
                    ? cursor : reservation.getEndTime();

        if (cursor.isBefore(LocalTime.MAX)) {
            schedule.add(AvailabilitySlot.available(cursor, LocalTime.MAX));
        }
        return List.copyOf(schedule);
    }

    public ReservationStore getReservationStore() {
        return reservationStore;
    }
}