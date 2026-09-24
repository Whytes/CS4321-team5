package service;

public enum ReservationCreationError {
    MISSING_REQUIRED_FIELD,
    UNKNOWN_SPACE,
    INVALID_TIME_RANGE,
    TIME_IN_PAST,
    RESERVATION_CONFLICT
}