package persistence;

import java.util.List;

import model.Space;

public final class InitialSpaceCatalog {

    private InitialSpaceCatalog() {
    }

    public static List<Space> getDefaultSpaces() {
        return List.of(
            new Space(
                "study-room-a", 
                "Study Room A", 
                "Magnolia Building", 
                4, 
                List.of("Table", "whiteboard")
            ),
            new Space(
                "study-room-b", 
                "Study Room B", 
                "Magnolia Building", 
                6, 
                List.of("Whiteboard", "projector")
            ),
            new Space(
                "conference-room", 
                "Conference Room", 
                "Student Center", 
                20, 
                List.of("Conference table", "projector", "internet")
            ),
            new Space(
                "theater", 
                "Theater", 
                "University Center", 
                250, 
                List.of("Projector", "screen", "sound system")
            ),
            new Space(
                "science-lab", 
                "Science Lab", 
                "Science Building", 
                10, 
                List.of("Blackboard", "table", "science equipment")
            )
        );
    }
}