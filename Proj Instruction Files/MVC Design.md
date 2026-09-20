# MVC Design

**Related issue:** #6 Document minimal MVC responsibilities and component contracts
**Scope:** US-1 through US-10

This note defines the smallest package map and contracts needed for the Campus Space Reservation System. It is a design boundary for implementation, not the implementation itself.

## Package map

```text
src/main/java/
  model/
    Space
    Reservation
    ReservationStore
    ValidationResult
    ReservationError
  controller/
    SpaceController
    ReservationController
    ApplicationController
  persistence/
    ReservationRepository
    JsonReservationRepository
  view/
    Main
    SpaceListView
    SpaceDetailsView
    AvailabilityView
    ReservationFormView
    MyReservationsView

src/test/java/
  model/              model unit tests
  controller/         controller integration tests
  persistence/        repository and lifecycle tests
```

The names above are proposed implementation types. The package responsibilities and contracts are the required design; classes may be combined or renamed only if the same boundaries remain clear.

## Shared application state

The application creates one shared state during startup:

```text
Space catalog + ReservationStore + Clock + Repository
                         |
              controllers receive references
                         |
                 all views use controllers
```

There must be one `ReservationStore` for the application session. Views do not create their own reservation lists. After a successful create, update, or cancel command, the controller updates the shared store and tells the relevant views to refresh from controller query results.

The predefined space catalog is separate from saved reservations. Loading an empty reservation file must leave the shared store empty and must never reseed sample reservations.

## Model responsibilities and contracts

### `Space`

Represents one predefined reservable space.

Required data:

- Stable nonblank `spaceId`.
- Nonblank name.
- Nonblank building.
- Positive capacity.
- Features represented as a collection of strings.

The model validates its own structural data. Space administration is out of scope, so the catalog is read-only after startup.

### `Reservation`

Represents one reservation on one calendar date.

Required data:

- Stable nonblank `reservationId`.
- Existing `spaceId`.
- `ownerId`, supplied by the application as `local-user`.
- `LocalDate date`.
- `LocalTime startTime`.
- `LocalTime endTime`.

Structural validation requires nonblank identifiers, a valid date and time, and `endTime.isAfter(startTime)`. It must not reject a record only because its date/time is historical. This allows previously saved reservations to be loaded and displayed.

### `ReservationStore`

Owns the in-memory reservation collection and provides read operations. It does not perform file I/O.

Required queries:

- Reservations for a space on a selected date, sorted by start time.
- Reservations for an owner, sorted by date and start time.
- Whether a proposed interval overlaps an existing reservation for the same space and date.

Overlap uses half-open intervals: `[start, end)`. Therefore, 10:00-11:00 and 11:00-12:00 are adjacent and do not overlap. A reservation must not conflict with itself during an update.

### `ValidationResult` and errors

Validation and command operations must return an explicit result rather than relying on UI text or an uninformative boolean. A result should communicate success or failure and include a stable error category and user-facing message.

Minimum error categories:

- `MISSING_REQUIRED_FIELD`
- `UNKNOWN_SPACE`
- `INVALID_TIME_RANGE`
- `TIME_IN_PAST`
- `RESERVATION_CONFLICT`
- `RESERVATION_NOT_FOUND`
- `NOT_RESERVATION_OWNER`
- `PERSISTENCE_ERROR`

The exact Java representation may be a result object, sealed hierarchy, or typed exception mapped by the controller. Views must not decide which business rule failed.

## Controller responsibilities and contracts

Controllers coordinate model, store, repository, clock, and views. They contain application use cases but do not render JavaFX controls or perform JSON parsing.

### `SpaceController`

Queries the catalog for:

- All spaces.
- A selected space by stable ID.
- Spaces whose capacity is greater than or equal to a requested minimum.

A missing selection or no matching spaces returns an empty result that the view can represent with a helpful message.

### `ReservationController`

Uses the shared store and an injected `Clock` to coordinate:

- Daily availability queries.
- Reservation creation.
- Local-user reservation queries.
- Reservation updates.
- Reservation cancellation.

Create and update commands must validate required fields, structural time ordering, future timing, space existence, ownership where applicable, and conflicts. The no-past rule applies to create/update commands only; it is not part of structural validation used while loading historical records.

The controller supplies `local-user` as the owner for new reservations. The UI does not implement authentication or ask the user to enter an owner identity.

For updates, the controller excludes the reservation being edited from its own conflict check, validates the proposed replacement interval, and changes the reservation only after all checks succeed.

For cancellation, the controller verifies that the reservation belongs to `local-user`, removes it from the shared store, and returns a success or typed failure result. Confirming cancellation is a view interaction; the deletion rule remains in the controller/store path.

### `ApplicationController`

Coordinates application-wide lifecycle operations:

- Load reservations once during startup.
- Expose shared controllers to views.
- Refresh dependent views after successful mutations.
- Save the current store through the repository during orderly shutdown.

## Persistence responsibilities and contracts

### `ReservationRepository`

Defines persistence without exposing JSON details to controllers or models.

- `load()` returns a collection of structurally valid `Reservation` objects.
- `save(reservations)` writes the complete current collection.
- A missing file is treated as an empty collection on first run.
- An empty collection is saved as an empty JSON array and loads as empty.
- Persistence errors are reported as `PERSISTENCE_ERROR`; data must not be silently replaced with sample reservations.

### `JsonReservationRepository`

Stores only reservation records in `data/reservations.json`, relative to the application working directory. Space catalog data is not persisted in this file.

The repository performs serialization, deserialization, and file-system operations. It does not enforce the create/update no-past rule, calculate availability, or own the in-memory collection.

## View responsibilities and refresh contract

Views contain JavaFX controls and short event handlers. Event handlers read input, call a controller command, and render the returned result. They do not calculate overlaps, compare clocks, mutate separate lists, or read/write files.

Required views:

- `SpaceListView`: displays name, building, and capacity; supports minimum-capacity filtering and selection.
- `SpaceDetailsView`: displays building, capacity, and features for the selected stable space ID; shows a helpful empty state when none is selected.
- `AvailabilityView`: requests a space/date schedule and visually distinguishes reserved blocks from available time.
- `ReservationFormView`: collects space, date, start time, and end time; displays validation or conflict messages returned by the controller.
- `MyReservationsView`: displays local-user reservations sorted by date and start time; provides edit and cancel actions.

After a successful create, update, or cancel operation, the controller or application shell triggers refreshes for the availability and local-reservations views. A failed operation leaves the shared store unchanged and displays the returned error result.

## Testability contract

- Inject `java.time.Clock` into reservation command logic. Production uses the system default zone; tests use a fixed clock.
- Inject a temporary-path `ReservationRepository` into lifecycle/controller tests.
- Build test stores and catalogs with explicit records rather than relying on current time or application-global state.
- Test structural validation separately from command validation.
- Verify that historical reservations load successfully while new or edited reservations in the past are rejected.
- Verify normal, edge, and invalid cases for overlap, adjacency, sorting, ownership, empty saves, and cancellation of the last reservation.

## Use-case flow

```text
View event
  -> controller query/command
    -> model validation + shared ReservationStore
      -> repository only for load/save lifecycle
    <- result/query data
  <- view renders result and refreshes from shared state
```

This keeps business rules in model/store/controller logic, coordination in controllers, file I/O in persistence, and presentation in views.
