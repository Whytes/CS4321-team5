package persistence;

import model.Reservation;
import model.ReservationStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReservationFileWriterTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void writesAllReservationFieldsToConfiguredPath() throws IOException {
        Path file = temporaryDirectory.resolve("nested").resolve("reservations.json");
        Reservation reservation = reservation("reservation-1");

        new ReservationFileWriter(file).write(List.of(reservation));

        assertEquals(
                "[{\"reservationId\":\"reservation-1\",\"spaceId\":\"study-room-a\","
                        + "\"ownerId\":\"local-user\",\"date\":\"2026-10-01\","
                        + "\"startTime\":\"10:15\",\"endTime\":\"11:45\"}]"
                        + System.lineSeparator(),
                Files.readString(file));
    }

    @Test
    void writesEmptyJsonArrayAndReplacesExistingSnapshot() throws IOException {
        Path file = temporaryDirectory.resolve("reservations.json");
        ReservationStore store = new ReservationStore();
        store.add(reservation("reservation-1"));
        ReservationFileWriter writer = new ReservationFileWriter(file);

        writer.write(store);
        store.remove("reservation-1");
        writer.write(store);

        assertEquals("[]" + System.lineSeparator(), Files.readString(file));
    }

    @Test
    void writesWhenDestinationFilenameIsShort() throws IOException {
        Path file = temporaryDirectory.resolve("a");

        new ReservationFileWriter(file).write(List.of(reservation("reservation-1")));

        assertTrue(Files.exists(file));
        assertTrue(Files.readString(file).contains("\"reservationId\":\"reservation-1\""));
    }

    @Test
    void reportsWriteFailure() throws IOException {
        Path directory = temporaryDirectory.resolve("not-a-file");
        Files.createDirectory(directory);

        IOException failure = assertThrows(
                IOException.class,
                () -> new ReservationFileWriter(directory).write(List.of(reservation("reservation-1"))));

        assertTrue(failure.getMessage() != null && !failure.getMessage().isBlank());
        assertTrue(Files.isDirectory(directory));
        try (var files = Files.list(temporaryDirectory)) {
            assertTrue(files.noneMatch(path -> path.getFileName().toString()
                    .startsWith("not-a-file.")));
        }
    }

    @Test
    void rejectsNullReservations() {
        Path file = temporaryDirectory.resolve("reservations.json");

        assertThrows(
                IllegalArgumentException.class,
                () -> new ReservationFileWriter(file)
                        .write(java.util.Collections.singletonList((Reservation) null)));
    }

    @Test
    void roundTripPreservesAllReservationFieldsIncludingHistoricalDate() throws IOException {
        Path file = temporaryDirectory.resolve("round-trip.json");
        Reservation original = new Reservation(
                "historical-reservation",
                "study-room-a",
                "local-user",
                LocalDate.of(2020, 10, 1),
                LocalTime.of(9, 30),
                LocalTime.of(11, 15));

        new ReservationFileWriter(file).write(List.of(original));

        ReservationStore loadedStore = new ReservationFileReader(file).read();
        Reservation loaded = loadedStore.getReservation("historical-reservation");

        assertEquals("historical-reservation", loaded.getReservationId());
        assertEquals("study-room-a", loaded.getSpaceId());
        assertEquals("local-user", loaded.getOwnerId());
        assertEquals(LocalDate.of(2020, 10, 1), loaded.getDate());
        assertEquals(LocalTime.of(9, 30), loaded.getStartTime());
        assertEquals(LocalTime.of(11, 15), loaded.getEndTime());
    }

    private static Reservation reservation(String reservationId) {
        return new Reservation(
                reservationId,
                "study-room-a",
                "local-user",
                LocalDate.of(2026, 10, 1),
                LocalTime.of(10, 15),
                LocalTime.of(11, 45));
    }
}
