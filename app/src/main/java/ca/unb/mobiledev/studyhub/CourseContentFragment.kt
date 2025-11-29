package ca.unb.mobiledev.studyhub

import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.view.*
import android.widget.Button
import android.widget.CheckBox
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import android.widget.Chronometer
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import androidx.appcompat.app.AlertDialog
import android.widget.EditText
import com.google.firebase.firestore.SetOptions
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.AdapterView




class CourseContentFragment : Fragment() {

    private lateinit var courseCodeView: TextView
    private lateinit var testTitleView: TextView
    private lateinit var testTopicsView: TextView
    private lateinit var topicNameView: TextView
    private lateinit var studyTimeView: TextView
    private lateinit var pdfRow: LinearLayout
    private lateinit var leftArrow: ImageView
    private lateinit var rightArrow: ImageView
    private lateinit var playButton: ImageView
    private lateinit var editCourseButton: ImageView
    private lateinit var chronometer: Chronometer

    private lateinit var techniqueSpinner: Spinner

    private lateinit var topicMoreButton: ImageView


    private val db by lazy { FirebaseFirestore.getInstance() }

    private var courseCode: String? = null
    private var courseName: String? = null
    private var topicId: String? = null
    private var topicName: String? = null

    private var courseTime: Long = 0L

    private lateinit var sessionCountView: TextView
    private var sessionCount: Int = 0

    private lateinit var techniques: List<String>
    private var allTechniqueStats = mutableMapOf<String, Pair<Long, Int>>()
    private var currentTechnique: String = "Your own Technique"
    private var timerStarted: Boolean = false

    private var timerBase: Long = 0L

