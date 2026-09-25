package view;

import static org.junit.jupiter.api.Assertions.*;
import static view.FxTestSupport.onFx;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import controller.ReservationController;
import javafx.scene.Scene;
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
import model.ReservationStore;
import model.Space;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import persistence.InitialSpaceCatalog;

/** Exercises actual JavaFX controls through the controller and shared store. */
class ReservationFormIntegrationTest {

    private static final LocalDate DATE = LocalDate.of(2026, 10, 1);

    @BeforeAll
    static void startJavaFx() throws Exception {
        FxTestSupport.start();
    }

    // US-6 AT1: valid submission saves, displays success, and refreshes the schedule.
    @Test
    void createsReservationAndRefreshesSchedule() throws Exception {
        onFx(() -> {
            Fixture fixture = new Fixture();
            fixture.submit();
            assertEquals(1, fixture.store.getReservations().size());
            Reservation created = fixture.notified.get();
            assertNotNull(created);
            assertEquals("local-user", created.getOwnerId());
            assertEquals("Reservation created", fixture.feedback());
            assertTrue(fixture.form.lookup("#reservation-feedback")
                    .getStyleClass().contains("success-feedback"));
            assertEquals(1, fixture.schedule().getItems().stream()
                    .filter(AvailabilitySlot::reserved).count());
            assertEquals(created.getReservationId(), fixture.schedule().getItems().stream()
                    .filter(AvailabilitySlot::reserved).findFirst().orElseThrow().reservationId());
        });
    }

    // US-6 AT2: an overlapping submission is rejected and all input is preserved.
    @Test
    void rejectsConflictWithoutClearingInputs() throws Exception {
        onFx(() -> {
            Fixture fixture = new Fixture();
            fixture.seedBooking();
            fixture.start().setText("10:30");
            fixture.end().setText("11:30");
            fixture.submit();
            fixture.assertFailure("The selected space is already reserved for that time", 1);
            assertEquals("10:30", fixture.start().getText());
            assertEquals("11:30", fixture.end().getText());
            assertEquals(DATE, fixture.date().getValue());
            assertEquals("study-room-a", fixture.space().getValue().getSpaceId());
        });
    }

    // US-6 AT3: a reservation starting exactly at the previous end is accepted.
    @Test
    void acceptsAdjacentBooking() throws Exception {
        onFx(() -> {
            Fixture fixture = new Fixture();
            fixture.seedBooking();
            fixture.start().setText("11:00");
            fixture.end().setText("12:00");
            fixture.submit();
            assertEquals(2, fixture.store.getReservations().size());
            assertNotNull(fixture.notified.get());
        });
    }

    // US-6 AT4: reversed and equal times fail without changing shared state.
    @ParameterizedTest
    @ValueSource(strings = {"09:00", "10:00"})
    void rejectsNonPositiveDuration(String end) throws Exception {
        onFx(() -> {
            Fixture fixture = new Fixture();
            fixture.end().setText(end);
            fixture.submit();
            fixture.assertFailure("End time must be after start time", 0);
            assertEquals(end, fixture.end().getText());
        });
    }

    // US-6 AT5: past starts are rejected with the controller's explanatory message.
    @Test
    void rejectsPastStart() throws Exception {
        onFx(() -> {
            Fixture fixture = new Fixture();
            fixture.date().setValue(DATE.minusDays(2));
            fixture.submit();
            fixture.assertFailure("Reservation start must be in the future", 0);
            assertEquals(DATE.minusDays(2), fixture.date().getValue());
        });
    }

    // US-6 AT6: each required input can be missing without saving or clearing others.
    @ParameterizedTest
    @ValueSource(strings = {"space", "date", "start", "end"})
    void rejectsMissingRequiredInput(String field) throws Exception {
        onFx(() -> {
            Fixture fixture = new Fixture();
            switch (field) {
                case "space" -> fixture.space().setValue(null);
                case "date" -> fixture.date().setValue(null);
                case "start" -> fixture.start().clear();
                case "end" -> fixture.end().clear();
                default -> throw new AssertionError(field);
            }
            fixture.submit();
            fixture.assertFailure("Space, date, start time, and end time are required", 0);
            if (!field.equals("start")) {
                assertEquals("10:00", fixture.start().getText());
            }
            if (!field.equals("end")) {
                assertEquals("11:00", fixture.end().getText());
            }
        });
    }

    // US-6 input validation: invalid clock text must not normalize into a valid booking.
    @ParameterizedTest
    @ValueSource(strings = {"24:00", "10:60", "abc"})
    void rejectsMalformedTime(String time) throws Exception {
        onFx(() -> {
            Fixture fixture = new Fixture();
            fixture.start().setText(time);
            fixture.submit();
            fixture.assertFailure("Enter start and end times using HH:mm, such as 09:00.", 0);
            assertEquals(time, fixture.start().getText());
        });
    }

