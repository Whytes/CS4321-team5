package view;

import controller.ReservationController;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Locale;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.AvailabilitySlot;
import model.Space;

/** Displays reserved and available intervals for one space and date. */
public final class AvailabilityView extends VBox {

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_WITH_SECONDS_FORMAT =
            DateTimeFormatter.ofPattern("h:mm:ss a", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_WITH_FRACTION_FORMAT =
            new DateTimeFormatterBuilder()
                    .appendPattern("h:mm:ss")
                    .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
                    .appendPattern(" a")
                    .toFormatter(Locale.ENGLISH);

    private final ComboBox<Space> spaceSelector = new ComboBox<>();
    private final DatePicker datePicker = new DatePicker(LocalDate.now());
    private final ListView<AvailabilitySlot> scheduleList = new ListView<>();
    private final Label emptyMessage = new Label("Select a space to see its daily availability.");
    private final ReservationController reservationController;
    private final ObjectProperty<Space> selectedSpace = new SimpleObjectProperty<>();

    public AvailabilityView(List<Space> spaces, ReservationController reservationController) {
        if (spaces == null || reservationController == null) {
            throw new IllegalArgumentException("spaces and reservationController must not be null");
        }
        this.reservationController = reservationController;
        getStyleClass().add("availability-view");
        setSpacing(10);
        setPadding(new Insets(18, 0, 0, 0));

        spaceSelector.setItems(FXCollections.observableArrayList(spaces));
        spaceSelector.setPromptText("Select a space");
        spaceSelector.setMaxWidth(Double.MAX_VALUE);
        spaceSelector.setCellFactory(list -> new SpaceCell());
        spaceSelector.setButtonCell(new SpaceCell());
        datePicker.setMaxWidth(Double.MAX_VALUE);

        HBox controls = new HBox(10, labeled("Space", spaceSelector), labeled("Date", datePicker));
        controls.getStyleClass().add("availability-controls");
        HBox.setHgrow(controls.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(controls.getChildren().get(1), Priority.ALWAYS);

        scheduleList.getStyleClass().add("availability-list");
        scheduleList.setPlaceholder(emptyMessage);
        scheduleList.setCellFactory(list -> new ScheduleCell());
        VBox.setVgrow(scheduleList, Priority.ALWAYS);
        getChildren().addAll(controls, scheduleList);

        spaceSelector.valueProperty().bindBidirectional(selectedSpace);
        spaceSelector.valueProperty().addListener((observable, oldValue, newValue) -> refresh());
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> refresh());
    }

    private static VBox labeled(String labelText, javafx.scene.Node control) {
        Label label = new Label(labelText);
        label.getStyleClass().add("availability-label");
        VBox box = new VBox(4, label, control);
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    public void refresh() {
        Space space = selectedSpace.get();
        LocalDate date = datePicker.getValue();
        if (space == null || date == null) {
            scheduleList.getItems().clear();
            return;
        }
        scheduleList.getItems().setAll(
                reservationController.getDailySchedule(space.getSpaceId(), date));
    }

    public void refreshAfterReservationChange() {
        refresh();
    }

    public ObjectProperty<Space> selectedSpaceProperty() {
        return selectedSpace;
    }

    public DatePicker getDatePicker() {
        return datePicker;
    }

    private static final class SpaceCell extends ListCell<Space> {
        @Override
        protected void updateItem(Space space, boolean empty) {
            super.updateItem(space, empty);
            setText(empty || space == null ? null : space.getName());
        }
    }

    private static final class ScheduleCell extends ListCell<AvailabilitySlot> {
        private final Label status = new Label();
        private final Label interval = new Label();
        private final VBox content = new VBox(3, status, interval);

        private ScheduleCell() {
            content.getStyleClass().add("availability-cell");
        }

        @Override
        protected void updateItem(AvailabilitySlot slot, boolean empty) {
            super.updateItem(slot, empty);
            if (empty || slot == null) {
                setGraphic(null);
                return;
            }
            status.setText(slot.getStatusLabel());
            interval.setText(formatTime(slot.startTime()) + " - " + formatTime(slot.endTime()));
            content.getStyleClass().removeAll("reserved-slot", "available-slot");
            content.getStyleClass().add(slot.reserved() ? "reserved-slot" : "available-slot");
            setGraphic(content);
        }

    }

    static String formatTime(LocalTime time) {
        if (time.equals(LocalTime.MIN)) {
            return "Start of day";
        }
        if (time.equals(LocalTime.MAX)) {
            return "End of day";
        }
        if (time.getNano() != 0) {
            return TIME_WITH_FRACTION_FORMAT.format(time);
        }
        if (time.getSecond() != 0) {
            return TIME_WITH_SECONDS_FORMAT.format(time);
        }
        return TIME_FORMAT.format(time);
    }
}
