package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.Space;

class SpaceControllerTest {

    private SpaceController controller;

    @BeforeEach
    void setUp() {
        List<Space> sampleSpaces = List.of(
            new Space("S1", "Room A", "Main Building", 10, List.of()),
            new Space("S2", "Room B", "Main Building", 20, List.of()), 
            new Space("S3", "Room C", "Main Building", 30, List.of())
        );
        controller = new SpaceController(sampleSpaces);
    }
    
    @Test
    void testFilterByMinCapacityExactBoundary() {
        List<Space> result = controller.filterByMinCapacity(20);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(s -> s.getSpaceId().equals("S2")));
        assertTrue(result.stream().anyMatch(s -> s.getSpaceId().equals("S3")));
    }

    @Test
    void testFilterByMinCapacityClearOrNull() {
        List<Space> resultNull = controller.filterByMinCapacity((Integer) null);
        assertEquals(3, resultNull.size());

        List<Space> resultEmptyStr = controller.filterByMinCapacity("   ");
        assertEquals(3, resultEmptyStr.size());
    }

    @Test
    void testFilterByMinCapacityNoMatches() {
        List<Space> result = controller.filterByMinCapacity(100);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFilterByMinCapacityInvalidInput() {
        assertThrows(NumberFormatException.class, () -> {
            controller.filterByMinCapacity("abc");
        });
    }

    @Test
    void testEmptyCatalogReturnsNoSpaces() {
        SpaceController emptyController = new SpaceController(List.of());

        assertTrue(emptyController.getSpaces().isEmpty());
        assertTrue(emptyController.filterByMinCapacity(10).isEmpty());
    }
}