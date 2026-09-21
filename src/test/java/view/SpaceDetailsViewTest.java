package view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import model.Space;

class SpaceDetailsViewTest {

    @Test
    void formatsSelectedSpaceSummaryAndFeatures() {
        Space space = new Space("room-a", "Room A", "Main Building", 12,
                List.of("Whiteboard", "Projector"));

        assertEquals("Main Building  |  Capacity 12", SpaceDetailsView.formatSummary(space));
        assertEquals("Features: Whiteboard, Projector", SpaceDetailsView.formatFeatures(space));
    }

    @Test
    void formatsEmptyFeaturesWithoutInventingDetails() {
        Space space = new Space("room-a", "Room A", "Main Building", 12, List.of());

        assertEquals("Features: ", SpaceDetailsView.formatFeatures(space));
    }
}