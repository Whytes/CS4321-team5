package view;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import controller.ReservationController;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import model.Reservation;
import model.Space;
import service.ReservationCreationResult;

/** Collects reservation details and delegates validation to the controller. */
public final class ReservationFormView extends VBox {

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("H:mm").withResolverStyle(ResolverStyle.STRICT);

    private final ReservationController reservationController;
    private final ComboBox<Space> spaceSelector = new ComboBox<>();
    private final DatePicker datePicker = new DatePicker(LocalDate.now());
    private final TextField startTimeField = new TextField();
    private final TextField endTimeField = new TextField();
    private final Label feedbackLabel = new Label();
    private final Button submitButton = new Button("Create reservation");
    private final Button cancelButton = new Button("Cancel");
    private final Consumer<Reservation> successListener;
    private final DateTimeFormatter dateParser = new DateTimeFormatterBuilder()
            .parseLenient()
            .appendLocalized(FormatStyle.SHORT, null)
            .parseDefaulting(ChronoField.ERA, 1)
            .toFormatter(Locale.getDefault(Locale.Category.FORMAT))
            .withChronology(datePicker.getChronology())
            .withResolverStyle(ResolverStyle.STRICT);
    private String invalidDateText;

    public ReservationFormView(
            List<Space> spaces,
            ReservationController reservationController,
            Consumer<Reservation> successListener,
            Runnable cancelListener) {
        if (spaces == null || reservationController == null || successListener == null
                || cancelListener == null) {
            throw new IllegalArgumentException(
                    "spaces, reservationController, and listeners must not be null");
        }

        this.reservationController = reservationController;
        this.successListener = successListener;
        spaceSelector.setId("reservation-space");
        datePicker.setId("reservation-date");
        startTimeField.setId("reservation-start");
        endTimeField.setId("reservation-end");
        feedbackLabel.setId("reservation-feedback");
        feedbackLabel.setWrapText(true);
        submitButton.setId("reservation-submit");
        cancelButton.setId("reservation-cancel");
        getStyleClass().add("reservation-form");
        setSpacing(12);
        setPadding(new Insets(18, 0, 0, 0));

        spaceSelector.setItems(FXCollections.observableArrayList(spaces));
        spaceSelector.setPromptText("Select a space");
        spaceSelector.setMaxWidth(Double.MAX_VALUE);
        spaceSelector.setCellFactory(list -> new SpaceCell());
        spaceSelector.setButtonCell(new SpaceCell());
        datePicker.setMaxWidth(Double.MAX_VALUE);
        StringConverter<LocalDate> displayConverter = datePicker.getConverter();
        datePicker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return invalidDateText == null ? displayConverter.toString(date) : invalidDateText;
            }

            @Override
            public LocalDate fromString(String text) {
                try {
                    LocalDate date = parseDate(text);
                    invalidDateText = null;
                    return date;
                } catch (DateTimeParseException exception) {
                    // DatePicker commits on focus loss and Enter, outside submit's handler.
                    // Keep the invalid text for correction without throwing on the UI thread.
                    invalidDateText = text;
                    showError("Enter a valid date or choose one from the calendar.");
                    return datePicker.getValue();
                }
            }
        });
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            invalidDateText = null;
            datePicker.getEditor().setText(newValue == null
                    ? "" : displayConverter.toString(newValue));
        });
        datePicker.getEditor().setText(
            datePicker.getConverter().toString(datePicker.getValue()));
        startTimeField.setPromptText("09:00");
        endTimeField.setPromptText("10:00");
        startTimeField.setTextFormatter(new javafx.scene.control.TextFormatter<String>(
                change -> change.getControlNewText().length() <= 5 ? change : null));
        endTimeField.setTextFormatter(new javafx.scene.control.TextFormatter<String>(
                change -> change.getControlNewText().length() <= 5 ? change : null));

        HBox firstRow = new HBox(10,
                labeled("Space", spaceSelector),
                labeled("Date", datePicker));
        HBox secondRow = new HBox(10,
                labeled("Start time", startTimeField),
                labeled("End time", endTimeField));
        HBox.setHgrow(firstRow.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(firstRow.getChildren().get(1), Priority.ALWAYS);
        HBox.setHgrow(secondRow.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(secondRow.getChildren().get(1), Priority.ALWAYS);

        feedbackLabel.getStyleClass().add("validation-feedback");
        HBox actions = new HBox(8, submitButton, cancelButton);
        submitButton.setDefaultButton(true);
        submitButton.setOnAction(event -> submit());
        cancelButton.setCancelButton(true);
        cancelButton.setOnAction(event -> cancelListener.run());
        addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE
                    && !spaceSelector.isShowing() && !datePicker.isShowing()) {
                cancelButton.fire();
                event.consume();
            }
        });

        getChildren().addAll(firstRow, secondRow, actions, feedbackLabel);
    }

    private static VBox labeled(String labelText, javafx.scene.Node control) {
        Label label = new Label(labelText);
        label.getStyleClass().add("availability-label");
        VBox box = new VBox(4, label, control);
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    private void submit() {
        LocalDate date;
        try {
            // Read the visible editor text: its value may not yet be committed by focus loss.
            date = parseDate(datePicker.getEditor().getText());
        } catch (DateTimeParseException exception) {
            showError("Enter a valid date or choose one from the calendar.");
            return;
        }

        LocalTime startTime;
        LocalTime endTime;
        try {
            startTime = parseTime(startTimeField.getText());
            endTime = parseTime(endTimeField.getText());
        } catch (DateTimeParseException exception) {
            showError("Enter start and end times using HH:mm, such as 09:00.");
            return;
        }

        Space selectedSpace = spaceSelector.getValue();
        ReservationCreationResult result = reservationController.createReservation(
                selectedSpace == null ? null : selectedSpace.getSpaceId(),
                date, startTime, endTime);
        if (!result.isSuccess()) {
            showError(result.getMessage());
            return;
        }

        feedbackLabel.getStyleClass().removeAll("validation-feedback", "success-feedback");
        feedbackLabel.getStyleClass().add("success-feedback");
        feedbackLabel.setText(result.getMessage());
        Reservation reservation = result.getReservation();
        clearInputs();
        successListener.accept(reservation);
    }

    private static LocalTime parseTime(String text) {
        return text == null || text.isBlank() ? null : LocalTime.parse(text.trim(), TIME_FORMAT);
    }

    private LocalDate parseDate(String text) {
        return text == null || text.isBlank() ? null : LocalDate.parse(text.trim(), dateParser);
    }

    private void clearInputs() {
        invalidDateText = null;
        spaceSelector.getSelectionModel().clearSelection();
        datePicker.setValue(LocalDate.now());
        datePicker.getEditor().setText(datePicker.getConverter().toString(datePicker.getValue()));
        startTimeField.clear();
        endTimeField.clear();
    }

    private void showError(String message) {
        feedbackLabel.getStyleClass().removeAll("validation-feedback", "success-feedback");
        feedbackLabel.getStyleClass().add("validation-feedback");
        feedbackLabel.setText(message);
    }

    private static final class SpaceCell extends javafx.scene.control.ListCell<Space> {
        @Override
        protected void updateItem(Space space, boolean empty) {
            super.updateItem(space, empty);
            setText(empty || space == null ? null : space.getName());
        }
    }
}
