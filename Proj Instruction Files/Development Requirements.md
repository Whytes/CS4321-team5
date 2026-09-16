# Project – Development Requirements

# Overview

You will build a stand-alone desktop application in Java (JavaFX) based on the user stories that have been provided while adhering to the constraints and requirements in the next four sections.

The focus of this project is on software engineering practices including object-oriented design, testing, version control, and team collaboration.

Read this document carefully, preferably as a Team.

# Use of AI

In this sprint, you are allowed to use AI to help generate code, especially for areas where you may have limited experience such as JavaFX, MVC, file I/O, and JUnit. The goal is to use AI as a development assistant, not a replacement developer.

An incremental approach will lead to a better system

AI works best when you use it to build small pieces that you understand and connect together. If you ask it to build everything at once, you’ll get code that you don’t understand, can’t fix, and can’t extend without making it worse.

The recommended workflow is:

Step 1: Understand the User Story – Read the user story and acceptance tests carefully. Identify inputs, outputs, and constraints before using AI.

Step 2: Break the Story Into Pieces – Do not ask AI to build entire features. Break work into components such as model, logic, UI, and persistence.

Step 3: Ask Targeted Questions – Ask focused questions such as how to implement a class, check time overlap, or create a JavaFX form. Avoid asking for complete systems.

Step 4: Review and Modify Code – Carefully read all generated code. Rename, simplify, and remove anything you do not understand. You must be able to explain any code you commit. If you cannot explain it, you must not use it.

Step 5: Integrate Into Your System – Ensure the code fits your MVC structure and works with existing components. Do not paste code randomly.

Step 6: Test Everything – Write JUnit tests for normal, edge, and invalid cases. Verify behavior matches acceptance tests.

Step 7: Refactor – Improve structure, remove duplication, and ensure proper separation of concerns.

# System Requirements

The system is written as a stand-alone JavaFX desktop application.

The system is object-oriented (OO) and should utilize best practices for OO design.

The system should utilize the MVC architecture (Model, View, Controller). In other words, the event handlers are as short as possible and they should call a controller class(es) to accomplish the requested service.

The system utilizes text, binary, JSON, or XML files for data persistence and all data is in memory as the program runs, represented as objects. The system DOES NOT utilize a database.

# Development Requirements

## Project Management

You will use a GitHub Project to manage your development. Name your View: Sprint 1. Use Board layout (see View button on the right). You will have these columns: To Do, Doing, Done. You can add columns if it helps your team.

You will break work down into small tasks that 1-2 people will work on (some tasks may involve the whole group). These tasks should be estimated to take between 0.5 hours and 4 hours in length.

ALL tasks must be entered in your GitHub Project as issues. Do not do work on the project without a task.

Issues in GitHub and added to the To Do column. From there, they will be moved to a Doing column and finally to the Done column.

Work must be distributed across all team members

Sometimes a task may be to figure out what needs to be done, in other words, a planning session. The conclusion of that task will generally result in concrete tasks that contribute to the development.

Sometimes a task may be a placeholder of sorts, broad, open-ended, just so you don’t forget out it. Then, when ready to address it, it might be broken down into concrete tasks. In this case, you would remove the initial task once it has been broken down.

Task/Issue breakdown is iterative. Your group will identify some initial tasks to get started on development. As issues are in progress or completed you will learn more about what you need to do. If a task turns out to be much bigger, or involve things you didn’t initially think of, then simply add them as new tasks, possibly closing out the initial task. Thus, Task/Issue breakdown is ongoing.

Task Size Guidance (Issue Breakdown)

Breaking work into appropriately sized tasks is critical for effective teamwork and project progress. Tasks (GitHub Issues) should generally be estimated to take between 0.5 and 4 hours to complete.

Well-Sized Tasks (Appropriate Scope)

The following are examples of tasks that are appropriately sized. Each has a clear goal and a clear completion point:

Create Appointment model class with fields, constructors, and basic methods

Implement method to check for overlapping appointment times

Build JavaFX form for adding a new customer (UI only)

Implement controller method to create a new appointment

Write JUnit tests for appointment time validation logic

Add JSON save functionality for appointments

Load customer data from file into memory at program startup

Implement validation for invalid appointment input (e.g., start time after end time)

Refactor controller to remove duplicate logic

Connect JavaFX form to controller method for submitting data

Tasks That Are Too Large (Must Be Broken Down)

The following examples are too broad and should be divided into smaller tasks:

Build the entire UI

Implement the appointment system

Add persistence

Complete User Story #3

Build MVC architecture

Example Breakdown: Instead of: Build the entire UI, break it into:

Create main application window layout

Create appointment entry form

Create list view for displaying appointments

Add event handlers for UI buttons

Tasks That Are Too Small (Avoid These)

The following are too trivial and should not be tracked as individual tasks:

Rename a variable

Fix a typo

Add a single getter or setter

Change a button label or color

Add a single import statement

Refining Vague Tasks

Some tasks may initially be too vague and should be clarified before being added to the project board:

Instead of: Write JUnit tests

