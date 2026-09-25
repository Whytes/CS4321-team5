package view;

import static org.junit.jupiter.api.Assertions.*;
import static view.FxTestSupport.onFx;

import java.time.LocalDate;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import model.AvailabilitySlot;
import model.Reservation;
import model.Space;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ReservationShellIntegrationTest {
    @BeforeAll
    static void startJavaFx() throws Exception {
        FxTestSupport.start();
    }

    // US-6 AT1 / issue #18: success also refreshes an already-opened personal list.
    @Test
    void creationNotifiesExistingPersonalList() throws Exception {
        onFx(() -> {
            try (Shell shell = new Shell()) {
                shell.navigate("My reservations");
                ListView<?> personalList = (ListView<?>) shell.stage.getScene()
                        .lookup("#my-reservations-list");
                assertTrue(personalList.getItems().isEmpty());
                shell.openForm("10:00", "11:00");
                shell.submit();
                shell.assertSuccess(1);
                assertEquals(1, personalList.getItems().size());
                Reservation created = (Reservation) personalList.getItems().get(0);
                assertEquals("study-room-a", created.getSpaceId());
                assertEquals(shell.day, created.getDate());
            }
        });
    }

    @Test
    void confirmedCancellationRefreshesPersonalListAndAvailability() throws Exception {
        onFx(() -> {
            try (Shell shell = new Shell()) {
                Reservation reservation = new Reservation(
                        "booking", "study-room-a", "local-user", shell.day,
                        java.time.LocalTime.of(10, 0), java.time.LocalTime.of(11, 0));
                shell.application.getApplicationController().getReservationStore().add(reservation);

                shell.navigate("My reservations");
                ListView<?> personalList = (ListView<?>) shell.stage.getScene()
                        .lookup("#my-reservations-list");
                personalList.getSelectionModel().selectFirst();
                shell.application.handleReservationCancellation(reservation);

                assertTrue(personalList.getItems().isEmpty());
                shell.navigate("Daily availability");
                AvailabilityView availability = (AvailabilityView) shell.stage.getScene()
                        .lookup(".availability-view");
                availability.selectedSpaceProperty().set(shell.application
                        .getApplicationController().getSpaceController()
                        .findById("study-room-a").orElseThrow());
                availability.getDatePicker().setValue(shell.day);
                assertEquals(1, shell.schedule().getItems().size());
                assertFalse(shell.schedule().getItems().get(0).reserved());
            }
        });
    }

    // US-6 AT2: create the original through the shell, then reject overlapping input.
    @Test
    void rejectsConflictingBookingThroughShell() throws Exception {
        onFx(() -> {
            try (Shell shell = new Shell()) {
                shell.openForm("10:00", "11:00");
                shell.submit();
                shell.assertSuccess(1);
                shell.openForm("10:30", "11:30");
                shell.submit();
                shell.assertFailure("The selected space is already reserved for that time", 1);
                shell.assertInputs("10:30", "11:30", shell.day);
            }
        });
    }

    // US-6 AT3: adjacent bookings are accepted and the newest interval is selected.
    @Test
    void acceptsAdjacentBookingThroughShell() throws Exception {
        onFx(() -> {
            try (Shell shell = new Shell()) {
                shell.openForm("10:00", "11:00");
                shell.submit();
                shell.assertSuccess(1);
                shell.openForm("11:00", "12:00");
                shell.submit();
                shell.assertSuccess(2);
                assertEquals(java.time.LocalTime.of(11, 0),
                        shell.schedule().getSelectionModel().getSelectedItem().startTime());
            }
        });
    }

    // US-6 AT4: reversed/equal time ranges retain the form and entered values.
    @ParameterizedTest
    @ValueSource(strings = {"09:00", "10:00"})
    void rejectsInvalidRangeThroughShell(String end) throws Exception {
        onFx(() -> {
            try (Shell shell = new Shell()) {
                shell.openForm("10:00", end);
                shell.submit();
                shell.assertFailure("End time must be after start time", 0);
                shell.assertInputs("10:00", end, shell.day);
            }
        });
    }

    // US-6 AT5: a past date is rejected and remains visible for correction.
    @Test
    void rejectsPastBookingThroughShell() throws Exception {
        onFx(() -> {
            try (Shell shell = new Shell()) {
                shell.openForm("10:00", "11:00");
                LocalDate past = LocalDate.now().minusDays(1);
                shell.date().setValue(past);
                shell.submit();
                shell.assertFailure("Reservation start must be in the future", 0);
                shell.assertInputs("10:00", "11:00", past);
            }
        });
    }

    // US-6 AT6: each missing required field is rejected through the full shell.
    @ParameterizedTest
    @ValueSource(strings = {"space", "date", "start", "end"})
    void rejectsMissingInputThroughShell(String field) throws Exception {
        onFx(() -> {
            try (Shell shell = new Shell()) {
                shell.openForm("10:00", "11:00");
                switch (field) {
                    case "space" -> shell.space().setValue(null);
                    case "date" -> shell.date().getEditor().clear();
                    case "start" -> shell.time("start").clear();
                    case "end" -> shell.time("end").clear();
                    default -> throw new AssertionError(field);
                }
                shell.submit();
                shell.assertFailure("Space, date, start time, and end time are required", 0);
                assertEquals(field.equals("start") ? "" : "10:00", shell.time("start").getText());
                assertEquals(field.equals("end") ? "" : "11:00", shell.time("end").getText());
                if (field.equals("date")) {
                    assertEquals("", shell.date().getEditor().getText());
                }
            }
        });
    }

    // Issue #18 navigation: clicking the active page must retain its selection.
    @ParameterizedTest
    @ValueSource(strings = {"New reservation", "Daily availability"})
    void repeatedNavigationKeepsCurrentPageSelected(String page) throws Exception {
        onFx(() -> {
            try (Shell shell = new Shell()) {
                shell.navigate(page);
                javafx.scene.Node original = shell.stage.getScene().lookup(
                        page.equals("New reservation") ? ".reservation-form" : ".availability-view");
                if (page.equals("New reservation")) {
                    shell.time("start").setText("10:30");
                }
                shell.navigate(page);
                assertTrue(shell.navigation(page).isSelected());
                assertSame(original, shell.stage.getScene().lookup(
                        page.equals("New reservation") ? ".reservation-form" : ".availability-view"));
                if (page.equals("New reservation")) {
                    assertEquals("10:30", shell.time("start").getText());
                }
            }
        });
    }

    // US-6 AT2/AT3: correcting rejected input succeeds without starting over.
    @Test
    void correctedConflictCanBeSubmitted() throws Exception {
        onFx(() -> {
            try (Shell shell = new Shell()) {
                shell.openForm("10:00", "11:00");
                shell.submit();
                shell.openForm("10:30", "11:30");
                shell.submit();
                shell.assertFailure("The selected space is already reserved for that time", 1);
                shell.time("start").setText("11:00");
                shell.time("end").setText("12:00");
                shell.submit();
                shell.assertSuccess(2);
            }
        });
    }

    // US-6 AT1: success switches a reused schedule from another room/day to the booking.
    @Test
    void creationReplacesPreviousScheduleSelection() throws Exception {
        onFx(() -> {
            try (Shell shell = new Shell()) {
                shell.navigate("Daily availability");
                AvailabilityView availability = (AvailabilityView) shell.stage.getScene()
                        .lookup(".availability-view");
                availability.selectedSpaceProperty().set(shell.application.getApplicationController()
                        .getSpaceController().findById("theater").orElseThrow());
                availability.getDatePicker().setValue(shell.day.plusDays(3));
                shell.openForm("10:00", "11:00");
                shell.submit();
                shell.assertSuccess(1);
                assertSame(availability, shell.stage.getScene().lookup(".availability-view"));
                shell.navigate("My reservations");
                shell.navigate("Daily availability");
                assertEquals(shell.day, availability.getDatePicker().getValue());
                assertEquals("study-room-a", availability.selectedSpaceProperty().get().getSpaceId());
                assertEquals(1, shell.schedule().getItems().stream()
                        .filter(AvailabilitySlot::reserved).count());
            }
        });
    }

    // US-6 date validation: February 30 must not be normalized into another date.
    @Test
    void rejectsImpossibleCalendarDate() throws Exception {
        onFx(() -> {
            try (Shell shell = new Shell()) {
                shell.openForm("10:00", "11:00");
                String invalidDate = shell.date().getConverter()
                        .toString(LocalDate.of(2031, 2, 28)).replace("28", "30");
                shell.date().getEditor().setText(invalidDate);
                shell.submit();
                shell.assertFailure("Enter a valid date or choose one from the calendar.", 0);
                assertEquals(invalidDate, shell.date().getEditor().getText());
            }
        });
    }

    // US-6 input preservation: leaving an invalid date must not restore the old date.
    @Test
    void invalidDateSurvivesFocusChangeAndCannotCreateBooking() throws Exception {
        java.util.concurrent.atomic.AtomicReference<Shell> reference =
                new java.util.concurrent.atomic.AtomicReference<>();
        java.util.concurrent.atomic.AtomicReference<Throwable> uncaught =
                new java.util.concurrent.atomic.AtomicReference<>();
        java.util.concurrent.atomic.AtomicReference<Thread.UncaughtExceptionHandler> previousHandler =
                new java.util.concurrent.atomic.AtomicReference<>();
        try {
            onFx(() -> {
                previousHandler.set(Thread.currentThread().getUncaughtExceptionHandler());
                Thread.currentThread().setUncaughtExceptionHandler((thread, error) -> uncaught.set(error));
                Shell shell = new Shell();
                reference.set(shell);
                shell.openForm("10:00", "11:00");
                shell.date().getEditor().requestFocus();
            });
            onFx(() -> {
                Shell shell = reference.get();
                assertSame(shell.date(), shell.stage.getScene().getFocusOwner());
                shell.date().getEditor().setText("not a date");
                shell.time("start").requestFocus();
            });
            onFx(() -> {
                Shell shell = reference.get();
                assertSame(shell.time("start"), shell.stage.getScene().getFocusOwner());
                assertEquals("not a date", shell.date().getEditor().getText());
                shell.submit();
                shell.assertFailure("Enter a valid date or choose one from the calendar.", 0);
                assertNull(uncaught.get(), "Invalid date must not throw on the JavaFX thread");
            });
        } finally {
            onFx(() -> {
                if (reference.get() != null) {
                    reference.get().close();
                }
                Thread.currentThread().setUncaughtExceptionHandler(previousHandler.get());
            });
        }
    }

    // US-6 input validation: a failed automatic date commit remains correctable.
    @ParameterizedTest
    @ValueSource(strings = {"typed", "calendar", "cleared"})
    void recoversAfterInvalidDateCommit(String correction) throws Exception {
        onFx(() -> {
            try (Shell shell = new Shell()) {
                shell.openForm("10:00", "11:00");
                shell.date().getEditor().setText("not a date");
                assertDoesNotThrow(() -> shell.date().commitValue());
                assertEquals("not a date", shell.date().getEditor().getText());
                if (correction.equals("calendar")) {
                    shell.date().setValue(shell.day.plusDays(1));
                    shell.date().setValue(shell.day);
                } else if (correction.equals("cleared")) {
                    shell.date().getEditor().clear();
                } else {
                    shell.date().getEditor().setText(java.time.format.DateTimeFormatter
                            .ofLocalizedDate(java.time.format.FormatStyle.SHORT).format(shell.day));
                }
                assertDoesNotThrow(() -> shell.date().commitValue());
                shell.submit();
                if (correction.equals("cleared")) {
                    shell.assertFailure("Space, date, start time, and end time are required", 0);
                } else {
                    shell.assertSuccess(1);
                }
            }
        });
    }

    // Issue #18: Escape cancels from an input field without saving.
    @Test
    void escapeCancelsFromTimeField() throws Exception {
        onFx(() -> {
            try (Shell shell = new Shell()) {
                shell.openForm("10:00", "11:00");
                javafx.event.Event.fireEvent(shell.time("start"), new javafx.scene.input.KeyEvent(
                        javafx.scene.input.KeyEvent.KEY_PRESSED, "", "",
                        javafx.scene.input.KeyCode.ESCAPE, false, false, false, false));
                assertTrue(shell.navigation("Browse spaces").isSelected());
                assertNotNull(shell.stage.getScene().lookup(".space-list"));
                assertTrue(shell.application.getApplicationController().getReservationStore()
                        .getReservations().isEmpty());
            }
        });
    }

    private static final class Shell implements AutoCloseable {
        final Main application = new Main();
        final Stage stage = new Stage();
        final LocalDate day = LocalDate.now().plusDays(2);

        Shell() {
            application.start(stage);
        }

        ToggleButton navigation(String text) {
            return stage.getScene().getRoot().lookupAll(".nav-button").stream()
                    .map(node -> (ToggleButton) node).filter(button -> text.equals(button.getText()))
                    .findFirst().orElseThrow();
        }

        void navigate(String text) {
            navigation(text).fire();
            stage.getScene().getRoot().applyCss();
            stage.getScene().getRoot().layout();
        }

        void openForm(String start, String end) {
            navigate("New reservation");
            space().setValue(application.getApplicationController().getSpaceController()
                    .findById("study-room-a").orElseThrow());
            date().setValue(day);
            time("start").setText(start);
            time("end").setText(end);
        }

        void submit() {
            ((Button) stage.getScene().lookup("#reservation-submit")).fire();
            stage.getScene().getRoot().applyCss();
            stage.getScene().getRoot().layout();
        }

        void assertSuccess(int count) {
            assertEquals(count, application.getApplicationController().getReservationStore()
                    .getReservations().size());
            assertEquals("Reservation created",
                    ((Label) stage.getScene().lookup("#availability-message")).getText());
            assertTrue(navigation("Daily availability").isSelected());
            assertNull(stage.getScene().lookup(".reservation-form"));
            AvailabilityView view = (AvailabilityView) stage.getScene().lookup(".availability-view");
            assertEquals("Study Room A", view.selectedSpaceProperty().get().getName());
            assertEquals(day, view.getDatePicker().getValue());
            assertEquals(count, schedule().getItems().stream().filter(AvailabilitySlot::reserved).count());
            assertTrue(schedule().getSelectionModel().getSelectedItem().reserved());
            assertFalse(stage.getScene().getRoot().lookupAll(".reserved-slot").isEmpty());
        }

        void assertFailure(String message, int count) {
            assertEquals(count, application.getApplicationController().getReservationStore()
                    .getReservations().size());
            assertNotNull(stage.getScene().lookup(".reservation-form"));
            assertTrue(navigation("New reservation").isSelected());
            assertEquals(message, ((Label) stage.getScene().lookup("#reservation-feedback")).getText());
        }

        void assertInputs(String start, String end, LocalDate date) {
            assertEquals(start, time("start").getText());
            assertEquals(end, time("end").getText());
            assertEquals(date, date().getValue());
            assertEquals("study-room-a", space().getValue().getSpaceId());
        }

        @SuppressWarnings("unchecked")
        ComboBox<Space> space() {
            return (ComboBox<Space>) stage.getScene().lookup("#reservation-space");
        }

        DatePicker date() {
            return (DatePicker) stage.getScene().lookup("#reservation-date");
        }

        TextField time(String field) {
            return (TextField) stage.getScene().lookup("#reservation-" + field);
        }

        @SuppressWarnings("unchecked")
        ListView<AvailabilitySlot> schedule() {
            return (ListView<AvailabilitySlot>) stage.getScene().lookup(".availability-list");
        }

        @Override
        public void close() {
            stage.close();
        }
    }
}
