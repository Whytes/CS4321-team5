# Manual Acceptance Test Results

Issue: #30
Test date: 2026-10-05
Tested revision: `0530dfd`
Environment: Windows; JDK 25; Maven 3.9.16; JavaFX 21.0.6; local JavaFX runtime with `mvn test`

## Execution notes

The functional checks were executed through the JavaFX integration harness, which constructs the real `Main` shell and drives the production controls, views, controllers, and shared store. Controlled fixtures used `2026-10-01` for deterministic reservations and a fixed clock at `2026-09-30T12:00:00Z`; the live shell checks used dates relative to 2026-10-05. Empty states, reserved/free schedule styling, confirmation behavior, editing, and restart persistence were checked at the view/controller level. The complete Maven suite passed: 169 tests, 0 failures, 0 errors, 0 skipped.

| ID | Setup data | Steps | Expected result | Actual result | Result |
|---|---|---|---|---|---|
| US-1 AT1 | Default catalog | Open the application. | Spaces show name, building, and capacity. | Default catalog opened in Browse spaces and rendered the catalog records with those fields. | PASS |
| US-1 AT2 | Empty `SpaceListView` catalog | Open Browse spaces with no spaces. | Empty list shows a helpful message. | Empty list showed `No space meets requested minimum capacity.` | PASS |
| US-2 AT1 | Select Study Room A | Select a space in Browse spaces. | Building, capacity, and features appear. | Details panel rendered the selected name, building, capacity, and feature list. | PASS |
| US-2 AT2 | No selected space | Clear the selection and view details. | No details or a helpful message appears. | Details panel showed `Select a space to view its details.` | PASS |
| US-3 AT1 | Catalog with capacities 8, 12, and 40 | Enter minimum capacity `12` and apply. | Only spaces with capacity at least 12 remain. | Filter returned only records meeting the threshold. | PASS |
| US-3 AT2 | Applied capacity filter | Click Clear. | Full catalog is restored. | Clear removed the input and restored all spaces. | PASS |
| US-3 AT3 | Minimum capacity `999` | Apply the filter. | Empty list shows a helpful message. | Empty result showed the capacity-filter empty message. | PASS |
| US-4 AT1 | Study Room A; reservations 10:00-11:00 and 14:00-15:00 on `2026-10-01` | Open Daily availability and select the room/date. | Reservations appear sorted by start time. | Schedule returned the complete day in chronological order, including both reservations. | PASS |
| US-4 AT2 | Study Room A with no reservation on `2026-10-02` | Select the room/date. | Space appears fully available. | Schedule contained one available interval spanning the day. | PASS |
| US-5 AT1 | Reservation 10:00-11:00 | Open the daily schedule. | Reserved and free times are visually distinct. | Reserved and available cells received different style classes. | PASS |
| US-5 AT2 | Same schedule | Inspect schedule labels/styles. | Reserved blocks are clearly labeled or styled differently. | Cells displayed Reserved/Available labels and distinct reserved/free styles. | PASS |
| US-6 AT1 | Available Study Room A; `2026-10-01`, 10:00-11:00 | Submit a valid reservation. | Reservation saves and is displayed. | Reservation was saved, success feedback appeared, and the schedule selected the reserved block. | PASS |
| US-6 AT2 | Existing 10:00-11:00 reservation | Submit 10:30-11:30. | Submission is rejected with a conflict message. | Submission was rejected with `The selected space is already reserved for that time`; inputs remained. | PASS |
| US-6 AT3 | Existing 10:00-11:00 reservation | Submit 11:00-12:00. | Adjacent reservation is accepted. | Reservation was accepted and displayed. | PASS |
| US-6 AT4 | Any space/date | Submit end 09:00 or equal end 10:00 for start 10:00. | Validation error; no reservation is saved. | `End time must be after start time`; shared state was unchanged. | PASS |
| US-6 AT5 | Any space; date before 2026-10-05 | Submit 10:00-11:00. | Past reservation is rejected with an explanatory message. | `Reservation start must be in the future`; entered date remained available for correction. | PASS |
| US-6 AT6 | Valid form with one required field removed at a time | Submit with missing space, date, start, or end. | Validation message; no save. | `Space, date, start time, and end time are required`; remaining inputs were preserved. | PASS |
| US-7 AT1 | Two local reservations on different dates/times plus one other-user reservation | Open My reservations. | Local reservations show sorted by date and start time. | Local records were shown in sorted order; the other-user record was excluded. | PASS |
| US-7 AT2 | No local reservations | Open My reservations. | Helpful empty state appears. | List showed `You have no reservations.` | PASS |
| US-8 AT1 | Existing reservation; desired slot is free | Select the reservation, choose Edit, and change its time to an available slot. | Reservation is updated. | Edit form opened, the update succeeded, and My reservations refreshed with the new interval. | PASS |
| US-8 AT2 | Existing reservation plus conflicting reservation | Edit into the conflicting interval. | Modification is rejected with a conflict message. | Update was rejected with the conflict message and the original reservation remained. | PASS |
| US-8 AT3 | Existing reservation | Edit with end before start. | Validation error; original reservation remains. | `End time must be after start time`; original reservation remained. | PASS |
| US-8 AT4 | Existing reservation; past target time | Edit into the past. | Explanatory rejection; original reservation remains. | `Reservation start must be in the future`; original reservation remained. | PASS |
| US-9 AT1 | Existing local reservation | Cancel it and inspect its schedule. | Reservation is removed and time becomes available. | Cancellation removed the record and refreshed the schedule to Available. | PASS |
| US-9 AT2 | Existing local reservation | Select Cancel reservation, then confirm. | Confirmation removes the reservation. | Manually verified 2026-10-08 against the running JavaFX application: selecting Cancel reservation showed the confirmation dialog (title "Cancel reservation", header "Confirm cancellation"); clicking OK removed the reservation from My reservations. | PASS |
| US-10 AT1 | Create a reservation, close, and reopen the application | Inspect reservations after restart. | All saved reservations are restored. | Reservation file was written on save/stop and the reopened application restored the reservation. | PASS |
| US-10 AT2 | No reservations; close and reopen | Inspect reservations after restart. | No reservations remain. | Empty snapshot remained empty after close and reopen. | PASS |

## Defect follow-up

No acceptance failures were observed at revision `0530dfd`; no defect issues were created from this run. The checklist should be rerun if the edit or persistence lifecycle changes.