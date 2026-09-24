package controller;

import java.time.Clock;
import java.util.List;

import model.ReservationStore;
import model.Space;

/**
 * Application composition root for shared state and feature controllers.
 */
public final class ApplicationController {

    private final ReservationStore reservationStore;
    private final SpaceController spaceController;
    private final ReservationController reservationController;

    public ApplicationController(List<Space> spaces) {
        reservationStore = new ReservationStore();
        spaceController = new SpaceController(spaces);
        reservationController = new ReservationController(
            reservationStore, spaces, Clock.systemDefaultZone());
    }

    public ReservationStore getReservationStore() {
        return reservationStore;
    }

    public SpaceController getSpaceController() {
        return spaceController;
    }

    public ReservationController getReservationController() {
        return reservationController;
    }
}