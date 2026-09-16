# Project – Getting Started Guide

This guide walks your team through exactly what to do first and how to begin turning user stories into working software.

# Step 1: Create the Team Repository (One Person Only)

One team member is responsible for the initial setup of the project.

Create a new GitHub repository

Name it: CS4321-teamX, where “X” is your team number.

Create the following package (folder) structure inside the project:

- model

- view

- controller

- persistence

- test

Add a simple starter class:

public class Reservation {

public static void main(String[] args) {

System.out.println("Sprint project started");

}

}

Commit and push this initial project

Invite all team members as collaborators on the repository

At this point:

- Everyone should be able to clone the repo

- The project should compile and run

- You have a clean starting point

# Step 2: Everyone Clones and Verifies Setup

Each team member should:

Clone the repository

Open it in your IDE (e.g., IntelliJ)

Run the Reservation class

Confirm the message prints successfully

If you cannot run the program, fix setup issues now before moving forward.

# Step 3: Read All User Stories (As a Team)

Before coding anything:

Read all user stories and acceptance tests together

Make sure everyone understands what the system should do, inputs/outputs, and rules

Do not start coding yet.

After discussing a story, you could run it through AI with prompts as shown below to gain a further understanding. At this point, your team needs an understanding of what the system is supposed to do. You’ll get deeper when you start development in Step 4, where these prompts may be more useful.

Explain this user story in simple terms

What edge cases should I consider?

Then, discuss again. Use AI to refine your understanding.

# Step 4: Break ONE User Story into Tasks

Start with just one user story and analyze it in detail, using AI as an aid to your current understanding, if necessary. The appendix below (Appendix 1) contains the step to analyze a user story in detail.

Break the user story into tasks that take 0.5-4 hours.

Example tasks:

Create Reservation model class

Add validation for reservation data

Create controller method

Build simple UI form

Write JUnit tests

Create each task as a GitHub Issue and add to the Project Board in the “To Do” column.

Assign issues to team members.

# Step 5: Start Development (One Task at a Time)

For each task:

Pick a task from To Do

Move it to Doing

Create a branch: feature/short-description

Implement the task

Write tests for logic

Commit frequently

# Step 6: Use Pull Requests

Push your branch

Create a Pull Request

Get a review

Merge and move task to Done

# Step 7: Work Iteratively

Continue with remaining user stories using small tasks.

# Step 8: Track Your Time

Record all work in the Time Log spreadsheet

Include coding, meetings, planning, testing. If you are logging time for a meeting, each participant should log time.

Add a short description – Provide enough detail so that the instructor knows exactly what you are working on. 1-3 sentences should usually suffice.

# Appendix

How to Analyze a User Story

Before writing any code, you must clearly understand what the user story is asking. Use the following questions to guide your analysis.

What are the Inputs?

What data does the user provide?

What fields are required?

What format should the data be in?

Examples:

Reservation date and time

Customer name

Number of guests

What are the Outputs?

What should the system do or display?

What confirms success?

Examples:

Reservation is successfully created and stored

Confirmation message is displayed

Reservation appears in a list

What are the Rules (Business Logic)?

What conditions must be true?

What validations are required?

Examples:

Reservation time cannot be in the past

Number of guests must be > 0

No overlapping reservations allowed

What are the Edge Cases?

What unusual or boundary situations might occur?

Examples:

Maximum capacity reached

Same start and end time

Very large input values

What are Invalid (Sad Path) Cases?

What should happen when something goes wrong?

Examples:

Missing required fields

Invalid date format

Duplicate reservation

What Classes Might Be Needed?

Start thinking in terms of MVC:

Model: What data objects represent this?

Controller: What logic processes the request?

View: What UI is needed?

Persistence: Does this need to be saved/loaded?
