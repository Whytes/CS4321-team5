package service;

import model.Reservation;

public final class ReservationUpdateResult {

    private final Reservation reservation;
    private final ReservationUpdateError error;
    private final String message;

    private ReservationUpdateResult(
            Reservation reservation,
            ReservationUpdateError error,
            String message) {
        this.reservation = reservation;
        this.error = error;
        this.message = message;
    }

    public static ReservationUpdateResult success(Reservation reservation) {
        if (reservation == null) {
            throw new IllegalArgumentException("reservation must not be null");
        }
        return new ReservationUpdateResult(
                reservation, null, "Reservation updated");
    }

    public static ReservationUpdateResult failure(
            ReservationUpdateError error,
            String message) {
        if (error == null) {
            throw new IllegalArgumentException("error must not be null");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }
        return new ReservationUpdateResult(null, error, message);
    }

    public boolean isSuccess() {
        return reservation != null;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public ReservationUpdateError getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }
}