package view;

import java.time.format.DateTimeFormatter;

import controller.ReservationController;
import controller.SpaceController;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import model.Reservation;
import model.Space;

/** Displays the local user's reservations from the shared reservation store. */
public final class MyReservationsView extends VBox {

    private final ReservationController reservationController;
    private final ListView<Reservation> reservationList = new ListView<>();

    public MyReservationsView(ReservationController reservationController,
                              SpaceController spaceController) {
        if (reservationController == null || spaceController == null) {
            throw new IllegalArgumentException("controllers must not be null");
        }
        this.reservationController = reservationController;
        getStyleClass().add("my-reservations-view");
        setPadding(new Insets(18, 0, 0, 0));
        reservationList.setPlaceholder(new Label("You have no reservations."));
        reservationList.setId("my-reservations-list");
        reservationList.setCellFactory(list -> new ReservationCell(spaceController));
        VBox.setVgrow(reservationList, javafx.scene.layout.Priority.ALWAYS);
        getChildren().add(reservationList);
        refresh();
    }

    public void refresh() {
        reservationList.setItems(FXCollections.observableArrayList(
                reservationController.getMyReservations()));
    }

    public void refreshAfterReservationChange() {
        refresh();
    }

    private static final class ReservationCell extends ListCell<Reservation> {
        private final SpaceController spaceController;
        private static final DateTimeFormatter DATE_FORMAT =
                DateTimeFormatter.ofPattern("MMM d, yyyy");
        private static final DateTimeFormatter TIME_FORMAT =
                DateTimeFormatter.ofPattern("h:mm a");

        private ReservationCell(SpaceController spaceController) {
            this.spaceController = spaceController;
        }

        @Override
        protected void updateItem(Reservation reservation, boolean empty) {
            super.updateItem(reservation, empty);
            if (empty || reservation == null) {
                setText(null);
                return;
            }
            String spaceName = spaceController.findById(reservation.getSpaceId())
                    .map(Space::getName).orElse("Unknown space (" + reservation.getSpaceId() + ")");
            setText(spaceName + "  |  "
                    + DATE_FORMAT.format(reservation.getDate()) + "  |  "
                    + TIME_FORMAT.format(reservation.getStartTime()) + " - "
                    + TIME_FORMAT.format(reservation.getEndTime()));
        }
    }
}
