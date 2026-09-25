package view;

import java.time.format.DateTimeFormatter;

import controller.ReservationController;
import controller.SpaceController;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Reservation;
import model.Space;
import java.util.function.Consumer;

/** Displays the local user's reservations from the shared reservation store. */
public final class MyReservationsView extends VBox {

    private final ReservationController reservationController;
    private final ListView<Reservation> reservationList = new ListView<>();
    private final Button cancelButton = new Button("Cancel reservation");
    private final Consumer<Reservation> cancellationHandler;

    public MyReservationsView(ReservationController reservationController,
                              SpaceController spaceController) {
        this(reservationController, spaceController, reservation -> {
        });
    }

    public MyReservationsView(ReservationController reservationController,
                              SpaceController spaceController,
                              Consumer<Reservation> cancellationHandler) {
        if (reservationController == null || spaceController == null
                || cancellationHandler == null) {
            throw new IllegalArgumentException("dependencies must not be null");
        }
        this.reservationController = reservationController;
        this.cancellationHandler = cancellationHandler;
        getStyleClass().add("my-reservations-view");
        setPadding(new Insets(18, 0, 0, 0));
        reservationList.setPlaceholder(new Label("You have no reservations."));
        reservationList.setId("my-reservations-list");
        reservationList.setCellFactory(list -> new ReservationCell(spaceController));
        cancelButton.setId("cancel-reservation");
        cancelButton.setDisable(true);
        cancelButton.setOnAction(event -> confirmCancellation());
        reservationList.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) ->
                        cancelButton.setDisable(newValue == null));
        VBox.setVgrow(reservationList, javafx.scene.layout.Priority.ALWAYS);
        HBox actions = new HBox(cancelButton);
        actions.getStyleClass().add("reservation-actions");
        getChildren().addAll(reservationList, actions);
        refresh();
    }

    public void refresh() {
        reservationList.setItems(FXCollections.observableArrayList(
                reservationController.getMyReservations()));
    }

    public void refreshAfterReservationChange() {
        refresh();
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    private void confirmCancellation() {
        Reservation selected = reservationList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        Alert confirmation = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Cancel this reservation?",
                ButtonType.OK,
                ButtonType.CANCEL);
        confirmation.setTitle("Cancel reservation");
        confirmation.setHeaderText("Confirm cancellation");
        confirmation.showAndWait()
                .filter(ButtonType.OK::equals)
                .ifPresent(button -> cancellationHandler.accept(selected));
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
