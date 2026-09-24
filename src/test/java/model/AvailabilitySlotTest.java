package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class AvailabilitySlotTest {

    @Test
    void rejectsNullIntervalEndpoints() {
        assertThrows(IllegalArgumentException.class,
                () -> new AvailabilitySlot(null, LocalTime.NOON, false, null));
        assertThrows(IllegalArgumentException.class,
                () -> new AvailabilitySlot(LocalTime.NOON, null, false, null));
    }

    @Test
    void rejectsNonPositiveIntervals() {
        assertThrows(IllegalArgumentException.class,
                () -> new AvailabilitySlot(LocalTime.NOON, LocalTime.NOON, false, null));
        assertThrows(IllegalArgumentException.class,
                () -> new AvailabilitySlot(LocalTime.of(13, 0), LocalTime.NOON, false, null));
    }

    @Test
    void rejectsReservedSlotsWithoutAnId() {
        assertThrows(IllegalArgumentException.class,
                () -> new AvailabilitySlot(LocalTime.NOON, LocalTime.of(13, 0), true, null));
        assertThrows(IllegalArgumentException.class,
                () -> new AvailabilitySlot(LocalTime.NOON, LocalTime.of(13, 0), true, ""));
        assertThrows(IllegalArgumentException.class,
                () -> new AvailabilitySlot(LocalTime.NOON, LocalTime.of(13, 0), true, " "));
    }

    @Test
    void rejectsAvailableSlotsWithAnId() {
        assertThrows(IllegalArgumentException.class,
                () -> new AvailabilitySlot(LocalTime.NOON, LocalTime.of(13, 0), false, "reservation-1"));
    }

    @Test
    void createsValidAvailableSlot() {
        AvailabilitySlot slot = AvailabilitySlot.available(LocalTime.NOON, LocalTime.of(13, 0));

        assertEquals(LocalTime.NOON, slot.startTime());
        assertEquals(LocalTime.of(13, 0), slot.endTime());
        assertEquals("Available", slot.getStatusLabel());
        assertEquals(false, slot.reserved());
        assertEquals(null, slot.reservationId());
    }

    @Test
    void createsValidReservedSlotFromReservation() {
        Reservation reservation = new Reservation(
                "reservation-1",
                "space-1",
                "owner-1",
                LocalDate.of(2026, 10, 1),
                LocalTime.NOON,
                LocalTime.of(13, 0));

        AvailabilitySlot slot = AvailabilitySlot.reserved(reservation);

        assertEquals(reservation.getStartTime(), slot.startTime());
        assertEquals(reservation.getEndTime(), slot.endTime());
        assertEquals("Reserved", slot.getStatusLabel());
        assertEquals(true, slot.reserved());
        assertEquals("reservation-1", slot.reservationId());
    }
}
