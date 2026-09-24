package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import model.Reservation;
import model.ReservationStore;
import model.AvailabilitySlot;

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
}
