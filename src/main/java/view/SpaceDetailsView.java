package view;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import model.Space;

/** Displays details for the currently selected space. */
public final class SpaceDetailsView extends VBox {

    private final Label emptyMessage = new Label("Select a space to view its details.");
    private final Label title = new Label();
    private final Label summary = new Label();
    private final Label features = new Label();

    public SpaceDetailsView() {
        super(5);
        getStyleClass().add("space-details");
        setPadding(new Insets(16));

        emptyMessage.getStyleClass().add("space-details-message");
        title.getStyleClass().add("space-details-title");
        summary.getStyleClass().add("space-details-summary");
        features.getStyleClass().add("space-details-summary");
        showSpace(null);
    }

    public void showSpace(Space space) {
        getChildren().clear();
        if (space == null) {
            getChildren().add(emptyMessage);
            return;
        }

        title.setText(space.getName());
        summary.setText(formatSummary(space));
        features.setText(formatFeatures(space));
        getChildren().addAll(title, summary, features, new Separator());
    }

    static String formatSummary(Space space) {
        return space.getBuilding() + "  |  Capacity " + space.getCapacity();
    }

    static String formatFeatures(Space space) {
        return "Features: " + String.join(", ", space.getFeatures());
    }
}