    private val prefs by lazy {
        requireContext().getSharedPreferences("timer_pref", Context.MODE_PRIVATE)
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // get arguments passed in
        arguments?.let {
            courseCode = it.getString("course_code")
            courseName = it.getString("course_name")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // use your existing XML layout here
        return inflater.inflate(R.layout.course_content, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var timerStarted = false
        var hours: Int
        var minutes: Int
        var seconds: Int

        courseCodeView = view.findViewById(R.id.courseContentCode)
        testTitleView  = view.findViewById(R.id.testTitle)
        testTopicsView = view.findViewById(R.id.testTopics)
        topicNameView  = view.findViewById(R.id.topicName)
        studyTimeView  = view.findViewById(R.id.studyTime)
        pdfRow         = view.findViewById(R.id.pdfRow)
        leftArrow      = view.findViewById(R.id.leftArrow)
        rightArrow     = view.findViewById(R.id.rightArrow)
        playButton     = view.findViewById(R.id.playButton)
        chronometer    = view.findViewById(R.id.chronometer)
        restoreTimerState()
        //sessionCountView = view.findViewById(R.id.sessionCount)
        sessionCountView.text = sessionCount.toString()

        //techniqueSpinner = view.findViewById(R.id.techniqueSpinner)

        techniques = listOf(
            "Your own Technique",
            "Pomodoro Technique",
            "Feynman Technique"
        )


        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            techniques
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        techniqueSpinner.adapter = spinnerAdapter
        currentTechnique = techniques[0]
        techniqueSpinner.setSelection(0)



        techniqueSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selected = techniques[position]

                // DO NOT SWITCH if timer is running (and selection changes)
                if (timerStarted && selected != currentTechnique) {
                    Toast.makeText(
                        requireContext(),
                        "Stop the timer before changing technique",
                        Toast.LENGTH_SHORT
                    ).show()
                    val idx = techniques.indexOf(currentTechnique)
                    if (idx >= 0) techniqueSpinner.setSelection(idx)
                    return
                }

                // Do nothing if the same item is selected
                if (selected == currentTechnique) return

                // Save the stats for the technique
                if (!courseCode.isNullOrEmpty()) {

                    //saveStudyStatsForTechnique()
                }

                // SWITCH to the new technique
                currentTechnique = selected


                if (!courseCode.isNullOrEmpty()) {
                    //loadStudyStatsForTechnique(courseCode!!, currentTechnique)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }



        courseCodeView.text = courseCode ?: "Course Code"
        topicNameView.text = courseName ?: "Topic name"
        studyTimeView.text = "Session Study Time: 00:00:00"

        if (!courseCode.isNullOrEmpty()) {
            //fetchNotes(courseCode!!)
            //fetchTestSummary(courseCode!!)
        }

        leftArrow.setOnClickListener {
            Toast.makeText(requireContext(), "Left arrow clicked", Toast.LENGTH_SHORT).show()
        }
        rightArrow.setOnClickListener {
            Toast.makeText(requireContext(), "Right arrow clicked", Toast.LENGTH_SHORT).show()
        }
        playButton.setOnClickListener {
            if (timerStarted) {
                // stop and add this session to total for *current technique*
                courseTime += chronometerStop()
                updateStudyTimeLabel(courseTime)

                sessionCount++
                sessionCountView.text = sessionCount.toString()

                saveStudyStatsForTechnique()

            if (timerStarted) {
                courseTime += chronometerStop()
                hours = (courseTime / 3600000).toInt()
                minutes = ((courseTime - hours*3600000) / 60000).toInt()
                seconds = ((courseTime - hours*3600000 - minutes*60000) / 1000).toInt()

                studyTimeView.text = "Session Study Time: %02d:%02d:%02d".format(hours, minutes, seconds)

                timerStarted = false
                playButton.setImageResource(R.drawable.outline_arrow_right_24)
            }
            else{
                chronometerStart()
                timerStarted = true
                playButton.setImageResource(R.drawable.pause)
            }
        }


        val editCourseCard = view.findViewById<androidx.cardview.widget.CardView>(R.id.editCourseCard)

        editCourseCard.setOnClickListener {
            showEditCourseDialog()   // the function you already have for editing/deleting
        }
        val optionTestButton = view.findViewById<ImageView>(R.id.option_test_button)

        optionTestButton.setOnClickListener {
            val popup = PopupMenu(requireContext(), optionTestButton)
            popup.menuInflater.inflate(R.menu.course_content_test_option, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                when(item.itemId) {
                    R.id.optionEditTest -> {
                        Toast.makeText(requireContext(), "Edit Test clicked", Toast.LENGTH_SHORT).show()
                        showEditTestDialog(courseCode!!, testTitleView.text.toString())
                        true
                    }
                    R.id.optionAddTest -> {
                        Toast.makeText(requireContext(), "Add Test clicked", Toast.LENGTH_SHORT).show()
                        showAddTestDialog()
                        true
                    }
                    R.id.optionRemoveTest -> {
                        Toast.makeText(requireContext(), "Remove Test clicked", Toast.LENGTH_SHORT).show()
                        true
                    }
                    else -> false
                }
            }

            popup.show()
        }

    }

    private fun showEditTestDialog(courseCode: String, testName: String) {

        val builder = AlertDialog.Builder(requireContext())
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.edit_test_dialog, null)

        val nameInput = dialogView.findViewById<EditText>(R.id.edit_test_name)
        val gradeInput = dialogView.findViewById<EditText>(R.id.edit_test_grade)
        val topicsContainer = dialogView.findViewById<LinearLayout>(R.id.layout_topics_list)

        val confirmButton = dialogView.findViewById<Button>(R.id.btn_save_edit_test)
        val cancelButton = dialogView.findViewById<Button>(R.id.btn_cancel_edit_test)

        // Pre-fill existing name
        nameInput.setText(testName)

        val dialog = builder.setView(dialogView).create()

        // Load topics dynamically
        FirebaseService.getTopics(courseCode) { topics ->
            topicsContainer.removeAllViews()

            for (topic in topics) {
                val checkbox = CheckBox(requireContext())
                checkbox.text = topic
                checkbox.isChecked = false
                topicsContainer.addView(checkbox)
            }
        }

        confirmButton.setOnClickListener {
            val newName = nameInput.text.toString().trim()
            val gradeText = gradeInput.text.toString().trim()

            if (newName.isEmpty()) {
                Toast.makeText(requireContext(), "Enter new test name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val grade = gradeText.toDoubleOrNull()
            if (grade == null) {
                Toast.makeText(requireContext(), "Enter valid grade", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Collect selected topics
            val selectedTopics = mutableListOf<String>()
            for (i in 0 until topicsContainer.childCount) {
                val checkBox = topicsContainer.getChildAt(i) as CheckBox
                if (checkBox.isChecked) {
                    selectedTopics.add(checkBox.text.toString())
                }
            }

            // Update test name
            FirebaseService.updateTest(courseCode, testName, newName)

            // Update grade
            FirebaseService.setGrade(courseCode, newName, grade)

            // Add selected topics
            for (topic in selectedTopics) {
                FirebaseService.addTopic(courseCode, newName, topic)
            }

            Toast.makeText(requireContext(), "Test updated", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showAddTestDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.add_test_dialog, null)

        val nameInput = dialogView.findViewById<EditText>(R.id.edit_test_name)
        val confirmButton = dialogView.findViewById<Button>(R.id.btn_add_test)
        val cancelButton = dialogView.findViewById<Button>(R.id.btn_cancel)

        val dialog = builder.setView(dialogView).create()

        confirmButton.setOnClickListener {
            val testName = nameInput.text.toString().trim()

            if (testName.isEmpty()) {
                Toast.makeText(requireContext(), "Enter test name", Toast.LENGTH_SHORT).show()
            } else {

                FirebaseService.createTest(courseCode!!, testName)

                Toast.makeText(requireContext(), "Test created", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


    }
    private fun restoreTimerState() {
        val running = prefs.getBoolean("timer_running", false)

        if (!running) {
            // timer is not running → chronometer reset to 0
            chronometer.stop()
            chronometer.base = SystemClock.elapsedRealtime()
            return
        }

        // Timer was running → continue counting from last start moment
        val startRealtime = prefs.getLong("timer_start_realtime", 0L)
        val now = SystemClock.elapsedRealtime()

        // Rebuild the base so session time continues
        timerBase = startRealtime
        chronometer.base = timerBase

        chronometer.start()
        timerStarted = true
        playButton.setImageResource(R.drawable.pause)
    }


    private fun showEditCourseDialog() {
        val oldCode = courseCode ?: return

        // container layout for two inputs
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(10), dp(20), 0)
        }

        val codeInput = EditText(requireContext()).apply {
            hint = "Course code (e.g., CS 2063)"
            setText(courseCode ?: "")
        }

        val nameInput = EditText(requireContext()).apply {
            hint = "Course name"
            setText(courseName ?: "")
        }

        container.addView(codeInput)
        container.addView(nameInput)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit course")
            .setView(container)
            .setPositiveButton("Save") { _, _ ->
                val newCode = codeInput.text.toString().trim()
                val newName = nameInput.text.toString().trim()

                if (newCode.isEmpty() || newName.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "Course code and name can’t be empty",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                (activity as? MainPage)?.updateCourse(oldCode, newCode, newName)
                /*This Function exists in different branch, delete comments when merged
                FirebaseService.updateCourse(courseCode!!, newCode)
                 */
                FirebaseService.updateCourseName(courseCode!!, newName)

                courseCode = newCode
                courseName = newName
                courseCodeView.text = newCode
                topicNameView.text = newName

            }
            .setNeutralButton("Delete course") { _, _ ->
                showDeleteConfirmDialog(oldCode)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun showDeleteConfirmDialog(code: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete course")
            .setMessage("Are you sure you want to delete this course?")
            .setPositiveButton("Delete") { _, _ ->
                (activity as? MainPage)?.deleteCourse(code)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // In CourseContentFragment
    override fun onPause() {
        super.onPause()

        // Check if the timer is running and the start time is saved
        val running = prefs.getBoolean("timer_running", false)
        if (running) {
            // Calculate the elapsed time *up to this moment* (when the app is backgrounded)
            val now = SystemClock.elapsedRealtime()
            val startRealtime = prefs.getLong("timer_start_realtime", 0L)
            val elapsedSessionMs = now - startRealtime

            // Add the elapsed time to courseTime and then save it
            courseTime += elapsedSessionMs

            // Update the start time to now so the next time it restores, it counts from here (optional,
            // but helps keep the persistent state clean if you resume the chronometer later)
            prefs.edit()
                .putLong("timer_start_realtime", now)
                .apply()

            // The chronometer on screen is stopped by the OS, but we need to stop its counting mechanism
            // by resetting its base and stopping it explicitly to prevent issues if it was still active
            chronometer.stop()
            chronometer.base = now

            // Note: We don't change timerStarted state yet, as the user didn't explicitly pause it.
            // It's still logically "running" but paused in its calculation.
        }

        // Now save the updated total time
        saveStudyStatsForTechnique()
        // Update Firebase with the total time
        // Note: Use a reliable method for courseCode check
        courseCode?.let { code ->
            FirebaseService.updateTime(code, courseTime / 3600000, "Fundamentals", 0)
            FirebaseService.updateDayStudyTime(code, courseTime / 3600000)
        }
    }
    private fun fetchNotes(code: String) { /* your original code */

        FirebaseService.getTestTopics(courseCode!!, testName){ topics ->
            if(topics.isEmpty()){
                testTitleView.text = "No tests yet"
                testTopicsView.text = ""
            }
            else {
                testTitleView.text = testName
                testTopicsView.text = "Includes $topics"
            }
        }
    }

    private fun updateStudyTimeLabel(totalMs: Long) {
        val hours = (totalMs / 3600000).toInt()
        val minutes = ((totalMs - hours * 3600000) / 60000).toInt()
        val seconds = ((totalMs - hours * 3600000 - minutes * 60000) / 1000).toInt()

        val hoursStr = String.format("%02d", hours)
        val minStr = String.format("%02d", minutes)
        val secStr = String.format("%02d", seconds)
        studyTimeView.text = "$hoursStr:$minStr:$secStr"
    }
    private fun fetchTestSummary(code: String) { /* your original code */ }

    private fun chronometerStart() {
        val now = SystemClock.elapsedRealtime()

        // Chronometer always starts from 0
        timerBase = now
        chronometer.base = timerBase
        chronometer.start()

        // Save running state persistently
        prefs.edit()
            .putBoolean("timer_running", true)
            .putLong("timer_start_realtime", now)   // only store the REAL time when started
            .apply()
    }

    // In CourseContentFragment
    private fun chronometerStop(): Long {
        chronometer.stop()

        val now = SystemClock.elapsedRealtime()
        // This calculates the session time *since the chronometer's base was last set*
        val session = now - chronometer.base

        // Add to total study time
        courseTime += session

        // Reset persistent running flag and start time
        prefs.edit()
            .putBoolean("timer_running", false)
            .remove("timer_start_realtime") // Remove the start time
            .apply()

        // Reset chronometer display to 0:00
        chronometer.base = now

        return session
        return (SystemClock.elapsedRealtime() - chronometer.base).toInt()
    }

    private fun saveStudyStatsForTechnique() {
        val technique = currentTechnique //TECHNIQUE IS EXPECTED AS INT IN FUNCTION, CHANGE IT!!!!

        /*
        val data = hashMapOf<String, Any>(
            "totalStudyMs" to courseTime, //CHANGE courseTime TO HOURS NOT MS!!!!!!!!!
            "sessionCount" to sessionCount
        )
        database is not correct
        db.collection("courses")
            .document(code)
            .collection("techniques")
            .document(technique)
            .set(data, SetOptions.merge())
        */
        FirebaseService.updateTopicTime(courseCode!!, courseTime.toDouble(), topicName!!, technique)
        FirebaseService.updateSession(courseCode!!, topicName!!, sessionCount)
    }


    private fun dp(v: Int): Int = (resources.displayMetrics.density * v).toInt()

    companion object {
        fun newInstance(courseCode: String, courseName: String): CourseContentFragment {
            return CourseContentFragment().apply {
                arguments = Bundle().apply {
                    putString("course_code", courseCode)
                    putString("course_name", courseName)
                }
            }
        }
    }
    private fun loadStudyStatsForTechnique(topic: String, technique: Int) {
        // Change loadStudyStatsForTechnique to a listener

        /*db.collection("courses")
            .document(code)
            .collection("techniques")
            .document(technique)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    // Handle error
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val totalMs = snapshot.getLong("totalStudyMs") ?: 0L
                    val sessions = snapshot.getLong("sessionCount") ?: 0L

                    // Only update local state if this is the currently selected technique
                    if (technique == currentTechnique) {
                        courseTime = totalMs
                        sessionCount = sessions.toInt()

                        sessionCountView.text = sessionCount.toString()
                        updateStudyTimeLabel(totalMs)

                        // reset live session timer display
                        chronometer.base = SystemClock.elapsedRealtime()
                        chronometer.stop()
                    }
                }
            }
            */
        FirebaseService.getCourseTimeByTechnique(courseCode!!, topic, technique){ time ->
            courseTime = time.toLong()//CHANGE courseTime TO HOURS NOT MS!!!!!!!!!
            FirebaseService.getSessions(courseCode!!, topic){ sessions ->
                sessionCount = sessions
            }
        }
    }

    private fun fetchAllTechniqueStats(technique: Int) {
        /*db.collection("courses")
            .document(code)
            .collection("techniques")
            .get()
            .addOnSuccessListener { snapshot ->
                allTechniqueStats.clear()
                for (document in snapshot.documents) {
                    val technique = document.id
                    val totalMs = document.getLong("totalStudyMs") ?: 0L
                    val sessions = document.getLong("sessionCount")?.toInt() ?: 0

                    allTechniqueStats[technique] = Pair(totalMs, sessions)
                }

                updateStatsForCurrentTechnique()
            }
            .addOnFailureListener {
                // Handle error
            }
         */
        //TIME IS STORED IN HOURS NOT MS!!!!!!!!
        FirebaseService.getCourseTimeByTechnique(courseCode!!, topicName!!, technique){ time ->

            FirebaseService.getSessions(courseCode!!, topicName!!){ sessions ->
                //SAVE TIME IN HOURS NOT MS!!!!!
                //USE YOUR LOGIC TO SAVE IT IN RIGHT VALUES HERE!!!!!
            }
        }
    }


    private fun updateStatsForCurrentTechnique() {
        //TIME IS STORED IN HOURS NOT MS!!!!!!!!
        val (totalMs, sessions) = allTechniqueStats[currentTechnique] ?: Pair(0L, 0)

        courseTime = totalMs //CHANGE courseTime TO HOURS NOT MS!!!!!!!!!
        sessionCount = sessions

        sessionCountView.text = sessionCount.toString()
        updateStudyTimeLabel(totalMs)

        // reset live session timer display
        chronometer.base = SystemClock.elapsedRealtime()
        chronometer.stop()
    }


}
