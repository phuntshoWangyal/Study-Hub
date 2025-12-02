# StudyHub – Smart Study Tracker<br>

StudyHub is an Android application designed to help students track their study habits, understand their learning efficiency, and improve academic performance through data-driven insights.<br>
The app allows students to create courses, add topics, log study time, take tests, and visualize how study patterns relate to grades.<br><br>

## Features<br><br>

### Course and Topic Management<br>
- Create courses and customize them as needed.<br>
- Add topics under each course to organize study material.<br>
- Associate tests with one or multiple topics.<br>
- Tests can belong to multiple courses if required.<br><br>

### Smart Study Time Tracking<br>
Track study time per topic using techniques such as:<br>
- Freestyle<br>
- Pomodoro<br>
- 90-minute blocks<br>
Each study session is logged and stored for later analysis.<br><br>

### Visual Analytics<br>

**Weekly Recap Graph**<br>
- Shows a stacked bar chart of study time for each course.<br>
- Navigate between weeks to compare study habits.<br><br>

**Study Efficiency Graph**<br>
- Scatter plot showing study time versus test grade.<br>
- Automatic linear regression calculates:<br>
  - Slope (improvement rate)<br>
  - R² (correlation strength)<br>
- Trend line updates dynamically based on your data.<br><br>

### Personalized Suggestions<br>

Based on test results:<br>
- Detects weak topics where the student scored below 60.<br>
- Removes duplicates for a clean list of topics to review.<br><br>

Based on regression analysis:<br>
- Low R² indicates insufficient data.<br>
- High slope suggests the study technique is effective.<br>
- Low slope suggests the technique may need improvement.<br><br>

### Ranking System<br>
- Earn EXP for every second spent studying.<br>
- Progress through ranking tiers (Iron → Bronze → Silver → Gold → Diamond).<br>
- Displays a dynamic badge and a progress bar with real-time progression.<br><br>

## Workflow Overview<br>
1. Create a course.<br>
2. Add topics to break down the subject into smaller units.<br>
3. Start a study session using a preferred technique.<br>
4. Create tests and link them to specific topics.<br>
5. Review analytics:<br>
   - Weekly recap<br>
   - Study efficiency graph<br>
   - Suggested weak topics<br>
   - Technique effectiveness<br>
6. Level up with EXP earned through studying.<br><br>

## Tech Stack<br>
- Kotlin<br>
- Firebase Realtime Database<br>
- MPAndroidChart<br>
- Android Jetpack Components<br>
