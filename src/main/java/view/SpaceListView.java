package view;

import java.util.List;

import controller.SpaceController;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Space;

/**
 * Displays the spaces available in the shared catalog and exposes its selection.
 */
public final class SpaceListView extends BorderPane {

    private final ListView<Space> spacesList = new ListView<>();
    private final ObservableList<Space> visibleSpaces = FXCollections.observableArrayList();
    private final SpaceDetailsView details = new SpaceDetailsView();
    private final TextField capacityField = new TextField();
    private final Button applyButton = new Button("Apply");
    private final Button clearButton = new Button("Clear");
    private final Label feedbackLabel = new Label();

    private List<Space> masterSpaces;
    private SpaceController spaceController;

    public SpaceListView(List<Space> spaces, SpaceController spaceController) {
        if (spaces == null || spaceController == null) {
            throw new IllegalArgumentException("spaces and spaceController must not be null");
        }
        this.masterSpaces = spaces;
        this.spaceController = spaceController;

        setPadding(new Insets(18, 0, 0, 0));
        spacesList.getStyleClass().add("space-list");

        Label emptyMessage = new Label("No space meets requested minimum capacity.");
        emptyMessage.getStyleClass().add("empty-message");
        spacesList.setPlaceholder(emptyMessage);

        spacesList.setItems(visibleSpaces);
        spacesList.setCellFactory(list -> new SpaceCell());

        capacityField.setPromptText("Enter Valid Number");
        capacityField.setPrefWidth(125);
        feedbackLabel.getStyleClass().add("validation-feedback");

        HBox filterBar = new HBox(8, new Label("Minimum capacity:"), capacityField, applyButton, clearButton, feedbackLabel);
        filterBar.getStyleClass().add("filter-bar");
        filterBar.setPadding(new Insets(10, 14, 10, 14));
        filterBar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        VBox centerContainer = new VBox(8, filterBar, spacesList);
        VBox.setVgrow(spacesList, javafx.scene.layout.Priority.ALWAYS);

        setCenter(centerContainer);
        setBottom(details);

        applyButton.setOnAction(e -> handleApplyFilter());
        clearButton.setOnAction(e -> handleClearFilter());
        capacityField.setOnAction(e -> handleApplyFilter());

        spacesList.getSelectionModel().selectedItemProperty()
                .addListener((observable, previous, selected) -> details.showSpace(selected));

        setSpaces(spaces);
    }

    private void handleApplyFilter() {
        String input = capacityField.getText();
        if (input == null || input.trim().isEmpty()) {
            handleClearFilter();
            return;
        }

        try {
            List<Space> filtered = spaceController.filterByMinCapacity(input);
            feedbackLabel.setText("");
            updateVisibleSpaces(filtered);
        } catch (NumberFormatException e) {
            feedbackLabel.setText("Please enter a valid whole number.");
        }
    }

    private void handleClearFilter() {
        capacityField.clear();
        feedbackLabel.setText("");
        updateVisibleSpaces(masterSpaces);
    }

    private void updateVisibleSpaces(List<Space> spaces) {
        Space selected = getSelectedSpace();
        visibleSpaces.setAll(spaces);
        if (selected != null && visibleSpaces.contains(selected)) {
            spacesList.getSelectionModel().select(selected);
        } else {
            spacesList.getSelectionModel().clearSelection();
        }
        details.showSpace(getSelectedSpace());
    }

    public void setSpaces(List<Space> spaces) {
        if (spaces == null) {
            throw new IllegalArgumentException("spaces must not be null");
        }
        this.masterSpaces = spaces;
        handleApplyFilter();
    }

    public ReadOnlyObjectProperty<Space> selectedSpaceProperty() {
        return spacesList.getSelectionModel().selectedItemProperty();
    }

    public Space getSelectedSpace() {
        return spacesList.getSelectionModel().getSelectedItem();
    }

    private static final class SpaceCell extends ListCell<Space> {
        private final Label name = new Label();
        private final Label summary = new Label();
        private final VBox content = new VBox(4, name, summary);

        private SpaceCell() {
            name.getStyleClass().add("space-cell-name");
            summary.getStyleClass().add("space-cell-summary");
            content.getStyleClass().add("space-cell");
        }

        @Override
        protected void updateItem(Space space, boolean empty) {
            super.updateItem(space, empty);
            if (empty || space == null) {
                setText(null);
                setGraphic(null);
            } else {
                name.setText(space.getName());
                summary.setText(space.getBuilding() + "  |  Capacity " + space.getCapacity());
                setGraphic(content);
            }
        }
    }
} 