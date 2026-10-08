package view;

import static org.junit.jupiter.api.Assertions.*;
import static view.FxTestSupport.onFx;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicReference;
import java.util.List;
import controller.ReservationController;
import controller.SpaceController;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Window;
import model.Reservation;
import model.ReservationStore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import persistence.InitialSpaceCatalog;

class MyReservationsViewTest {
    @BeforeAll
    static void startJavaFx() throws Exception {
        FxTestSupport.start();
    }

    // Existing list rendering: friendly catalog name, date, and interval are visible.
    @Test
    void rendersFriendlyNameAndReservationDetails() throws Exception {
        onFx(() -> {
            Fixture fixture = new Fixture();
            Reservation booking = reservation("booking", "study-room-a", "local-user", 10);
            fixture.store.add(booking);
            fixture.refresh();
            String expected = "Study Room A  |  "
                    + DateTimeFormatter.ofPattern("MMM d, yyyy").format(booking.getDate())
                    + "  |  " + DateTimeFormatter.ofPattern("h:mm a").format(booking.getStartTime())
                    + " - " + DateTimeFormatter.ofPattern("h:mm a").format(booking.getEndTime());
            assertTrue(fixture.visibleTexts().contains(expected));
        });
    }

    // Existing list refresh: read sorted local-user state and render its empty state.
    @Test
    void refreshesSortedLocalReservationsAndEmptyState() throws Exception {
        onFx(() -> {
            Fixture fixture = new Fixture();
            assertEquals("You have no reservations.", ((Label) fixture.list.getPlaceholder()).getText());
            assertTrue(fixture.list.getPlaceholder().isVisible());
            Reservation later = reservation("later", "study-room-a", "local-user", 14);
            Reservation earlier = reservation("earlier", "study-room-b", "local-user", 9);
            fixture.store.add(later);
            fixture.store.add(earlier);
            fixture.store.add(reservation("other", "theater", "other-user", 8));
            fixture.refresh();
            assertEquals(List.of(earlier, later), fixture.list.getItems());
            assertTrue(fixture.visibleTexts().stream().anyMatch(text -> text.startsWith("Study Room B")));
            assertFalse(fixture.visibleTexts().stream().anyMatch(text -> text.startsWith("Theater")));
            fixture.store.loadSnapshot(List.of());
            fixture.refresh();
            assertTrue(fixture.list.getItems().isEmpty());
            assertTrue(fixture.visibleTexts().isEmpty());
            assertTrue(fixture.list.getPlaceholder().isVisible());
        });
    }

    // Display-only fallback for records whose space is absent from the supplied catalog.
    @Test
    void rendersUnknownSpaceWithoutFailing() throws Exception {
        onFx(() -> {
            Fixture fixture = new Fixture();
            fixture.store.add(reservation("unknown", "missing-room", "local-user", 10));
            fixture.refresh();
            assertTrue(fixture.visibleTexts().stream()
                    .anyMatch(text -> text.startsWith("Unknown space (missing-room)")));
        });
    }

    @Test
    void reservationActionsAreDisabledUntilAReservationIsSelected() throws Exception {
        onFx(() -> {
            Fixture fixture = new Fixture();
            assertTrue(fixture.view.getCancelButton().isDisabled());
            assertTrue(fixture.view.getEditButton().isDisabled());

            fixture.store.add(reservation("booking", "study-room-a", "local-user", 10));
            fixture.refresh();
            fixture.list.getSelectionModel().selectFirst();

            assertFalse(fixture.view.getEditButton().isDisabled());
            assertFalse(fixture.view.getCancelButton().isDisabled());
        });
    }

    @Test
    void editActionPassesSelectedReservationToHandler() throws Exception {
        onFx(() -> {
            ReservationStore store = new ReservationStore();
            ReservationController reservationController = new ReservationController(store);
            SpaceController spaceController =
                    new SpaceController(InitialSpaceCatalog.getDefaultSpaces());
            AtomicReference<Reservation> editedReservation = new AtomicReference<>();

            MyReservationsView view = new MyReservationsView(
                    reservationController,
                    spaceController,
                    editedReservation::set,
                    reservation -> {
                    });

            new Scene(view, 700, 400);

            Reservation booking =
                    reservation("booking", "study-room-a", "local-user", 10);
            store.add(booking);
            view.refreshAfterReservationChange();
            view.applyCss();
            view.layout();

            ListView<?> list =
                    (ListView<?>) view.lookup("#my-reservations-list");
            list.getSelectionModel().selectFirst();

            view.getEditButton().fire();

            assertSame(booking, editedReservation.get());
        });
    }

    @Test
    void confirmedCancellationPassesSelectedReservationToHandler() throws Exception {
        onFx(() -> {
            ReservationStore store = new ReservationStore();
            ReservationController reservationController = new ReservationController(store);
            SpaceController spaceController =
                    new SpaceController(InitialSpaceCatalog.getDefaultSpaces());
            AtomicReference<Reservation> cancelledReservation = new AtomicReference<>();
            MyReservationsView view = new MyReservationsView(
                    reservationController,
                    spaceController,
                    reservation -> {
                    },
                    cancelledReservation::set);
            new Scene(view, 700, 400);

            Reservation booking =
                    reservation("booking", "study-room-a", "local-user", 10);
            store.add(booking);
            view.refreshAfterReservationChange();
            view.applyCss();
            view.layout();
            ListView<?> list =
                    (ListView<?>) view.lookup("#my-reservations-list");
            list.getSelectionModel().selectFirst();

            Platform.runLater(() -> Window.getWindows().stream()
                    .filter(window -> window.getScene() != null)
                    .map(window -> window.getScene().getRoot())
                    .filter(DialogPane.class::isInstance)
                    .map(DialogPane.class::cast)
                    .findFirst()
                    .ifPresent(dialogPane ->
                            ((Button) dialogPane.lookupButton(ButtonType.OK)).fire()));

            view.getCancelButton().fire();

            assertSame(booking, cancelledReservation.get());
        });
    }

    private static Reservation reservation(String id, String space, String owner, int hour) {
        return new Reservation(id, space, owner, LocalDate.of(2026, 10, 1),
                LocalTime.of(hour, 0), LocalTime.of(hour + 1, 0));
    }

    private static final class Fixture {
        final ReservationStore store = new ReservationStore();
        final MyReservationsView view = new MyReservationsView(new ReservationController(store),
                new SpaceController(InitialSpaceCatalog.getDefaultSpaces()));
        final ListView<?> list;

        Fixture() {
            new Scene(view, 700, 400);
            list = (ListView<?>) view.lookup("#my-reservations-list");
            refresh();
        }

        void refresh() {
            view.refreshAfterReservationChange();
            view.applyCss();
            view.layout();
        }

        List<String> visibleTexts() {
            return list.lookupAll(".list-cell").stream()
                    .filter(node -> node instanceof ListCell<?> cell && !cell.isEmpty())
                    .map(node -> ((ListCell<?>) node).getText()).toList();
        }
    }
}
