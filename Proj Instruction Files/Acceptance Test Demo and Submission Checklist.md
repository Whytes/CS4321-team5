# Acceptance-Test Demo and Submission Checklist

This document follows the instructor-provided **Video Demo Requirements** agenda and is based on [User Stories](User%20Stories.md) and [Story Scope](Story%20Scope.md). It separates the required video agenda and story/demo subset from the full manual system-test checklist.

## Recording requirements

- Follow the seven agenda topics below in the exact order shown. Target the suggested time for each topic (14–27 minutes total).
- Narrate throughout; one speaker or multiple speakers are fine. Speak clearly and loudly, increase the font or zoom where possible, and avoid rushing or scrolling too quickly.
- Record the screen using Kaltura Capture or other video-recording software (not a phone). The camera may be off; make sure the audio is on.
- Host on Kaltura, OneDrive, YouTube, or another permitted platform. Test playback and audio, set instructor viewing permissions, and put the video link at the very top of the README under **Sprint 1 Demo**.

Also ask the instructor to resolve the time-log activity-code discrepancy: Team Instructions says to use code `O` for all meetings, while Development Requirements lists `A/I/V/R/M/P` and `M` for meetings. Do not assume these codes are interchangeable.

## Repeatable demo setup

1. Use the latest integrated, runnable application and record its revision/build and test date. The demo must show the working UI, not just test output.
2. Run from a clean, isolated copy or working directory so test bookings cannot overwrite personal data. Back up any existing `data/reservations.json`; use an empty reservation file (`[]`) or the documented first-run empty state. The planned file path is relative to the application's working directory.
3. Choose `D` as a future date at least one day after recording and `E` as the following day. Enter dates using the application's date picker/format. All listed times are local.
4. Use the predefined **Study Room A** (Magnolia Building, capacity 4, Table and whiteboard). Create these reservations in this order:

   | Order | Date | Start | End | Purpose |
   | ---: | --- | --- | --- | --- |
   | 1 | E | 09:00 | 10:00 | Prove date sorting |
   | 2 | D | 13:00 | 14:00 | Prove time sorting; later edit/cancel |
   | 3 | D | 10:00 | 11:00 | Prove schedule sorting; conflict/adjacency |

   The order is deliberate: the reservation list and D's schedule should sort chronologically, not by creation order.
5. When recording persistence, close the application normally and reopen it from the same isolated working directory. At the end, restore the backed-up data or discard the isolated copy.

## Required video agenda and script

Follow these topics in order. Fill in the actual PR and test method selected for the recording; do not claim a feature is demonstrated if it is not integrated and working in the recorded revision.

| Order / topic | Time | Narration and on-screen actions |
| --- | ---: | --- |
| 1. Project Board | 1–2 min | Open the team's GitHub Project board. Explain the To Do / Doing / Done workflow, how tasks are tracked, and how work is distributed. |
| 2. Pull Request | 2–3 min | Show one representative PR and its included commit(s). Briefly explain the change, linked issue, and review/merge status. Prefer a merged PR from the revision being demonstrated. |
| 3. MVC | 2–4 min | Show one feature's view, controller, and model/service code. Trace a user action from the view to controller/business logic and back to the displayed result; explain the separation of responsibilities. |
| 4. Persistence | 2–4 min | Show the persistence implementation and trace a reservation being saved and loaded. If demonstrating restore behavior, show the same reservation after restarting the application. **Recording gate:** in the current working tree, the application startup constructs a fresh in-memory controller and no reader/writer is wired into that lifecycle. Do not claim working persistence unless the recorded revision implements and demonstrates save/restore end to end; coordinate completion before recording this agenda section. |
| 5. Unit Testing | 1–2 min | Show and explain one focused unit test, its setup, the behavior under test, and the assertions. |
| 6. Acceptance Testing | 1–2 min | Show and explain one test method that implements a named acceptance criterion. Identify its US/AT ID, arrange the given state, perform the action, and point out how assertions establish the expected result. Suggested example: `ReservationCreationServiceTest.createsReservationAndAddsExactlyOneReservation` for **US-6 AT1**. |
| 7. Demo — read a story, demo, repeat | 5–10 min | For each selected story, read or briefly state the user story and its US/AT IDs, perform the matching UI steps, and show the result before moving to the next story. Use the repeatable setup below. |

### Suggested story/demo subset for agenda item 7

This is a concise selection for the agenda's **5–10 minute** demo portion; adjust selection and pacing to the finished application. State each US/AT ID as the story is read.

| Story / acceptance tests | User steps | Expected result |
| --- | --- | --- |
| **US-1 AT1, US-2 AT1** — view spaces and details | Open Browse spaces; point out Study Room A's name, building, and capacity; select it. | The list shows required summary fields; details show the building, capacity, and features. |
| **US-3 AT1–2** — filter by capacity | Set minimum capacity to 20, then clear the filter. | Only Conference Room (20) and Theater (250) remain; clearing restores all five spaces. |
| **US-4 AT1, US-5 AT1–2** — daily availability | Open Daily availability; choose Study Room A and D. | Reservations display in start-time order (10:00–11:00 before 13:00–14:00); reserved intervals are clearly distinguished from free intervals. |
| **US-6 AT1–3** — create and validate | Create Study Room A / D / 15:00–16:00 and show it saved; try 10:30–11:30; then create 11:00–12:00. | The valid booking is saved; overlap with 10:00–11:00 is rejected with a conflict message; exact adjacency is accepted. No rejected attempt is saved. |
| **US-7 AT1, US-8 AT1, US-9 AT1–2** — manage reservations | Open My reservations; inspect order; edit the 13:00–14:00 booking to 14:00–15:00; cancel another booking and confirm. | The list is date/start-time sorted; the edit is shown; cancellation requires confirmation and removes the booking, releasing its interval. |
| **US-10 AT1** — persist reservations | Close and reopen normally; revisit My reservations. | Remaining bookings are restored after restart. |

