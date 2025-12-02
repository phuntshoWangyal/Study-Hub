StudyHub – Smart Study Tracker

StudyHub is an Android application designed to help students track their study habits, understand their learning efficiency, and improve academic performance through data-driven insights. The app allows students to create courses, add topics, log study time, take tests, and visualize how study patterns relate to grades.

Features
Course and Topic Management

Create courses and customize them as needed.

Add topics under each course to organize study material.

Associate tests with one or multiple topics.

Tests can belong to multiple courses if required.

Smart Study Time Tracking

Track study time per topic using different techniques:

Freestyle

Pomodoro

90-minute blocks

Each study session is logged and stored for later analysis.

Visual Analytics

Weekly Recap Graph

Displays a stacked bar chart of study time for each course.

Navigate between weeks to compare study habits.

Study Efficiency Graph

Scatter plot showing study time versus test grade.

Automatic linear regression calculates:

Slope (improvement rate)

R² (correlation strength)

Trend line updates dynamically based on user data.

Personalized Suggestions

Based on test results:

Detects weak topics where the student scored below 60.

Removes duplicates for a clear list of topics to review.

Based on regression analysis:

Low R² indicates insufficient data.

High slope suggests an effective study technique.

Low slope suggests the technique may need improvement.

Ranking System

To keep users motivated:

Earn EXP for every second spent studying.

Progress through ranking tiers (Iron → Bronze → Silver → Gold → Diamond).

Dynamic badge and progress bar show real-time progression.

Workflow Overview

Create a course.

Add topics to break down the subject into smaller concepts.

Start a study session using a preferred study technique.

Create tests and link them to specific topics.

Review analytics:

Weekly recap

Study efficiency graph

Suggested weak topics

Technique effectiveness

Level up by earning EXP through consistent study time.

Tech Stack

Kotlin

Firebase Realtime Database

MPAndroidChart

Android Jetpack Components
