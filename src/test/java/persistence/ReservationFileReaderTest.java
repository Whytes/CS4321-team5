package persistence;

import model.ReservationStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReservationFileReaderTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void missingFileLoadsAnEmptyStore() throws IOException {
        Path file = temporaryDirectory.resolve("missing-reservations.json");

        ReservationStore store = new ReservationFileReader(file).read();

        assertTrue(store.getReservations().isEmpty());
    }

    @Test
    void emptySavedCollectionLoadsAnEmptyStore() throws IOException {
        Path file = temporaryDirectory.resolve("empty-reservations.json");
        Files.writeString(file, "[]");

        ReservationStore store = new ReservationFileReader(file).read();

        assertTrue(store.getReservations().isEmpty());
    }

    @Test
    void loadsSavedReservation() throws IOException {
        Path file = temporaryDirectory.resolve("reservations.json");
        Files.writeString(
                file,
                "[{\"reservationId\":\"reservation-1\","
                        + "\"spaceId\":\"study-room-a\","
                        + "\"ownerId\":\"local-user\","
                        + "\"date\":\"2020-10-01\","
                        + "\"startTime\":\"10:15\","
                        + "\"endTime\":\"11:45\"}]");

        ReservationStore store = new ReservationFileReader(file).read();

        assertEquals(1, store.getReservations().size());
        assertEquals("reservation-1", store.getReservations().get(0).getReservationId());
    }
    @Test
void reportsMalformedJson() throws IOException {
    Path file = temporaryDirectory.resolve("broken.json");
    Files.writeString(file, "not valid JSON");

    org.junit.jupiter.api.Assertions.assertThrows(
            IOException.class,
            () -> new ReservationFileReader(file).read());
}@Test
void reportsUnknownSpace() throws IOException {
    Path file = temporaryDirectory.resolve("unknown-space.json");
    Files.writeString(
            file,
            "[{\"reservationId\":\"reservation-1\","
                    + "\"spaceId\":\"missing-space\","
                    + "\"ownerId\":\"local-user\","
                    + "\"date\":\"2026-10-01\","
                    + "\"startTime\":\"10:15\","
                    + "\"endTime\":\"11:45\"}]");

    org.junit.jupiter.api.Assertions.assertThrows(
            IOException.class,
            () -> new ReservationFileReader(file).read());
}
@Test
void reportsMissingReservationField() throws IOException {
    Path file = temporaryDirectory.resolve("missing-field.json");
    Files.writeString(
            file,
            "[{\"reservationId\":\"reservation-1\","
                    + "\"spaceId\":\"study-room-a\","
                    + "\"date\":\"2026-10-01\","
                    + "\"startTime\":\"10:15\","
                    + "\"endTime\":\"11:45\"}]");

    org.junit.jupiter.api.Assertions.assertThrows(
            IOException.class,
            () -> new ReservationFileReader(file).read());
}
@Test
void reportsInvalidReservationTime() throws IOException {
    Path file = temporaryDirectory.resolve("invalid-time.json");
    Files.writeString(
            file,
            "[{\"reservationId\":\"reservation-1\","
                    + "\"spaceId\":\"study-room-a\","
                    + "\"ownerId\":\"local-user\","
                    + "\"date\":\"2026-10-01\","
                    + "\"startTime\":\"not-a-time\","
                    + "\"endTime\":\"11:45\"}]");

    org.junit.jupiter.api.Assertions.assertThrows(
            IOException.class,
            () -> new ReservationFileReader(file).read());
}
}