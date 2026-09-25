package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import model.Reservation;
import model.ReservationStore;
import model.AvailabilitySlot;
import persistence.InitialSpaceCatalog;
import service.ReservationCreationResult;

class ReservationControllerTest {

    @Test
    void dailyAvailabilityReturnsOnlySelectedSpaceAndDateInStartTimeOrder() {
        ReservationStore store = new ReservationStore();
        ReservationController controller = new ReservationController(store);
        LocalDate date = LocalDate.of(2026, 10, 1);

        store.add(new Reservation("later", "study-room-a", "local-user",
                date, LocalTime.of(11, 0), LocalTime.of(12, 0)));
        store.add(new Reservation("earlier", "study-room-a", "local-user",
                date, LocalTime.of(9, 0), LocalTime.of(10, 0)));
        store.add(new Reservation("other-space", "study-room-b", "local-user",
                date, LocalTime.of(8, 0), LocalTime.of(9, 0)));
        store.add(new Reservation("other-date", "study-room-a", "local-user",
                date.plusDays(1), LocalTime.of(8, 0), LocalTime.of(9, 0)));

        var results = controller.getReservationsForSpace("study-room-a", date);

        assertEquals(2, results.size());
        assertEquals("earlier", results.get(0).getReservationId());
        assertEquals("later", results.get(1).getReservationId());
    }

    @Test
    void dailyAvailabilityReturnsEmptyListWhenNoReservationsMatch() {
        ReservationController controller =
                new ReservationController(new ReservationStore());

        var results = controller.getReservationsForSpace(
                "study-room-a", LocalDate.of(2026, 10, 1));

        assertTrue(results.isEmpty());
    }

    @Test
    void dailyAvailabilityRejectsBlankSpaceId() {
        ReservationController controller =
                new ReservationController(new ReservationStore());

        assertThrows(IllegalArgumentException.class,
                () -> controller.getReservationsForSpace(
                        " ", LocalDate.of(2026, 10, 1)));
    }

    @Test
    void dailyAvailabilityRejectsNullDate() {
        ReservationController controller =
                new ReservationController(new ReservationStore());

        assertThrows(IllegalArgumentException.class,
                () -> controller.getReservationsForSpace(
                        "study-room-a", null));
    }

    @Test
    void dailyScheduleIncludesLeadingBetweenAndTrailingAvailability() {
        ReservationStore store = new ReservationStore();
        LocalDate date = LocalDate.of(2026, 10, 1);
        store.add(new Reservation("first", "room-a", "owner", date,
                LocalTime.of(9, 0), LocalTime.of(10, 0)));
        store.add(new Reservation("second", "room-a", "owner", date,
                LocalTime.of(13, 0), LocalTime.of(14, 0)));

        var schedule = new ReservationController(store).getDailySchedule("room-a", date);

        assertEquals(5, schedule.size());
        assertEquals(LocalTime.MIN, schedule.get(0).startTime());
        assertEquals(LocalTime.of(9, 0), schedule.get(0).endTime());
        assertEquals("first", schedule.get(1).reservationId());
        assertEquals(LocalTime.of(10, 0), schedule.get(2).startTime());
        assertEquals("second", schedule.get(3).reservationId());
        assertEquals(LocalTime.MAX, schedule.get(4).endTime());
    }

    @Test
    void dayWithNoReservationsIsFullyAvailable() {
        var schedule = new ReservationController(new ReservationStore())
                .getDailySchedule("room-a", LocalDate.of(2026, 10, 1));

        assertEquals(List.of(AvailabilitySlot.available(LocalTime.MIN, LocalTime.MAX)),
                schedule);
    }

