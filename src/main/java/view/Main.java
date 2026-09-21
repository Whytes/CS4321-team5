package view;

import controller.ApplicationController;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Space;
import persistence.InitialSpaceCatalog;

public class Main extends Application {

    private final BorderPane content = new BorderPane();
    private ApplicationController applicationController;
    private SpaceListView spaceListView;

    @Override
    public void start(Stage stage) {
        applicationController = new ApplicationController(InitialSpaceCatalog.getDefaultSpaces());

        BorderPane root = new BorderPane();
        root.setTop(createHeader());
        root.setLeft(createNavigation());
        root.setCenter(content);

        showBrowseSpaces();

        Scene scene = new Scene(root, 960, 600);
        scene.getStylesheets().add(Main.class.getResource("main.css").toExternalForm());

        stage.setTitle("Campus Space Reservations");
        stage.setMinWidth(760);
        stage.setMinHeight(480);
        stage.setScene(scene);
        stage.show();
    }

    private Node createHeader() {
        Label title = new Label("Campus Space Reservations");
        title.getStyleClass().add("app-title");

        Label subtitle = new Label("Find a room. Plan your time.");
        subtitle.getStyleClass().add("app-subtitle");

        VBox branding = new VBox(3, title, subtitle);

        Label user = new Label("Local User");
        user.getStyleClass().add("user-badge");

        HBox header = new HBox(16, branding, user);
        header.getStyleClass().add("app-header");
        header.setPadding(new Insets(18, 24, 18, 24));
        HBox.setHgrow(branding, Priority.ALWAYS);
        return header;
    }

    private Node createNavigation() {
        ToggleGroup pages = new ToggleGroup();
        VBox navigation = new VBox(8);
        navigation.getStyleClass().add("sidebar");
        navigation.setPadding(new Insets(20, 12, 20, 12));
        navigation.setPrefWidth(190);

        Label section = new Label("WORKSPACE");
        section.getStyleClass().add("sidebar-label");
        navigation.getChildren().add(section);
        Separator separator = new Separator();
        separator.getStyleClass().add("sidebar-separator");
        navigation.getChildren().add(separator);

        addNavigationButton(navigation, pages, "Browse spaces", "Browse spaces",
                "Select a space to view its details.");
        addNavigationButton(navigation, pages, "Daily availability", "Daily availability",
                "Choose a space and date to see reserved and available time.");
        addNavigationButton(navigation, pages, "New reservation", "New reservation",
                "Reservation entry will be available here.");
        addNavigationButton(navigation, pages, "My reservations", "My reservations",
                "Your reservations will appear here.");

        ToggleButton first = (ToggleButton) navigation.getChildren().get(2);
        first.fire();
        return navigation;
    }

    private void addNavigationButton(VBox navigation, ToggleGroup pages, String label,
                                     String title, String message) {
        ToggleButton button = new ToggleButton(label);
        button.getStyleClass().add("nav-button");
        button.setToggleGroup(pages);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(event -> {
            if ("Browse spaces".equals(title)) {
                showBrowseSpaces();
            } else {
                showPage(title, message);
            }
        });
        navigation.getChildren().add(button);
    }

    private void showBrowseSpaces() {
        Label eyebrow = new Label("RESERVATION WORKSPACE");
        eyebrow.getStyleClass().add("page-eyebrow");

        Label heading = new Label("Browse spaces");
        heading.getStyleClass().add("page-title");

        Label message = new Label("Select a space to view its details.");
        message.getStyleClass().add("page-message");
        message.setWrapText(true);

        spaceListView = new SpaceListView(
                applicationController.getSpaceController().getSpaces(),
                applicationController.getSpaceController());
        VBox page = new VBox(10, eyebrow, heading, message, spaceListView);
        page.getStyleClass().add("page");
        page.setPadding(new Insets(32));
        VBox.setVgrow(spaceListView, Priority.ALWAYS);
        content.setCenter(page);
    }

    private void showPage(String heading, String message) {
        Label eyebrow = new Label("RESERVATION WORKSPACE");
        eyebrow.getStyleClass().add("page-eyebrow");

        Label headingLabel = new Label(heading);
        headingLabel.getStyleClass().add("page-title");

        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("page-message");
        messageLabel.setWrapText(true);

        Label catalogValue = new Label(String.valueOf(applicationController.getSpaceController()
            .getSpaces().size()));
        catalogValue.getStyleClass().add("metric-value");
        Label catalogLabel = new Label("spaces in catalog");
        catalogLabel.getStyleClass().add("metric-label");
        VBox catalogMetric = new VBox(2, catalogValue, catalogLabel);
        catalogMetric.getStyleClass().add("metric");

        Label statusValue = new Label("Ready");
        statusValue.getStyleClass().add("status-value");
        Label statusLabel = new Label("shared session state");
        statusLabel.getStyleClass().add("metric-label");
        VBox statusMetric = new VBox(2, statusValue, statusLabel);
        statusMetric.getStyleClass().add("metric");

        HBox metrics = new HBox(12, catalogMetric, statusMetric);
        metrics.getStyleClass().add("metrics");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        VBox page = new VBox(10, eyebrow, headingLabel, messageLabel, spacer, metrics);
        page.getStyleClass().add("page");
        page.setPadding(new Insets(32));
        content.setCenter(page);
    }

    public ApplicationController getApplicationController() {
        return applicationController;
    }

    public Space getSelectedSpace() {
        return spaceListView == null ? null : spaceListView.getSelectedSpace();
    }

    public ReadOnlyObjectProperty<Space> selectedSpaceProperty() {
        return spaceListView == null ? null : spaceListView.selectedSpaceProperty();
    }

    public static void main(String[] args) {
        launch(args);
    }
}