package service;

import model.Reservation;

public final class ReservationCreationResult {

    private final Reservation reservation;
    private final ReservationCreationError error;
    private final String message;

    private ReservationCreationResult(
            Reservation reservation,
            ReservationCreationError error,
            String message) {
        this.reservation = reservation;
        this.error = error;
        this.message = message;
    }

    public static ReservationCreationResult success(Reservation reservation) {
        if (reservation == null) {
            throw new IllegalArgumentException("reservation must not be null");
        }
        return new ReservationCreationResult(reservation, null, "Reservation created");
    }

    public static ReservationCreationResult failure(
            ReservationCreationError error,
            String message) {
        if (error == null) {
            throw new IllegalArgumentException("error must not be null");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }
        return new ReservationCreationResult(null, error, message);
    }

    public boolean isSuccess() {
        return reservation != null;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public ReservationCreationError getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }
}