    // US-6 AT1: text typed into the date editor must be used even before focus changes.
    @Test
    void submitsTypedDateInsteadOfPreviousSelection() throws Exception {
        onFx(() -> {
            Fixture fixture = new Fixture();
            LocalDate typedDate = DATE.plusDays(1);
            fixture.date().getEditor().setText(fixture.date().getConverter().toString(typedDate));
            fixture.submit();
            assertNotNull(fixture.notified.get());
            assertEquals(typedDate, fixture.notified.get().getDate());
        });
    }

    // US-6 AT6: clearing the date editor must not silently reuse its previous value.
    @Test
    void rejectsClearedDateEditor() throws Exception {
        onFx(() -> {
            Fixture fixture = new Fixture();
            fixture.date().getEditor().clear();
            fixture.submit();
            fixture.assertFailure("Space, date, start time, and end time are required", 0);
            assertEquals("", fixture.date().getEditor().getText());
        });
    }

    // US-6 input validation: an invalid date must not save using a stale selection.
    @Test
    void rejectsMalformedDateAndPreservesInput() throws Exception {
        onFx(() -> {
            Fixture fixture = new Fixture();
            fixture.date().getEditor().setText("not a date");
            fixture.submit();
            fixture.assertFailure("Enter a valid date or choose one from the calendar.", 0);
            assertEquals("not a date", fixture.date().getEditor().getText());
            assertEquals("10:00", fixture.start().getText());
            assertEquals("11:00", fixture.end().getText());
        });
    }

    // Issue #18 cancel deliverable: abandoning input does not create a reservation.
    @Test
    void cancelNotifiesShellWithoutSubmitting() throws Exception {
        onFx(() -> {
            Fixture fixture = new Fixture();
            Button cancel = (Button) fixture.form.lookup("#reservation-cancel");
            assertTrue(cancel.isCancelButton());
            cancel.fire();
            assertEquals(1, fixture.cancelled.get());
            assertTrue(fixture.store.getReservations().isEmpty());
            assertNull(fixture.notified.get());
        });
    }

    // Issue #18 shell integration: Cancel returns to Browse with matching navigation.
    @Test
    void cancelReturnsToBrowseSpaces() throws Exception {
        onFx(() -> {
            Main application = new Main();
            Stage stage = new Stage();
            try {
                application.start(stage);
                ToggleButton newReservation = navigation(stage, "New reservation");
                newReservation.fire();
                ((TextField) stage.getScene().lookup("#reservation-start")).setText("10:00");
                ((Button) stage.getScene().lookup("#reservation-cancel")).fire();
                assertNotNull(stage.getScene().lookup(".space-list"));
                assertNull(stage.getScene().lookup(".reservation-form"));
                assertTrue(navigation(stage, "Browse spaces").isSelected());
                assertFalse(newReservation.isSelected());
                assertTrue(application.getApplicationController().getReservationStore()
                        .getReservations().isEmpty());
            } finally {
                stage.close();
            }
        });
    }

    // US-6 AT1 / issue #18: real application wiring refreshes an existing schedule.
    @Test
    @SuppressWarnings("unchecked")
    void shellRefreshesAvailabilityAfterCreation() throws Exception {
        onFx(() -> {
            Main application = new Main();
            Stage stage = new Stage();
            try {
                application.start(stage);
                navigation(stage, "Daily availability").fire();
                AvailabilityView availability = (AvailabilityView) stage.getScene()
                        .lookup(".availability-view");
                Space space = application.getApplicationController().getSpaceController()
                        .getSpaces().get(0);
                LocalDate tomorrow = LocalDate.now().plusDays(1);
                availability.selectedSpaceProperty().set(space);
                availability.getDatePicker().setValue(tomorrow);
                ListView<AvailabilitySlot> schedule = (ListView<AvailabilitySlot>)
                        availability.lookup(".availability-list");
                assertEquals(0, schedule.getItems().stream().filter(AvailabilitySlot::reserved).count());

                navigation(stage, "New reservation").fire();
                stage.getScene().getRoot().applyCss();
                ((ComboBox<Space>) stage.getScene().lookup("#reservation-space")).setValue(space);
                ((DatePicker) stage.getScene().lookup("#reservation-date")).setValue(tomorrow);
                ((TextField) stage.getScene().lookup("#reservation-start")).setText("10:00");
                ((TextField) stage.getScene().lookup("#reservation-end")).setText("11:00");
                ((Button) stage.getScene().lookup("#reservation-submit")).fire();

                assertEquals("Reservation created",
                        ((Label) stage.getScene().lookup("#availability-message")).getText());
                assertSame(availability, stage.getScene().lookup(".availability-view"));
                assertTrue(navigation(stage, "Daily availability").isSelected());
                assertEquals(1, schedule.getItems().stream().filter(AvailabilitySlot::reserved).count());
                assertEquals(1, application.getApplicationController().getReservationStore()
                        .getReservations().size());
            } finally {
                stage.close();
            }
        });
    }

