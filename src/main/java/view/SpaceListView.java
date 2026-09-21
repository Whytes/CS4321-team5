package view;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import model.Space;

/**
 * Displays the spaces available in the shared catalog and exposes its selection.
 */
public final class SpaceListView extends BorderPane {

    private final ListView<Space> spacesList = new ListView<>();
    private final ObservableList<Space> visibleSpaces = FXCollections.observableArrayList();
    private final SpaceDetailsView details = new SpaceDetailsView();

    public SpaceListView(List<Space> spaces) {
        if (spaces == null) {
            throw new IllegalArgumentException("spaces must not be null");
        }

        setPadding(new Insets(18, 0, 0, 0));
        spacesList.getStyleClass().add("space-list");
        Label emptyMessage = new Label("No spaces are currently available.");
        emptyMessage.getStyleClass().add("empty-message");
        spacesList.setPlaceholder(emptyMessage);
        spacesList.setItems(visibleSpaces);
        spacesList.setCellFactory(list -> new SpaceCell());
        setCenter(spacesList);
        setBottom(details);

        spacesList.getSelectionModel().selectedItemProperty()
                .addListener((observable, previous, selected) -> details.showSpace(selected));
        setSpaces(spaces);
    }

    /** Replaces the visible catalog and removes any selection no longer shown. */
    public void setSpaces(List<Space> spaces) {
        if (spaces == null) {
            throw new IllegalArgumentException("spaces must not be null");
        }

        Space selected = getSelectedSpace();
        visibleSpaces.setAll(spaces);
        if (selected != null && visibleSpaces.contains(selected)) {
            spacesList.getSelectionModel().select(selected);
        } else {
            spacesList.getSelectionModel().clearSelection();
        }
        details.showSpace(getSelectedSpace());
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
