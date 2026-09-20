# Story Scope

**Status:** Sprint 1 story scope
**Related stories:** US-1 through US-10

## Purpose

This document records the domain choices needed to implement the supplied user stories. It separates requirements stated by the stories from implementation choices made for this stand-alone sprint.

## Explicit story requirements

The application must support:

- Viewing all reservable spaces with name, building, and capacity.
- Viewing a selected space's building, capacity, and features.
- Filtering spaces by minimum capacity and clearing that filter.
- Viewing one space's reservations for a selected day, sorted by start time.
- Distinguishing reserved time blocks from available time.
- Creating reservations for an available space and time.
- Rejecting overlapping reservations while accepting adjacent reservations.
- Rejecting an end time before the start time; equal start and end handling is defined as a team choice below.
- Rejecting reservations in the past, using the interpretation recorded below.
- Rejecting reservations with missing required information.
- Viewing the local user's reservations sorted by date and start time.
- Modifying the local user's reservation when the new time is valid and available.
- Rejecting conflicting, invalid, or past modifications.
- Cancelling the local user's reservations.
- Saving reservations between application runs and restoring them at startup.
- Preserving an empty reservation collection after an empty save or after cancelling the last reservation.

## Fixed sample space catalog

Sample spaces are application data, not saved reservations. Their stable identifiers must not change between runs.

### Study Room A

- **ID:** `study-room-a`
- **Building:** Magnolia Building
- **Capacity:** 4 people
- **Features:** Table, whiteboard

### Study Room B

- **ID:** `study-room-b`
- **Building:** Magnolia Building
- **Capacity:** 6 people
- **Features:** Whiteboard, projector

### Conference Room

- **ID:** `conference-room`
- **Building:** Student Center
- **Capacity:** 20 people
- **Features:** Conference table, projector, internet

### Theater

- **ID:** `theater`
- **Building:** University Center
- **Capacity:** 250 people
- **Features:** Projector, screen, sound system

### Science Lab

- **ID:** `science-lab`
- **Building:** Science Building
- **Capacity:** 10 people
- **Features:** Blackboard, table, science equipment

The catalog is predefined for Sprint 1. Room administration, including adding, editing, or deleting spaces, is out of scope.

## Local user and reservation ownership

This is a stand-alone sprint with no authentication or accounts. The application uses one fixed local user:

- **User ID:** `local-user`
- **Display name:** Local User

“My reservations” means reservations whose `ownerId` is `local-user`. The owner ID is stored with each reservation so ownership rules are explicit, but the application does not implement login or multiple-user identity management.

The required reservation inputs are space, date, start time, and end time. The application supplies `ownerId` as `local-user`; the user is not asked to enter owner information.

## Date and time rules

- A reservation covers exactly one calendar date.
- The date is represented by `LocalDate`.
- Start and end times are represented by `LocalTime` on that date.
- The team interpretation of a valid reservation is positive duration: `endTime` must be after `startTime`. This makes equal start and end times invalid without adding a minimum-duration rule.
- Reservations that meet exactly at a boundary are allowed; for example, 10:00-11:00 and 11:00-12:00 do not conflict.
- Overlap checks apply to both creation and modification.
- The schedule displays the selected date only. It does not support multi-day, recurring, or cross-midnight reservations.
- Time input is minute precision. No minimum duration is imposed because the stories do not require one.
- The team interpretation of “reserve a space in the past” is that the reservation's start must be in the future relative to the application's clock. A reservation ending in the future but starting in the past is also rejected.
- The application uses the host system's default time zone and current clock. Business logic should receive a clock where practical so tests can use a fixed time.
- No operating hours, fixed booking increments, or displayed booking window are imposed by Sprint 1.

## Persistence decision

- Format: JSON.
- Location: `data/reservations.json`, relative to the application working directory.
- The file contains saved reservations only. The predefined space catalog is loaded from code and is never written as reservations.
- On first run, if the file or its parent directory does not exist, the application starts with an empty reservation collection.
- Saving an empty collection writes an empty JSON array, such as `[]`.
- Loading an empty array leaves the reservation collection empty.
- Cancelling the last reservation does not restore sample reservations.
- Malformed or unreadable data must produce a clear user-facing error and must not silently seed or invent reservations. The exact recovery behavior is an open question below.

## In scope for Sprint 1

- Predefined space catalog.
- Space discovery, details, and minimum-capacity filtering.
- Single-day availability display.
- Create, view, modify, and cancel reservations for the local user.
- Conflict and structural validation described above.
- JSON save/load across application runs.
- JavaFX presentation using MVC and focused JUnit tests for business logic.

## Out of scope

- Authentication, login, accounts, or multiple user identities.
- Room administration.
- Multi-day or cross-midnight reservations.
- Recurring reservations.
- Operating-hour restrictions.
- Email notifications.
- Database storage.
- A minimum reservation duration such as 30 minutes.
- Any business rule not required by the supplied user stories or explicitly recorded in this scope agreement.

## Implementation questions

These do not block recording the proposed scope, but they should be confirmed before implementation is considered final:

1. Is the proposed five-space catalog acceptable, or should the team change names, buildings, capacities, or features?
2. Does the instructor accept the fixed `local-user` interpretation for ownership without a login screen?
3. Is the system-default time zone acceptable for the demonstration environment?
4. Is rejecting equal start and end times acceptable as structural validation?
5. What user-facing recovery message and behavior should be used for malformed JSON?
6. Does the instructor require a specific time precision or schedule display range?

## Summary

Sprint 1 implements a single-date reservation system for a predefined catalog and one local user. It does not implement authentication, room administration, multi-day support, operating hours, or a minimum reservation duration.