## Full manual system-test checklist

Run all 27 acceptance tests manually against the integrated application, independently of the video subset. Use a clean isolated data file for each scenario or reset state between tests. For each row, record **Pass/Fail**, brief observed result, and evidence location (screenshot or video timestamp); include the app revision and environment with the results.

| ID | Manual action / setup | Expected result | Result / evidence |
| --- | --- | --- | --- |
| **US-1 AT1** | Start with one or more catalog spaces and open the application. | The list shows each space's name, building, and capacity. | |
| **US-1 AT2** | Start with an empty space catalog, if the application supports that test fixture, and open the application. | An empty list and helpful message are shown. | |
| **US-2 AT1** | Select a listed space. | Its building, capacity, and features are displayed. | |
| **US-2 AT2** | Clear the space selection or open details with no selection. | No stale details are shown, or a helpful message is shown. | |
| **US-3 AT1** | With the five-space catalog, filter by minimum capacity 20. | Only Conference Room (20) and Theater (250) are displayed. | |
| **US-3 AT2** | Clear the minimum-capacity filter. | All five spaces are displayed again. | |
| **US-3 AT3** | Filter by a value above the largest catalog capacity (for example, 251). | An empty list and helpful message are shown. | |
| **US-4 AT1** | For Study Room A on D, create 13:00–14:00 before 10:00–11:00, then view D. | Both reservations are displayed in start-time order. | |
| **US-4 AT2** | Select a catalog space and date with no reservations. | The space appears fully available. | |
| **US-5 AT1** | View a daily schedule containing at least one reservation. | Reserved times are visually distinguishable from free times. | |
| **US-5 AT2** | Inspect the same schedule's reserved and free intervals. | Reserved blocks are clearly labeled or styled differently from available blocks. | |
| **US-6 AT1** | Create a reservation for an available space on a future date with valid start/end times. | It is saved and displayed. | |
| **US-6 AT2** | With an existing 10:00–11:00 reservation, submit 10:30–11:30 for the same space/date. | The new reservation is rejected with a conflict message and is not saved. | |
| **US-6 AT3** | With an existing 10:00–11:00 reservation, submit 11:00–12:00 for the same space/date. | The adjacent reservation is accepted. | |
| **US-6 AT4** | Submit a reservation whose end time is before its start time. | It is rejected with a validation error. | |
| **US-6 AT5** | Submit a reservation starting in the past (for example, yesterday). | It is rejected with an explanatory message. | |
| **US-6 AT6** | Submit with each required field missing in turn: space, date, start time, and end time. | Each incomplete reservation is rejected with a validation message and is not saved. | |
| **US-7 AT1** | Create reservations on D and E, in an order different from date/start-time order; open My reservations. | All reservations are displayed sorted by date and start time. | |
| **US-7 AT2** | Start with no reservations and open My reservations. | An empty list and helpful message are shown. | |
| **US-8 AT1** | Edit an existing reservation to an available time. | The reservation is updated to the new time. | |
| **US-8 AT2** | Edit a reservation into a time occupied by another reservation in the same space. | The edit is rejected with a conflict message; the original reservation remains unchanged. | |
| **US-8 AT3** | Edit a reservation so its end time is before its start time. | The edit is rejected with a validation error; the original remains unchanged. | |
| **US-8 AT4** | Edit a reservation to start in the past. | The edit is rejected with an explanatory message; the original remains unchanged. | |
| **US-9 AT1** | Cancel an existing reservation and inspect that space/date's schedule. | The reservation is removed and its time becomes available. | |
| **US-9 AT2** | Select a reservation, start cancellation, then confirm it. | The confirmation is shown; after confirmation the reservation is removed. | |
| **US-10 AT1** | Create one or more reservations, close normally, and reopen from the same working directory. | Saved reservations are restored. | |
| **US-10 AT2** | Start with no reservations, close normally, and reopen from the same working directory. | The system still has no reservations. | |

## Submission checklist

- [ ] Each team member separately completes and submits their own time log using the required spreadsheet and meaningful activity descriptions.
- [ ] Each team member separately completes and submits their own retrospective using the provided template; submit as Word or PDF on Blazeview.
- [ ] Do not treat closed issues #34 (time logs) or #35 (retrospectives) as evidence that any individual's submission is complete.
- [ ] Ask the instructor which activity code to use for meetings (`O` in Team Instructions versus `M` in Development Requirements); apply the confirmed code consistently to each member's own log.
- [ ] Follow all seven agenda sections above in the exact order and stay within the suggested topic times (14–27 minutes total).
- [ ] Record the selected stories with their US/AT IDs, narrate throughout, show the actions and outcomes, and link the finished video at the top of the README under **Sprint 1 Demo**.
- [ ] Verify that the instructor can access the video, especially if it is hosted on a restricted platform.
- [ ] Test playback and confirm the recording's audio is clear and audible.
- [ ] Retain the completed full manual checklist and its test revision/environment/evidence as system-testing records.