    // US-6 AT1: creation is immediately displayed without previously opening other pages.
    @Test
    @SuppressWarnings("unchecked")
    void shellShowsCreatedReservationWhenNewReservationIsOpenedFirst() throws Exception {
        onFx(() -> {
            Main application = new Main();
            Stage stage = new Stage();
            try {
                application.start(stage);
                navigation(stage, "New reservation").fire();
                Space space = application.getApplicationController().getSpaceController()
                        .getSpaces().get(0);
                LocalDate tomorrow = LocalDate.now().plusDays(1);
                ((ComboBox<Space>) stage.getScene().lookup("#reservation-space"))
                        .setValue(space);
                ((DatePicker) stage.getScene().lookup("#reservation-date"))
                        .setValue(tomorrow);
                ((TextField) stage.getScene().lookup("#reservation-start"))
                        .setText("10:00");
                ((TextField) stage.getScene().lookup("#reservation-end"))
                        .setText("11:00");

                ((Button) stage.getScene().lookup("#reservation-submit")).fire();

                assertNotNull(stage.getScene().lookup(".availability-view"));
                ListView<AvailabilitySlot> schedule = (ListView<AvailabilitySlot>)
                        stage.getScene().lookup(".availability-list");
                assertEquals(1, schedule.getItems().stream()
                        .filter(AvailabilitySlot::reserved).count());
                assertTrue(navigation(stage, "Daily availability").isSelected());
                assertFalse(navigation(stage, "New reservation").isSelected());
                AvailabilityView availability = (AvailabilityView) stage.getScene()
                        .lookup(".availability-view");
                assertEquals(space, availability.selectedSpaceProperty().get());
                assertEquals(tomorrow, availability.getDatePicker().getValue());
                assertTrue(schedule.getSelectionModel().getSelectedItem().reserved());
                assertEquals("Reservation created",
                        ((Label) stage.getScene().lookup("#availability-message")).getText());
            } finally {
                stage.close();
            }
        });
    }

    // US-6 AT1/AT6: a second submit after success must not create a duplicate booking.
    @Test
    void repeatedSubmitDoesNotCreateAnotherReservation() throws Exception {
        onFx(() -> {
            Fixture fixture = new Fixture();
            fixture.submit();
            assertNotNull(fixture.notified.get());
            fixture.notified.set(null);
            fixture.submit();
            fixture.assertFailure("Space, date, start time, and end time are required", 1);
            assertFalse(fixture.form.lookup("#reservation-feedback")
                    .getStyleClass().contains("success-feedback"));
        });
    }

    private static ToggleButton navigation(Stage stage, String text) {
        return stage.getScene().getRoot().lookupAll(".nav-button").stream()
                .map(node -> (ToggleButton) node)
                .filter(button -> button.getText().equals(text)).findFirst().orElseThrow();
    }

    private static final class Fixture {
        final ReservationStore store = new ReservationStore();
        final AtomicReference<Reservation> notified = new AtomicReference<>();
        final AtomicInteger cancelled = new AtomicInteger();
        final AvailabilityView availability;
        final ReservationFormView form;

        Fixture() {
            List<Space> spaces = InitialSpaceCatalog.getDefaultSpaces();
            ReservationController controller = new ReservationController(store, spaces,
                    Clock.fixed(Instant.parse("2026-09-30T12:00:00Z"), ZoneOffset.UTC));
            availability = new AvailabilityView(spaces, controller);
            availability.selectedSpaceProperty().set(spaces.get(0));
            availability.getDatePicker().setValue(DATE);
            form = new ReservationFormView(spaces, controller, reservation -> {
                notified.set(reservation);
                availability.refreshAfterReservationChange();
            }, cancelled::incrementAndGet);
            new Scene(form);
            form.applyCss();
            space().setValue(spaces.get(0));
            date().setValue(DATE);
            start().setText("10:00");
            end().setText("11:00");
        }

        void seedBooking() {
            store.add(new Reservation("existing", "study-room-a", "local-user", DATE,
                    LocalTime.of(10, 0), LocalTime.of(11, 0)));
        }

        void submit() {
            ((Button) form.lookup("#reservation-submit")).fire();
        }

        void assertFailure(String message, int count) {
            assertEquals(message, feedback());
            assertEquals(count, store.getReservations().size());
            assertNull(notified.get());
            assertTrue(form.lookup("#reservation-feedback")
                    .getStyleClass().contains("validation-feedback"));
        }

        String feedback() {
            return ((Label) form.lookup("#reservation-feedback")).getText();
        }

        @SuppressWarnings("unchecked")
        ComboBox<Space> space() {
            return (ComboBox<Space>) form.lookup("#reservation-space");
        }

        DatePicker date() {
            return (DatePicker) form.lookup("#reservation-date");
        }

        TextField start() {
            return (TextField) form.lookup("#reservation-start");
        }

        TextField end() {
            return (TextField) form.lookup("#reservation-end");
        }

        @SuppressWarnings("unchecked")
        ListView<AvailabilitySlot> schedule() {
            return (ListView<AvailabilitySlot>) availability.lookup(".availability-list");
        }
    }
}