Write JUnit tests for appointment time validation logic

Instead of: Fix bugs

Fix bug allowing overlapping appointments

## Testing

Unit Testing Requirement – You are required to implement unit tests using JUnit for each class. Unit tests should target methods that contain logic (e.g., conditionals, loops, calculations), rather than simple getters, setters, or constructors without logic. Each test should verify that the method behaves correctly under normal conditions, as well as under edge cases and invalid inputs (“sad paths”) where the method should handle errors appropriately.

Integration Testing Requirement – You are required to implement a set of integration tests using JUnit that correspond to selected acceptance tests involving business logic (as opposed to presentation/UI behavior). These tests should invoke controller methods that coordinate multiple classes (e.g., controller → service → data) to demonstrate that the system behaves correctly as described in the acceptance test. Integration tests must be placed in separate test classes from your unit tests. Each test method must include a preceding comment identifying the related User Story and Acceptance Test, for example:

System Testing Requirement – You are required to perform system testing by validating the acceptance tests through manual execution of the application. System testing focuses on verifying that the fully integrated system behaves correctly from a user’s perspective, including workflows that involve multiple components and user interactions. As part of your submission, you must demonstrate selected acceptance tests during your video demo. During the demonstration, you should clearly identify the User Story and Acceptance Test being tested and show the steps taken along with the resulting system behavior.

## Coding

Follow Java best practices and naming conventions and proper OO design. Avoid unnecessary static methods.

Adhere to MVC design principles: controllers should coordinate logic; models enforce rules; no business logic in the view. For example:

Bad example:

Good example:

Persistence should use JSON files or text files, but not a database.

Persistence code must be placed in separate classes and should not be implemented inside model classes. These classes are responsible for saving and loading data and should be kept separate from business logic.

## Version Control

Only working, tested code should be in your master branch of GitHub and it should be in a packages: model, view, controller, or something similar.

Use feature branches; do not commit directly to main. For example, branch names are similar to:

feature/short-description

bugfix/short-description

refactor/controller-cleanup

All merges must use Pull Requests. PRs must include description and peer review

Commit frequently with clear messages. Commit early and commit often. When you add a method, commit. When you change something, commit, etc.

## Individual Team Members

Each individual will download the Time Log found on the project page and record all time spent working on the project., on a timely basis. You must provide a meaningful description of your work for each entry (1-3 sentences, usually). Directions for the Time Log are in the next section.

You should work consistently on the project. In other words, you should not do a minimal amount of work one week, and then double the next week. This ensures that project is not held back.

Each member must contribute meaningful GitHub activity

# Time Log Instructions

Each individual will download the Time Log found on the project page and record all time spent working on the project. The spreadsheet has two tabs.

## Activity Codes Tab

The second tab is “Activity Codes” as shown below. You will use one of these codes to allocate each increment of time that you contribute to the project.

Activity Codes

You will record all time (including thinking, meetings, etc) that you spend for this project using the Time Log tab on this spreadsheet. Use the Activity Codes below to indicate how your time was spent.

Choose the code that best represents the primary activity during that time period. You do not need to split time across multiple codes for a single session.

| Code | Activity |
| --- | --- |
| A | Analysis & Understanding (user stories, requirements, thinking, AI exploration) |
| I | Implementation (writing, integrating, or modifying code — including AI-assisted coding) |
| V | Verification (testing, debugging, validation, checking acceptance tests) |
| R | Refactoring & Improvement (cleaning code, restructuring, fixing design issues) |
| M | Meetings & Coordination (team discussions, planning, standups) |
| P | Presentation Preparation (video, retrospective) |

## Time Log Tab

The first tab is “Time Log” as shown below.

Each time you work on the project you will make an entry by supplying: begin date and time, end date and time, interrupt time, activity code, and description of what you did.

For Description of Activity, provide enough detail so that the instructor knows exactly what you are working on. 1-3 sentences should usually suffice.

The total time and running total time will be automatically calculated.

The formulas can get corrupted if you delete a row. You can fix it yourself if you can, or I can fix it.

The date/time field is formatted; however, you don’t have to enter these exactly as shown. For example, for the first entry shown below, if you type: “6/5 9 am” will produce the result shown.

Interrupt time (minutes) is time you were not working. For example, you may have worked from 11 am to 1 pm, and taken 20 minutes for a snack. Thus, for interrupt time you would enter: “0:20”. For this to work properly, you must enter “h:mm”.

For group meetings, each person should enter their time

| Time Log |  |  |  |  |  |  |
| --- | --- | --- | --- | --- | --- | --- |
|  |  |  | Automatically Calculated |  |  |  |
| Beg Date/Time | End Date/Time | Int.Time | Tot Time | Run Total | Act Code | Description of Activity |
| 06/05 09:00 AM | 06/05 09:00 AM | 0:00 | 0:00 | 0:00 | A |  |
| 06/05 09:00 AM | 06/05 09:06 AM | 0:01 | 0:05 | 0:05 | A | Project overview in class |
|  |  |  |  |  |  |  |
