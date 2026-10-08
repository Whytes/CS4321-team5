# Team 5 Reservation System

## Sprint 1 Demo

Video link: **Pending recording and upload.** (Recording and publishing is tracked in [#33](https://github.com/Whytes/CS4321-team5/issues/33); the final link will be inserted here once available.)

A JavaFX desktop application for managing campus space reservations.

**Project board:** [Sprint 1](https://github.com/users/Whytes/projects/2) tracks To Do / Doing / Done status for all issues.

The Sprint 1 scope, sample spaces, reservation rules, and persistence decisions are documented in [Story Scope](Proj%20Instruction%20Files/Story%20Scope.md).

The MVC package map and component contracts are documented in [MVC Design](Proj%20Instruction%20Files/MVC%20Design.md).

The required video agenda script, full manual checklist, and individual submission reminders are documented in [Acceptance-Test Demo and Submission Checklist](Proj%20Instruction%20Files/Acceptance%20Test%20Demo%20and%20Submission%20Checklist.md).

## Requirements

- JDK 25
- Apache Maven 3.9.16
- Internet connection for Maven dependency downloads

## Run the Program

1. Install JDK 25 and Apache Maven 3.9.16.
2. Clone this repository and open a terminal in the project folder.
3. Start the JavaFX application:

```powershell
mvn javafx:run
```

The first run may take longer because Maven downloads JavaFX and other project dependencies. The application stores reservations in `data/reservations.json` relative to the project folder.

## Run the Tests

From the project folder:

```powershell
mvn clean test
```

This runs all unit and integration tests (169 tests as of this writing) with JUnit 5 via the Surefire plugin. There is no separate integration-test command — integration tests (for example `ReservationControllerIntegrationTest`, `ReservationShellIntegrationTest`) run together with unit tests under `mvn clean test`.

On headless Linux (no display), several tests initialize JavaFX via `Platform.startup` and require a virtual display. Use `xvfb-run` as CI does:

```bash
xvfb-run -a mvn --batch-mode test
```

Both commands above have been verified from a clean checkout with JDK 25 and Maven 3.9.16.

## MVC Overview

The application follows a Model-View-Controller structure under `src/main/java/`:

- **`model/`** — `Space`, `Reservation`, and `ReservationStore` hold domain data and validation rules (for example, rejecting overlapping reservations, requiring `endTime` after `startTime`). The store owns the one shared, in-memory reservation collection for the application session.
- **`controller/`** — `SpaceController` and `ReservationController` implement the use cases (querying spaces, creating/updating/cancelling reservations). `ApplicationController` is the composition root that builds and exposes the shared store and these controllers; it does not itself load or save data. Controllers contain no JavaFX rendering and no JSON parsing.
- **`persistence/`** — `ReservationFileReader` and `ReservationFileWriter` read and write `data/reservations.json`. They are the only classes that touch the file system.
- **`view/`** — JavaFX screens (`SpaceListView`, `SpaceDetailsView`, `AvailabilityView`, `ReservationFormView`, `MyReservationsView`) render controller results and forward user actions back to controllers. Views contain no business logic.

`view.Main` owns the lifecycle: it loads reservations once at startup via `ReservationFileReader`, and writes the full store via `ReservationFileWriter` after every successful create, update, or cancellation, as well as on normal shutdown. A failed write rolls back the in-memory mutation rather than leaving the store and file out of sync.

See [MVC Design](Proj%20Instruction%20Files/MVC%20Design.md) for the original package-map proposal and contracts; some class names above reflect the as-implemented code rather than that design note.

### Key workflows

- **Browse spaces:** view the predefined catalog (name, building, capacity), filter by minimum capacity, and view a selected space's details.
- **Check availability:** pick a space and date to see existing reservations and open time blocks for that day.
- **Create a reservation:** choose a space, date, start time, and end time; the controller rejects missing fields, invalid time ranges, past start times, and conflicts with existing reservations (adjacent times are allowed).
- **Manage "my reservations":** view, edit, or cancel the local user's own reservations (there is no login; all reservations belong to a single fixed `local-user`).

## Persistence

- **Format:** JSON array of reservation records.
- **Location:** `data/reservations.json`, relative to the application's working directory.
- **First run:** if the file or its parent directory does not exist, the application starts with an empty reservation list; the predefined space catalog itself is not stored in this file.
- **Saving:** the full, current reservation list is saved after every successful create, update, or cancellation, and again on normal application shutdown; a failed write rolls back the mutation instead of leaving data inconsistent. Saving an empty list writes `[]`, and a cancelled last reservation is never replaced with sample data.
- **Errors:** unreadable or malformed data produces a user-facing error rather than silently overwriting or inventing reservations.

## Submission Notes

- Individual submission items (time logs, retrospectives) are tracked per team member and are **not** claimed complete here; see the checklist in [Acceptance-Test Demo and Submission Checklist](Proj%20Instruction%20Files/Acceptance%20Test%20Demo%20and%20Submission%20Checklist.md).
- The demo video link above will be updated once recording and publishing ([#33](https://github.com/Whytes/CS4321-team5/issues/33)) is complete.