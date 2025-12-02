# StudyHub – Smart Study Tracker<br>

StudyHub is an Android application designed to help students track their study habits, understand their learning efficiency, and improve academic performance through data-driven insights.<br>
The app allows students to create courses, add topics, log study time, take tests, and visualize how study patterns relate to grades.<br><br>

## Features<br><br>

### Course and Topic Management<br>
Create courses and customize them as needed.<br>
Add topics under each course to organize study material.<br>
Associate tests with one or multiple topics.<br>
Tests can belong to multiple courses if required.<br><br>

### Smart Study Time Tracking<br>
Track study time per topic using different techniques:<br>
Freestyle<br>
Pomodoro<br>
90-minute blocks<br>
Each study session is logged and stored for later analysis.<br><br>

### Visual Analytics<br><br>

**Weekly Recap Graph**<br>
Displays a stacked bar chart of study time for each course.<br>
Navigate between weeks to compare study habits.<br><br>

**Study Efficiency Graph**<br>
Scatter plot showing study time versus test grade.<br>
Automatic linear regression calculates:<br>
Slope (improvement rate)<br>
R² (correlation strength)<br>
Trend line updates dynamically based on user data.<br><br>

### Personalized Suggestions<br><br>

Based on test results:<br>
Detects weak topics where the student scored below 60.<br>
Removes duplicates for a clear list of topics to review.<br><br>

Based on regression analysis:<br>
Low R² indicates insufficient data.<br>
High slope suggests an effective study technique.<br>
Low slope suggests the technique may need improvement.<br><br>

### Ranking System<br>
Earn EXP for every second spent studying.<br>
Progress through ranking tiers (Iron → Bronze → Silver → Gold → Diamond).<br>
Dynamic badge and progress bar show real-time progression.<br><br>

## Workflow Overview<br>
Create a course.<br>
Add topics to break down the subject into smaller concepts.<br>
Start a study session using a preferred study technique.<br>
Create tests and link them to specific topics.<br>
Review analytics:<br>
Weekly recap<br>
Study efficiency graph<br>
Suggested weak topics<br>
Technique effectiveness<br>
Level up by earning EXP through consistent study time.<br><br>

## Tech Stack<br>
Kotlin<br>
Firebase Realtime Database<br>
MPAndroidChart<br>
Android Jetpack Components<br>
