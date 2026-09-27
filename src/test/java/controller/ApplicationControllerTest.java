package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import model.Reservation;
import model.ReservationStore;
import persistence.InitialSpaceCatalog;

class ApplicationControllerTest {

    @Test
    void applicationCompositionSharesOneReservationStore() {
        ApplicationController application = new ApplicationController(
                InitialSpaceCatalog.getDefaultSpaces());

        assertSame(application.getReservationStore(),
                application.getReservationController().getReservationStore());
        assertEquals(5, application.getSpaceController().getSpaces().size());
    }

    @Test
    void reservationQueryRefreshesFromSharedStoreInDateAndTimeOrder() {
        ApplicationController application = new ApplicationController(
                InitialSpaceCatalog.getDefaultSpaces());
        ReservationStore store = application.getReservationStore();

        store.add(new Reservation("later", "study-room-a", "local-user",
                LocalDate.of(2026, 10, 2), LocalTime.of(14, 0), LocalTime.of(15, 0)));
        store.add(new Reservation("earlier", "study-room-a", "local-user",
                LocalDate.of(2026, 10, 1), LocalTime.of(9, 0), LocalTime.of(10, 0)));
        store.add(new Reservation("other-user", "study-room-a", "other-user",
                LocalDate.of(2026, 10, 1), LocalTime.of(8, 0), LocalTime.of(9, 0)));

        assertEquals(java.util.List.of("earlier", "later"),
                application.getReservationController().getMyReservations().stream()
                        .map(Reservation::getReservationId)
                        .toList());
    }
    @Test
void applicationUsesPreloadedReservationStore() {
    ReservationStore loadedStore = new ReservationStore();
    loadedStore.add(new Reservation(
            "saved-reservation", "study-room-a", "local-user",
            LocalDate.of(2026, 10, 1),
            LocalTime.of(10, 0), LocalTime.of(11, 0)));

    ApplicationController application = new ApplicationController(
            InitialSpaceCatalog.getDefaultSpaces(), loadedStore);

    assertSame(loadedStore, application.getReservationStore());
    assertSame(loadedStore, application.getReservationController().getReservationStore());
    assertEquals(1, application.getReservationController().getMyReservations().size());
}
}