    @Test
    void dailyScheduleDoesNotCreateAvailabilityInsideOverlappingSnapshotReservations() {
        ReservationStore store = new ReservationStore();
        LocalDate date = LocalDate.of(2026, 10, 1);
        Reservation first = new Reservation("first", "room-a", "owner", date,
                LocalTime.of(9, 0), LocalTime.of(12, 0));
        Reservation overlapping = new Reservation("overlapping", "room-a", "owner", date,
                LocalTime.of(10, 0), LocalTime.of(14, 0));
        store.loadSnapshot(List.of(first, overlapping));

        var schedule = new ReservationController(store).getDailySchedule("room-a", date);

        assertEquals(4, schedule.size());
        assertEquals(LocalTime.MIN, schedule.get(0).startTime());
        assertEquals(LocalTime.of(9, 0), schedule.get(0).endTime());
        assertEquals("first", schedule.get(1).reservationId());
        assertEquals("overlapping", schedule.get(2).reservationId());
        assertEquals(LocalTime.of(14, 0), schedule.get(2).endTime());
        assertEquals(LocalTime.of(14, 0), schedule.get(3).startTime());
        assertEquals(LocalTime.MAX, schedule.get(3).endTime());
    }

    @Test
    void cancelReservation_removesReservationFromSharedStore() {
        ReservationStore store = new ReservationStore();
        ReservationController controller = new ReservationController(store);

        Reservation reservation = new Reservation(
                "res-1",
                "space-1",
                "local-user",
                LocalDate.of(2026, 9, 23),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0));

        store.add(reservation);

        Reservation cancelled = controller.cancelReservation("res-1");

        assertEquals(reservation, cancelled);
        assertNull(store.getReservation("res-1"));
        assertEquals(0, controller.getMyReservations().size());
    }
    @Test
    void cancelReservation_rejectsReservationOwnedByAnotherUser() {
        ReservationStore store = new ReservationStore();
        ReservationController controller = new ReservationController(store);

        Reservation reservation = new Reservation(
                "res-1",
                "space-1",
                "other-user",
                LocalDate.of(2026, 9, 23),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0));

        store.add(reservation);

        assertThrows(
                IllegalArgumentException.class,
                () -> controller.cancelReservation("res-1"));

        assertEquals(reservation, store.getReservation("res-1"));
    }

        @Test
        void createReservationCommandAddsReservationToSharedStore() {
                ReservationStore store = new ReservationStore();
                ReservationController controller = new ReservationController(
                                store,
                                InitialSpaceCatalog.getDefaultSpaces(),
                                Clock.fixed(Instant.parse("2026-09-30T12:00:00Z"), ZoneOffset.UTC));

                ReservationCreationResult result = controller.createReservation(
                                "study-room-a", LocalDate.of(2026, 10, 1),
                                LocalTime.of(9, 0), LocalTime.of(10, 0));

                assertTrue(result.isSuccess());
                assertEquals(result.getReservation(),
                                store.getReservation(result.getReservation().getReservationId()));
                assertEquals(1, store.getReservations().size());
        }

        @Test
        void legacyConstructorCanCreateFromDefaultCatalog() {
                ReservationStore store = new ReservationStore();
                ReservationController controller = new ReservationController(store);

                ReservationCreationResult result = controller.createReservation(
                                "study-room-a", LocalDate.now().plusDays(1),
                                LocalTime.NOON, LocalTime.of(13, 0));

                assertTrue(result.isSuccess());
                assertEquals(1, store.getReservations().size());
        }

        @Test
        void getMyReservationsReturnsOnlyLocalUserReservationsInDateAndTimeOrder() {
                ReservationStore store = new ReservationStore();
                ReservationController controller = new ReservationController(store);

                LocalDate firstDate = LocalDate.of(2026, 10, 1);
                LocalDate secondDate = LocalDate.of(2026, 10, 2);

                store.add(new Reservation("later-date", "study-room-a", "local-user",
                        secondDate, LocalTime.of(9, 0), LocalTime.of(10, 0)));
                store.add(new Reservation("later-time", "study-room-b", "local-user",
                        firstDate, LocalTime.of(13, 0), LocalTime.of(14, 0)));
                store.add(new Reservation("earlier-time", "study-room-a", "local-user",
                        firstDate, LocalTime.of(9, 0), LocalTime.of(10, 0)));
                store.add(new Reservation("other-user", "study-room-c", "other-user",
                        firstDate, LocalTime.of(8, 0), LocalTime.of(9, 0)));

                List<Reservation> results = controller.getMyReservations();

                assertEquals(3, results.size());
                assertEquals("earlier-time", results.get(0).getReservationId());
                assertEquals("later-time", results.get(1).getReservationId());
                assertEquals("later-date", results.get(2).getReservationId());
        }
}
