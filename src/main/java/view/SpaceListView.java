package view;

import java.util.List;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import model.Space;

/**
 * Displays the spaces available in the shared catalog and exposes its selection.
 */
public final class SpaceListView extends BorderPane {

    private final ListView<Space> spacesList = new ListView<>();
    private final Label emptyMessage = new Label();
    private final VBox details = new VBox(5);

    public SpaceListView(List<Space> spaces) {
        if (spaces == null) {
            throw new IllegalArgumentException("spaces must not be null");
        }

        setPadding(new Insets(18, 0, 0, 0));
        spacesList.getStyleClass().add("space-list");
        spacesList.setPlaceholder(emptyMessage);
        spacesList.setItems(javafx.collections.FXCollections.observableArrayList(spaces));
        spacesList.setCellFactory(list -> new SpaceCell());
        setCenter(spacesList);

        emptyMessage.setText("No spaces are currently available.");
        emptyMessage.getStyleClass().add("empty-message");

        details.getStyleClass().add("space-details");
        details.setPadding(new Insets(16, 0, 0, 0));
        refreshDetails(null);
        setBottom(details);

        spacesList.getSelectionModel().selectedItemProperty()
                .addListener((observable, previous, selected) -> refreshDetails(selected));
    }

    public ReadOnlyObjectProperty<Space> selectedSpaceProperty() {
        return spacesList.getSelectionModel().selectedItemProperty();
    }

    public Space getSelectedSpace() {
        return spacesList.getSelectionModel().getSelectedItem();
    }

    private void refreshDetails(Space space) {
        details.getChildren().clear();
        if (space == null) {
            Label message = new Label("Select a space to view its details.");
            message.getStyleClass().add("space-details-message");
            details.getChildren().add(message);
            return;
        }

        Label title = new Label(space.getName());
        title.getStyleClass().add("space-details-title");
        Label summary = new Label(space.getBuilding() + "  |  Capacity " + space.getCapacity());
        summary.getStyleClass().add("space-details-summary");
        Label features = new Label("Features: " + String.join(", ", space.getFeatures()));
        features.getStyleClass().add("space-details-summary");
        details.getChildren().addAll(title, summary, features, new Separator());
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
