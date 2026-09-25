package persistence;

import model.Reservation;
import model.ReservationStore;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;

/**
 * Writes the complete reservation snapshot to a JSON file.
 */
public final class ReservationFileWriter {

    private final Path filePath;

    public ReservationFileWriter(Path filePath) {
        this.filePath = Objects.requireNonNull(filePath, "filePath must not be null");
    }

    public Path getFilePath() {
        return filePath;
    }

    public void write(ReservationStore reservationStore) throws IOException {
        Objects.requireNonNull(reservationStore, "reservationStore must not be null");
        write(reservationStore.exportSnapshot());
    }

    public void write(Collection<Reservation> reservations) throws IOException {
        Objects.requireNonNull(reservations, "reservations must not be null");

        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        for (Reservation reservation : reservations) {
            if (reservation == null) {
                throw new IllegalArgumentException(
                        "reservations must not contain null reservations");
            }
            if (!first) {
                json.append(',');
            }
            appendReservation(json, reservation);
            first = false;
        }
        json.append(']').append(System.lineSeparator());

        Path parent = filePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(
                filePath,
                json,
                StandardCharsets.UTF_8);
    }

    private static void appendReservation(StringBuilder json, Reservation reservation) {
        json.append('{')
                .append("\"reservationId\":");
        appendJsonString(json, reservation.getReservationId());
        json.append(",\"spaceId\":");
        appendJsonString(json, reservation.getSpaceId());
        json.append(",\"ownerId\":");
        appendJsonString(json, reservation.getOwnerId());
        json.append(",\"date\":");
        appendJsonString(json, reservation.getDate().toString());
        json.append(",\"startTime\":");
        appendJsonString(json, reservation.getStartTime().toString());
        json.append(",\"endTime\":");
        appendJsonString(json, reservation.getEndTime().toString());
        json.append('}');
    }

    private static void appendJsonString(StringBuilder json, String value) {
        json.append('"');
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '"' -> json.append("\\\"");
                case '\\' -> json.append("\\\\");
                case '\b' -> json.append("\\b");
                case '\f' -> json.append("\\f");
                case '\n' -> json.append("\\n");
                case '\r' -> json.append("\\r");
                case '\t' -> json.append("\\t");
                default -> {
                    if (character < 0x20) {
                        json.append(String.format("\\u%04x", (int) character));
                    } else {
                        json.append(character);
                    }
                }
            }
        }
        json.append('"');
    }
}
