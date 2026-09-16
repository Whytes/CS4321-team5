# Project Planning Handoff

## Reviewed plan

This handoff replaces the original planning snapshot after comparing all local Markdown instructions, all ten user stories (27 acceptance tests), every tracked starter source file, and the live GitHub issues/project. The Markdown story document is the acceptance-test reference used below; the spreadsheet was not independently compared.

- Repository: https://github.com/Whytes/CS4321-team5
- Project: https://github.com/users/Whytes/projects/2
- View: **Sprint 1**, **Board** layout
- Status values: **To Do**, **Doing**, **Done**
- Task fields: **Order**, **Estimate (hours)** and **Phase**; issue bodies contain prerequisites, requirement references, deliverables, and completion rules.
- Reviewed final inventory: **37 board items: 35 open / To Do and 2 completed / Done (#31, #32)**.
- All future work remains unassigned for team selection. Claim an issue before moving it to Doing; distribute meaningful work across the team.
- Remaining estimates total **79 task-hours**, before newly discovered defects. These are initial planning estimates, not a team-approved sprint capacity or a delivery promise. Re-estimate together; each task should remain between 0.5 and 4 hours.

## What the review corrected

1. **Board setup was not actually finished.** The project had a table named `View 1`, with `Todo` and `In Progress` status names, even though #31 was closed. The view is now a board named `Sprint 1`, and statuses match the assignment. #31 is retained on the board as completed setup evidence.
2. **The original issues had no estimates or prerequisites.** Every current task now has an estimate, a phase, explicit dependencies, and checkable deliverables. Literal `\n` text in issue descriptions was replaced with real Markdown formatting.
3. **Some tasks mixed too many responsibilities.** #10 now owns shared storage/read queries; #17/#20/#21 own create/update/cancel rules. Capacity controls (#38), edit UI (#39), and cancellation confirmation (#40) are distinct from business commands. These changes bound tasks rather than merely renaming entire stories.
4. **Application assembly was missing.** #37 explicitly owns the main window, navigation, shared-state construction, and view attachment/refresh contracts.
5. **Several assumptions looked like requirements.** Login/accounts, mandatory owner entry, operating-hour restrictions, fixed booking increments, and multi-day support are not required by the supplied stories. #5 records team choices without silently expanding scope. A single local user is a recommendation awaiting team confirmation.
6. **Persistence responsibilities overlapped.** #22 writes, #23 reads, and #24 wires startup/shutdown. Historical reservations may reload even though new/edited reservations in the past must be rejected. Empty saves must stay empty; cancelling the last record must not resurrect sample data.
7. **Testing was duplicated and too broad.** Implementers own initial focused tests. #25–#28 audit and fill missing coverage instead of recreating the same tests. Controller integration testing is split between #29 and #41. #30 explicitly covers all 27 manual acceptance tests; only the video uses a selected subset.
8. **Planning and submission tasks lacked a completion point.** #32 is this finite review, not a permanent reminder to use GitHub. #42 prepares the demo script and submission checklist; #33 records/publishes the video; #36 owns setup/usage documentation.

## Actual implementation baseline

- `model/Reservation.java` only prints `Sprint project started`; it is not a reservation domain model.
- `controller/` and `persistence/` contain only `.gitkeep` files.
- `view` is an extensionless file containing another starter class, not a Java package directory. `view2/` is a placeholder. #4 must inspect and migrate these into the agreed build structure.
- There is no JavaFX build configuration or JavaFX application entry point, test tree, reservation logic, or persistence implementation yet.
- `README.md` contains only the project title/description.
- Local Git status already showed deleted root `User Stories.md` / `User Stories.xlsx` and an untracked `Proj Instruction Files/` directory. Those existing local changes were preserved. The review changes GitHub planning and this handoff; it does not implement application features.

## Corrected task inventory

Dependencies are maintained in the live issue bodies. Phase is a sequencing aid; independent tasks can proceed in parallel after their prerequisites are satisfied.

### Foundation and finite planning

| Issue | Task | Hours |
| --- | --- | ---: |
| #4 | Set up runnable JavaFX build and JUnit source structure | 3 |
| #5 | Agree on story scope, sample spaces, and reservation rules | 2 |
| #6 | Document minimal MVC responsibilities and component contracts | 2 |
| #7 | Create Space model with validation | 2 |
| #8 | Create Reservation model with structural validation | 2 |
| #11 | Provide initial campus space catalog | 1 |
| #37 | Build application shell, navigation, and shared-state wiring | 3 |
| #31 | Configure and verify Sprint 1 board — completed | 0.5 |
| #32 | Review and correct Sprint 1 issue scopes and coverage — completed | 2 |

### Business logic, discovery, and schedule

| Issue | Task | Hours |
| --- | --- | ---: |
| #9 | Implement and unit-test reservation overlap detection | 2 |
| #10 | Implement shared in-memory reservation store and read queries | 3 |
| #12 | Build and wire space list view | 2 |
| #13 | Build and wire selected-space details view | 1.5 |
| #14 | Implement minimum-capacity query and controller operation | 2 |
| #38 | Add capacity filter controls and wire list refresh | 1.5 |
| #15 | Implement daily-availability controller query | 1.5 |
| #16 | Build and wire daily availability schedule | 3 |
| #17 | Implement create-reservation service and controller command | 3 |
| #20 | Implement update-reservation service and controller command | 3 |
| #21 | Implement cancellation service and controller command | 1.5 |

### Reservation UI and persistence

| Issue | Task | Hours |
| --- | --- | ---: |
| #18 | Build and wire create-reservation form | 3 |
| #19 | Build and wire my-reservations list | 2 |
| #39 | Build and wire reservation editing UI | 2 |
| #40 | Add cancellation confirmation and wire view refresh | 1.5 |
| #22 | Implement reservation file writer | 2 |
| #23 | Implement reservation file reader | 2.5 |
| #24 | Wire startup loading and application save lifecycle | 2.5 |

### Verification and submission

| Issue | Task | Hours |
| --- | --- | ---: |
| #25 | Complete Space and capacity-query unit coverage | 1.5 |
| #26 | Complete reservation validation and overlap unit coverage | 2 |
| #27 | Complete reservation query and mutation unit coverage | 2.5 |
| #28 | Complete persistence and lifecycle unit coverage | 2 |
| #29 | Add discovery and creation controller integration tests | 3 |
| #41 | Add management and restart controller integration tests | 3 |
| #30 | Execute and record all 27 manual acceptance tests | 4 |
| #42 | Prepare acceptance-test demo script and submission checklist | 1.5 |
| #33 | Record and publish acceptance-test video demonstration | 3 |
| #36 | Document reproducible setup, tests, usage, and persistence | 2 |

## Acceptance-test traceability

`AT` is the numbered acceptance criterion under each story in [User Stories.md](User%20Stories.md). Every row also receives manual verification in #30. Controller integration tests cover business outcomes; they do not substitute for checking UI styling, messages, or confirmation dialogs.

| Story / acceptance tests | Implementation owners | Automated verification |
| --- | --- | --- |
| US-1 AT1–2: populated/empty startup list | #7, #11, #12, #37 | Logic unit tests; coverage audit #25 |
| US-2 AT1–2: selected details/no selection | #12, #13 | Unit tests for any selection logic; presentation checked manually |
| US-3 AT1–3: minimum capacity, clear, no matches | #14, #38 | Implementation unit tests, #25, #29 |
| US-4 AT1–2: selected space/day sorted reservations, fully available empty day | #10, #15, #16 | Implementation unit tests, #27, #29 |
| US-5 AT1–2: reserved/free distinction and clear labels/style | #16 | Logic tests if interval calculations are introduced; visual acceptance checked manually |
| US-6 AT1–6: create/display, conflict, adjacency, invalid range, past, missing fields | #8, #9, #10, #17, #18 | Implementation unit tests, #26, #27, #29 |
| US-7 AT1–2: date/start sorted personal list, empty state | #10, #19 | Implementation unit tests, #27, #41 |
| US-8 AT1–4: valid edit, conflicting edit, invalid range, past | #9, #20, #39 | Implementation unit tests, #26, #27, #41 |
| US-9 AT1–2: removal/releases time, confirmation | #21, #40 | Command unit tests, #27, #41 for removal/released time; dialog tested manually |
| US-10 AT1–2: restore saved reservations, restore zero reservations | #22, #23, #24 | Implementation unit tests, #28, #41 with isolated temporary files/fresh state |

## Numbered execution order

The 35 remaining tasks are physically arranged in this order on the board. Each title starts with its sequence number, and the **Order** field matches. GitHub issue IDs are permanent: task **1** is issue **#4**, not a renumbered GitHub issue. Completed setup/review issues #31/#32 are outside this remaining-work sequence.

Every prerequisite comes before its dependent task. This is a convenient start-to-finish path; team members may still work in parallel when the dependencies in their issue are satisfied. Initial tests belong in each implementation task, even though coverage audits appear later.

| Order | GitHub issue | Task |
| ---: | --- | --- |
| 1 | #4 | Set up runnable JavaFX build and JUnit source structure |
| 2 | #5 | Agree on story scope, sample spaces, and reservation rules |
| 3 | #6 | Document minimal MVC responsibilities and component contracts |
| 4 | #7 | Create Space model with validation |
| 5 | #8 | Create Reservation model with structural validation |
| 6 | #11 | Provide initial campus space catalog |
| 7 | #37 | Build application shell, navigation, and shared-state wiring |
| 8 | #12 | Build and wire space list view |
| 9 | #13 | Build and wire selected-space details view |
| 10 | #14 | Implement minimum-capacity query and controller operation |
| 11 | #38 | Add capacity filter controls and wire list refresh |
| 12 | #10 | Implement shared in-memory reservation store and read queries |
| 13 | #9 | Implement and unit-test reservation overlap detection |
| 14 | #15 | Implement daily-availability controller query |
| 15 | #16 | Build and wire daily availability schedule |
| 16 | #17 | Implement create-reservation service and controller command |
| 17 | #18 | Build and wire create-reservation form |
| 18 | #19 | Build and wire my-reservations list |
| 19 | #20 | Implement update-reservation service and controller command |
| 20 | #39 | Build and wire reservation editing UI |
| 21 | #21 | Implement cancellation service and controller command |
| 22 | #40 | Add cancellation confirmation and wire view refresh |
| 23 | #22 | Implement reservation file writer |
| 24 | #23 | Implement reservation file reader |
| 25 | #24 | Wire startup loading and application save lifecycle |
| 26 | #25 | Complete Space and capacity-query unit coverage |
| 27 | #26 | Complete reservation validation and overlap unit coverage |
| 28 | #27 | Complete reservation query and mutation unit coverage |
| 29 | #28 | Complete persistence and lifecycle unit coverage |
| 30 | #29 | Add discovery and creation controller integration tests |
| 31 | #41 | Add management and restart controller integration tests |
| 32 | #42 | Prepare acceptance-test demo script and submission checklist |
| 33 | #30 | Execute and record all 27 manual acceptance tests |
| 34 | #36 | Document reproducible setup, tests, usage, and persistence |
| 35 | #33 | Record and publish acceptance-test video demonstration |

## Shared completion rules

- Stand-alone JavaFX, OO/MVC, in-memory objects, JSON/text persistence in separate classes; no database.
- Each issue is for 1–2 people and an estimated 0.5–4 hours. Split work if the estimate grows; create issues for discovered defects rather than hiding unlimited repair work inside testing tasks.
- Implementation PRs include focused JUnit tests for logic: normal, edge, and invalid cases. Do not defer initial testing until #25–#28.
- Test each class's meaningful logic, not trivial getters/setters. Keep integration test classes separate and place a US/AT-identifying comment before every integration test method.
- Use feature branches, described PRs, and peer review; merge only working, tested code. Move implementation tasks to Done after merge. Planning/manual-test tasks finish with recorded evidence.
- #30 records all acceptance results, the tested revision/environment, and defect/retest links. #33 demonstrates the selected subset required by the instructor agenda.

## Team decisions and outstanding inputs

These remain team decisions, not completed requirements:

- Approve the fixed space catalog, local-user interpretation, time representation/precision and equal-start/end policy in #5.
- Choose Java/JavaFX versions and Maven/Gradle in #4; agree persistence format/path and error handling in #5/#6.
- Choose a simple schedule presentation that visibly distinguishes occupied and free intervals; actual booking-hour restrictions require explicit justification.
- Obtain **Video Demo Requirements**: the local instructions refer to an agenda, but no separate agenda file is present. #42 owns obtaining it and planning the demo.
- Confirm the time-log code discrepancy: `Team Instructions.md` mentions `O` for meetings; `Development Requirements.md` lists `A/I/V/R/M/P` with `M` for meetings. Use instructor clarification rather than assuming both are consistent.
- Each member still submits their own time log and retrospective (Word/PDF retrospective on Blazeview). #34 and #35 were previously closed/removed as individual responsibilities; those closures do **not** verify anyone has submitted. #42 preserves the reminder in the team submission checklist.

## Verification of this planning pass

- Read back the live issue inventory, estimates, phases, status options and board view configuration after editing; verified the board columns are grouped by Status.
- Checked issue prerequisites for missing references/cycles and confirmed every open task has an estimate between 0.5 and 4 hours and clear deliverables.
- Reconciled the 27 supplied acceptance tests with the coverage map above.
- Verified the later execution-order update: all 35 open task titles and Order values match the physical board sequence, and every prerequisite precedes its dependent task.
- No application test results are claimed: runnable JavaFX and JUnit setup are still future work in #4.
