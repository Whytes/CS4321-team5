package view;

import controller.ReservationController;
import java.util.List;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.Reservation;
import model.Space;

public final class MyReservationsView extends VBox {

    private final ReservationController reservationController;
    private final ListView<Reservation> reservationList = new ListView<>();
    private final List<Space> spaces;

    public MyReservationsView(List<Space> spaces, ReservationController reservationController) {

        if (spaces == null || reservationController == null) {
            throw new IllegalArgumentException("spaces and reservationController must not be null");
        }

        this.reservationController = reservationController;
        this.spaces = List.copyOf(spaces);

        reservationList.setPlaceholder(new Label("No reservations found."));
        reservationList.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(reservationList, Priority.ALWAYS);

        reservationList.setCellFactory(list -> new ReservationCell());

        getChildren().add(reservationList);

        refresh();
    }

    public void refresh() {
        Reservation selectedReservation = getSelectedReservation();

        reservationList.getItems().setAll(
                reservationController.getMyReservations());

        if (selectedReservation != null) {
            reservationList.getSelectionModel().select(selectedReservation);
        }
    }

    public void refreshAfterReservationChange() {
        refresh();
    }

    public Reservation getSelectedReservation() {
        return reservationList.getSelectionModel().getSelectedItem();
    }

    public ReadOnlyObjectProperty<Reservation> selectedReservationProperty() {
        return reservationList.getSelectionModel().selectedItemProperty();
    }

    private final class ReservationCell extends ListCell<Reservation> {

        private final Label spaceName = new Label();
        private final Label dateAndTime = new Label();
        private final VBox content = new VBox(4, spaceName, dateAndTime);

        @Override
        protected void updateItem(Reservation reservation, boolean empty) {
            super.updateItem(reservation, empty);

            if (empty || reservation == null) {
                setGraphic(null);
                return;
            }

            spaceName.setText(getSpaceName(reservation.getSpaceId()));

            dateAndTime.setText(
                reservation.getDate()
                    + " • "
                    + AvailabilityView.formatTime(reservation.getStartTime())
                    + " - "
                    + AvailabilityView.formatTime(reservation.getEndTime()));

            setGraphic(content);

        }
    }

    private String getSpaceName(String spaceId) {
        return spaces.stream()
                .filter(space -> space.getSpaceId().equals(spaceId))
                .map(Space::getName)
                .findFirst()
                .orElse(spaceId);
    }
}