package view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class AvailabilityViewTest {

    @Test
    void formatsDayBoundariesClearly() {
        assertEquals("Start of day", AvailabilityView.formatTime(LocalTime.MIN));
        assertEquals("End of day", AvailabilityView.formatTime(LocalTime.MAX));
    }

    @Test
    void preservesSecondsAndFractionalSecondsWhenPresent() {
        assertEquals("9:00:01 AM", AvailabilityView.formatTime(LocalTime.of(9, 0, 1)));
        assertEquals("9:00:01.123456789 AM",
                AvailabilityView.formatTime(LocalTime.of(9, 0, 1, 123456789)));
    }
}
