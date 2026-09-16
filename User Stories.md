# Sprint 1 User Stories

## US-1: View All Spaces

- **Epic:** Discover Spaces
- **User story:** As a user, I want to view a list of all reservable campus spaces so that I know which rooms are available for reservation.

### Acceptance criteria

1. Given the system contains one or more spaces; When I open the application; Then I see a list of spaces with name, building, and capacity.
2. Given the system contains no spaces; When I open the application; Then I see an empty list with a helpful message.

## US-2: View Space Details

- **Epic:** Discover Spaces
- **User story:** As a user, I want to view detailed information about a specific space so that I can determine whether it meets my needs.

### Acceptance criteria

1. Given I am viewing the list of spaces; When I select a space; Then I see its building, capacity, and features.
2. Given no space is selected; When I attempt to view details; Then no details are displayed or a helpful message is shown.

## US-3: Filter by Capacity

- **Epic:** Discover Spaces
- **User story:** As a user, I want to filter spaces by capacity so that I can find rooms that fit my group size.

### Acceptance criteria

1. Given the system contains spaces with different capacities; When I filter spaces by a minimum capacity; Then only spaces with capacity greater than or equal to that value are displayed.
2. Given I have applied a capacity filter; When I clear the filter; Then all spaces are displayed again.
3. Given no spaces meet the selected capacity; When I apply the filter; Then I see an empty list with a helpful message.

## US-4: View Availability by Day

- **Epic:** View Availability
- **User story:** As a user, I want to view reservations for a space on a selected day so that I can determine when the space is available.

### Acceptance criteria

1. Given a space has reservations on a date; When I view the space for that date; Then I see all reservations sorted by start time.
2. Given a space has no reservations; When I view that date; Then the space appears fully available.

## US-5: Distinguish Reserved Times

- **Epic:** View Availability
- **User story:** As a user, I want reserved times to be visually distinguishable from available times so that I can quickly identify open slots.

### Acceptance criteria

1. Given a space has reservations; When I view the daily schedule; Then reserved times are visually distinguishable from free times.
2. Given a space has reservations; When I view the schedule; Then reserved time blocks are clearly labeled or styled differently from available times.

## US-6: Create Reservation

- **Epic:** Create Reservation
- **User story:** As a user, I want to reserve a space for a specific date and time so that I can ensure it is available when I need it.

### Acceptance criteria

1. Given the space is available; When I create a reservation with a valid start and end time; Then the reservation is saved and displayed.
2. Given an existing reservation from 10:00-11:00; When I reserve from 10:30-11:30; Then the reservation is rejected with a conflict message.
3. Given an existing reservation from 10:00-11:00; When I reserve from 11:00-12:00; Then the reservation is accepted.
4. When I attempt to reserve with an end time before the start time; Then the reservation is rejected with a validation error.
5. When I attempt to reserve a space in the past; Then the reservation is rejected with an explanatory message.
6. When I attempt to create a reservation with missing required information; Then the reservation is rejected with a validation message.

## US-7: View My Reservations

- **Epic:** Manage Reservations
- **User story:** As a user, I want to view all reservations I have made so that I can keep track of my scheduled room usage.

### Acceptance criteria

1. Given I have made one or more reservations; When I view my reservations; Then they are displayed sorted by date and start time.
2. Given I have made no reservations; When I view my reservations; Then I see an empty list with a helpful message.

## US-8: Modify Reservation

- **Epic:** Manage Reservations
- **User story:** As a user, I want to modify an existing reservation so that I can adjust my plans if needed.

### Acceptance criteria

1. Given I have an existing reservation; When I change the time to an available slot; Then the reservation is updated.
2. Given another reservation exists at the new time; When I attempt the modification; Then it is rejected with a conflict message.
3. When I attempt to modify a reservation with an end time before the start time; Then the modification is rejected with a validation error.
4. When I attempt to modify a reservation to a time in the past; Then the modification is rejected with an explanatory message.

## US-9: Cancel Reservation

- **Epic:** Manage Reservations
- **User story:** As a user, I want to cancel a reservation so that the space becomes available again.

### Acceptance criteria

1. Given I have an existing reservation; When I cancel it; Then it is removed and the time becomes available.
2. Given I attempt to cancel a reservation; When I confirm the cancellation; Then the reservation is removed.

## US-10: Persist Data

- **Epic:** Persistence
- **User story:** As a user, I want reservations to be saved between application runs so that my data is not lost when I close the application.

### Acceptance criteria

1. Given I have created reservations; When I close and reopen the application; Then all reservations are restored.
2. Given I have no reservations; When I close and reopen the application; Then the system still contains no reservations.