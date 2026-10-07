# Team 5 Reservation System

## Sprint 1 Demo

Video link: **Pending recording and upload.**

A JavaFX desktop application for managing campus space reservations.

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