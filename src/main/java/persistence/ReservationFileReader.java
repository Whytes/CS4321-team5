package persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.Reservation;
import model.ReservationStore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class ReservationFileReader {

    private final Path filePath;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ReservationFileReader(Path filePath) {
        this.filePath = Objects.requireNonNull(filePath, "filePath must not be null");
    }

    public ReservationStore read() throws IOException {
        ReservationStore store = new ReservationStore();

        if (!Files.exists(filePath)) {
            return store;
        }

        JsonNode root = objectMapper.readTree(filePath.toFile());

        if (root == null || (root.isArray() && root.isEmpty())) {
            return store;
        }

        if (!root.isArray()) {
            throw new IOException("Reservation file must contain a JSON array");
        }

        ArrayList<Reservation> reservations = new ArrayList<>();
        Set<String> reservationIds = new HashSet<>();

        for (JsonNode record : root) {
            if (!record.isObject()) {
                throw new IOException("Each reservation must be a JSON object");
            }

            String reservationId = requiredText(record, "reservationId");
            String spaceId = requiredText(record, "spaceId");
            String ownerId = requiredText(record, "ownerId");
            String date = requiredText(record, "date");
            String startTime = requiredText(record, "startTime");
            String endTime = requiredText(record, "endTime");

            if (!reservationIds.add(reservationId)) {
                throw new IOException("Duplicate reservation ID: " + reservationId);
            }

            boolean knownSpace = InitialSpaceCatalog.getDefaultSpaces().stream()
                    .anyMatch(space -> space.getSpaceId().equals(spaceId));

            if (!knownSpace) {
                throw new IOException("Unknown space ID: " + spaceId);
            }

            try {
                reservations.add(new Reservation(
                        reservationId,
                        spaceId,
                        ownerId,
                        LocalDate.parse(date),
                        LocalTime.parse(startTime),
                        LocalTime.parse(endTime)));
            } catch (DateTimeException | IllegalArgumentException exception) {
                throw new IOException("Invalid reservation: " + reservationId, exception);
            }
        }

        store.loadSnapshot(reservations);
        return store;
    }

    private static String requiredText(JsonNode record, String fieldName) throws IOException {
        JsonNode value = record.get(fieldName);

        if (value == null || !value.isTextual() || value.asText().isBlank()) {
            throw new IOException("Reservation record is missing a valid " + fieldName);
        }

        return value.asText();
    }
}