package ca.unb.mobiledev.studyhub

import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.Chronometer
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import androidx.appcompat.app.AlertDialog
import android.widget.EditText
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.widget.Button
import android.widget.CheckBox

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
    private lateinit var noTopicMessage: TextView

    private lateinit var techniqueSpinner: Spinner
    private lateinit var topicMoreButton: ImageView

    private var courseCode: String? = null
    private var courseName: String? = null
    private var topicId: String? = null
    private var topicName: String? = null

    // total time for current topic+technique (ms), NOT including currently-running session
    private var courseTime: Long = 0L
    private val db = FirebaseFirestore.getInstance()

    private lateinit var sessionCountView: TextView
    private var sessionCount: Int = 0

    private lateinit var techniques: List<String>
    private var allTechniqueStats = mutableMapOf<String, Pair<Long, Int>>()
    private var currentTechniqueIndex: Int = 0

    // timer state
    private var timerStarted: Boolean = false
    private var lastSavedHoursForTopic: Double = 0.0
    private var timerStartTsMs: Long = 0L      // wall-clock start time of current session

    private val topics: MutableList<String> = mutableListOf()
    private var currentTopicIndexInList: Int = -1

    private val prefs by lazy {
        requireContext().getSharedPreferences("timer_pref", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        return inflater.inflate(R.layout.course_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        courseCodeView = view.findViewById(R.id.courseContentCode)
        testTitleView  = view.findViewById(R.id.testTitle)
        topicNameView  = view.findViewById(R.id.topicName)
        studyTimeView  = view.findViewById(R.id.studyTime)
        pdfRow         = view.findViewById(R.id.pdfRow)
        leftArrow      = view.findViewById(R.id.leftArrow)
        rightArrow     = view.findViewById(R.id.rightArrow)
        playButton     = view.findViewById(R.id.playButton)
        chronometer    = view.findViewById(R.id.chronometer)
        noTopicMessage = view.findViewById(R.id.noTopicMessage)
        sessionCountView = view.findViewById(R.id.sessionCount)

        val btnViewTestScores = view.findViewById<Button>(R.id.btnViewTestScores)
        btnViewTestScores.setOnClickListener {
            val code = courseCode
            if (code == null) {
                Toast.makeText(
                    requireContext(),
                    "Course code missing",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            (activity as? MainPage)?.openTestScores(code)
        }

        techniqueSpinner = view.findViewById(R.id.techniqueSpinner)
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
        currentTechniqueIndex = 0
        techniqueSpinner.setSelection(currentTechniqueIndex)

        techniqueSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (timerStarted && position != currentTechniqueIndex) {
                    Toast.makeText(
                        requireContext(),
                        "Stop the timer before changing technique",
                        Toast.LENGTH_SHORT
                    ).show()
                    techniqueSpinner.setSelection(currentTechniqueIndex)
                    return
                }

                if (position == currentTechniqueIndex) return

                // before switching technique, sync time for the old one
                if (!courseCode.isNullOrEmpty() && topicName != null) {
                    syncTimeToFirebaseIfNeeded()
                    saveStudyStatsForTechnique()
                }

                // switch
                currentTechniqueIndex = position

                // reset local counters for the new technique
                courseTime = 0L
                lastSavedHoursForTopic = 0.0
                updateStudyTimeLabel(0L)
                sessionCount = 0
                sessionCountView.text = "0"

                // load time for the new technique from DB
                if (!courseCode.isNullOrEmpty() && topicName != null) {
                    loadStudyStatsForTechnique(topicName!!, currentTechniqueIndex)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        courseCodeView.text = courseCode ?: "Course Code"
        topicNameView.text = "No topics yet"
        studyTimeView.text = "00:00:00"
        sessionCountView.text = sessionCount.toString()

        showNoTopicState()

        if (!courseCode.isNullOrEmpty()) {
            FirebaseService.getTopics(courseCode!!) { topicList ->
                topics.clear()
                topics.addAll(topicList)

                if (topics.isEmpty()) {
                    showNoTopicState()
                    currentTopicIndexInList = -1
                } else {
                    currentTopicIndexInList = 0
                    topicName = topics[currentTopicIndexInList]
                    topicNameView.text = topicName
                    loadStudyStatsForTechnique(topicName!!, currentTechniqueIndex)
                }
            }
        }

        // restore timer (after views are ready)
        restoreTimerState()

        leftArrow.setOnClickListener {
            if (timerStarted) {
                Toast.makeText(
                    requireContext(),
                    "Stop the timer before switching topics",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            if (topics.isEmpty()) {
                Toast.makeText(requireContext(), "Add a topic first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (topics.size == 1) {
                Toast.makeText(requireContext(), "Only one topic", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (currentTopicIndexInList <= 0) {
                Toast.makeText(requireContext(), "No previous topic", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveStudyStatsForTechnique()
            currentTopicIndexInList--
            switchToTopic(currentTopicIndexInList)
        }

        rightArrow.setOnClickListener {
            if (timerStarted) {
                Toast.makeText(
                    requireContext(),
                    "Stop the timer before switching topics",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            if (topics.isEmpty()) {
                Toast.makeText(requireContext(), "Add a topic first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (topics.size == 1) {
                Toast.makeText(requireContext(), "Only one topic", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (currentTopicIndexInList >= topics.size - 1) {
                Toast.makeText(requireContext(), "No next topic", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveStudyStatsForTechnique()
            currentTopicIndexInList++
            switchToTopic(currentTopicIndexInList)
        }

        playButton.setOnClickListener {
            if (topicName == null) {
                Toast.makeText(
                    requireContext(),
                    "Create or select a topic first",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (timerStarted) {
                // pause
                val sessionMs = chronometerStop()
                // courseTime already updated in chronometerStop()
                updateStudyTimeLabel(getCurrentAccumulatedMs())

                sessionCount++
                sessionCountView.text = sessionCount.toString()

                saveStudyStatsForTechnique()
                syncTimeToFirebaseIfNeeded()

                timerStarted = false
                playButton.setImageResource(R.drawable.outline_arrow_right_24)
            } else {
                chronometerStart()
                timerStarted = true
                playButton.setImageResource(R.drawable.pause)
            }
        }

        val editCourseCard =
            view.findViewById<androidx.cardview.widget.CardView>(R.id.editCourseCard)

        editCourseCard.setOnClickListener {
            showEditCourseDialog()
        }

        topicMoreButton = view.findViewById(R.id.topicMoreButton)
        topicMoreButton.setOnClickListener {
            showTopicOptionsMenu(it)
        }
    }

    // ---------------- TIMER HELPERS ----------------

    private fun restoreTimerState() {
        courseTime = prefs.getLong("course_time_ms", 0L)
        sessionCount = prefs.getInt("session_count", 0)
        val running = prefs.getBoolean("timer_running", false)
        val startTs = prefs.getLong("timer_start_ts", 0L)

        if (running && startTs > 0L) {
            val nowMs = System.currentTimeMillis()
            val elapsedSessionMs = nowMs - startTs
            chronometer.base = SystemClock.elapsedRealtime() - elapsedSessionMs
            chronometer.start()
            playButton.setImageResource(R.drawable.pause)
            timerStarted = true
            timerStartTsMs = startTs
        } else {
            chronometer.stop()
            chronometer.base = SystemClock.elapsedRealtime()
            playButton.setImageResource(R.drawable.outline_arrow_right_24)
            timerStarted = false
            timerStartTsMs = 0L
        }

        // update UI for total time
        updateStudyTimeLabel(getCurrentAccumulatedMs())
        sessionCountView.text = sessionCount.toString()
    }

    private fun chronometerStart() {
        val startTsMs = System.currentTimeMillis()
        timerStartTsMs = startTsMs

        prefs.edit()
            .putBoolean("timer_running", true)
            .putLong("timer_start_ts", startTsMs)
            .apply()

        val elapsedSinceStart = System.currentTimeMillis() - startTsMs
        chronometer.base = SystemClock.elapsedRealtime() - elapsedSinceStart
        chronometer.start()
    }

    private fun chronometerStop(): Long {
        chronometer.stop()

        val startTs = prefs.getLong("timer_start_ts", 0L)
        val nowMs = System.currentTimeMillis()
        val sessionMs =
            if (startTs > 0L) nowMs - startTs
            else (SystemClock.elapsedRealtime() - chronometer.base)

        courseTime += sessionMs

        prefs.edit()
            .putBoolean("timer_running", false)
            .remove("timer_start_ts")
            .apply()

        chronometer.base = SystemClock.elapsedRealtime()
        timerStartTsMs = 0L

        // don't forget to persist totals (sessionCount is updated outside)
        persistTotals()

        return sessionMs
    }

    private fun getCurrentAccumulatedMs(): Long {
        val running = prefs.getBoolean("timer_running", false)
        val startTs = prefs.getLong("timer_start_ts", 0L)
        return if (running && startTs > 0L) {
            val now = System.currentTimeMillis()
            courseTime + (now - startTs)
        } else {
            courseTime
        }
    }

    private fun persistTotals() {
        prefs.edit()
            .putLong("course_time_ms", courseTime)
            .putInt("session_count", sessionCount)
            .apply()
    }

    // --------------------------------------------------

    private fun showTopicOptionsMenu(anchor: View) {
        val popup = android.widget.PopupMenu(requireContext(), anchor)
        popup.menuInflater.inflate(R.menu.course_content_test_option, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.optionAddTest -> {
                    showAddTopicDialog()
                    true
                }
                R.id.optionEditTest -> {
                    showEditTopicDialog()
                    true
                }
                R.id.optionRemoveTest -> {
                    showRemoveTopicDialog()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun showAddTopicDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.add_topic_dialog, null)

        val nameInput = dialogView.findViewById<EditText>(R.id.edit_topic_name)
        val confirmButton = dialogView.findViewById<Button>(R.id.btn_add_topic)
        val cancelButton = dialogView.findViewById<Button>(R.id.btn_cancel)

        val dialog = builder.setView(dialogView).create()

        confirmButton.setOnClickListener {
            val newTopicName = nameInput.text.toString().trim()

            if (newTopicName.isEmpty()) {
                Toast.makeText(requireContext(), "Enter topic name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val code = courseCode ?: run {
                Toast.makeText(
                    requireContext(),
                    "Course code missing",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            FirebaseService.createTopic(code, newTopicName)

            if (!topics.contains(newTopicName)) {
                topics.add(newTopicName)
            }
            currentTopicIndexInList = topics.indexOf(newTopicName)

            switchToTopic(currentTopicIndexInList)

            Toast.makeText(requireContext(), "Topic created", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showEditTopicDialog() {
        val code = courseCode ?: run {
            Toast.makeText(requireContext(), "Course code missing", Toast.LENGTH_SHORT).show()
            return
        }

        val oldTopicName = topicName ?: run {
            Toast.makeText(requireContext(), "No topic selected to rename", Toast.LENGTH_SHORT)
                .show()
            return
        }

        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.edit_topic_dialog, null)

        val nameInput = dialogView.findViewById<EditText>(R.id.edit_topic_name)
        val confirmButton = dialogView.findViewById<Button>(R.id.btn_confirm_edit_topic)
        val cancelButton = dialogView.findViewById<Button>(R.id.btn_cancel_edit_topic)

        nameInput.setText(oldTopicName)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        confirmButton.setOnClickListener {
            val newTopicName = nameInput.text.toString().trim()

            if (newTopicName.isEmpty()) {
                Toast.makeText(requireContext(), "Enter a topic name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newTopicName == oldTopicName) {
                dialog.dismiss()
                return@setOnClickListener
            }
            FirebaseService.updateTopic(code, oldTopicName, newTopicName)

            val idx = topics.indexOf(oldTopicName)
            if (idx != -1) {
                topics[idx] = newTopicName
                currentTopicIndexInList = idx
            }
            topicName = newTopicName
            topicNameView.text = newTopicName

            loadStudyStatsForTechnique(newTopicName, currentTechniqueIndex)

            Toast.makeText(requireContext(), "Topic renamed", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showRemoveTopicDialog() {
        val code = courseCode ?: run {
            Toast.makeText(requireContext(), "Course code missing", Toast.LENGTH_SHORT).show()
            return
        }

        val currentTopic = topicName ?: run {
            Toast.makeText(requireContext(), "No topic selected to delete", Toast.LENGTH_SHORT)
                .show()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Delete topic")
            .setMessage("Are you sure you want to delete \"$currentTopic\"? All study stats for this topic will be removed.")
            .setPositiveButton("Delete") { _, _ ->

                if (timerStarted) {
                    chronometer.stop()
                    timerStarted = false
                    playButton.setImageResource(R.drawable.outline_arrow_right_24)
                }

                courseTime = 0L
                sessionCount = 0
                sessionCountView.text = "0"
                updateStudyTimeLabel(0L)

                val removedIndex = topics.indexOf(currentTopic)
                if (removedIndex != -1) {
                    topics.removeAt(removedIndex)
                }

                if (topics.isEmpty()) {
                    currentTopicIndexInList = -1
                    showNoTopicState()
                } else {
                    currentTopicIndexInList = if (removedIndex >= topics.size) {
                        topics.size - 1
                    } else {
                        removedIndex
                    }
                    switchToTopic(currentTopicIndexInList)
                }

                FirebaseService.deleteTopic(code, currentTopic)

                Toast.makeText(requireContext(), "Topic deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditCourseDialog() {
        val oldCode = courseCode ?: return

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
                FirebaseService.updateCourse(courseCode!!, newCode)
                FirebaseService.updateCourseName(courseName!!, newName)
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
                FirebaseService.deleteCourse(courseCode!!)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showNoTopicState() {
        topicName = null
        topicNameView.text = "No topics yet"
        studyTimeView.text = "00:00:00"
        sessionCount = 0
        sessionCountView.text = "0"
    }

    override fun onPause() {
        super.onPause()

        // persist local totals
        persistTotals()

        val code = courseCode
        val topic = topicName
        if (code != null && topic != null) {
            saveStudyStatsForTechnique()
            syncTimeToFirebaseIfNeeded()
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

    private fun switchToTopic(index: Int) {
        if (index < 0 || index >= topics.size) return

        topicName = topics[index]
        topicNameView.text = topicName

        courseTime = 0L
        sessionCount = 0
        sessionCountView.text = "0"
        updateStudyTimeLabel(0L)
        lastSavedHoursForTopic = 0.0

        // reset persistent timer state for the new topic
        prefs.edit()
            .putBoolean("timer_running", false)
            .remove("timer_start_ts")
            .putLong("course_time_ms", courseTime)
            .putInt("session_count", sessionCount)
            .apply()

        loadStudyStatsForTechnique(topicName!!, currentTechniqueIndex)
    }

    private fun saveStudyStatsForTechnique() {
        val code = courseCode ?: return
        val topic = topicName ?: return
        FirebaseService.updateSession(code, topic, currentTechniqueIndex, sessionCount)
    }

    private fun dp(v: Int): Int = (resources.displayMetrics.density * v).toInt()

    companion object {
        fun newInstance(courseCode: String, courseName: String): CourseContentFragment {
            val frag = CourseContentFragment()
            frag.arguments = Bundle().apply {
                putString("course_code", courseCode)
                putString("course_name", courseName)
            }
            return frag
        }
    }

    private fun loadStudyStatsForTechnique(topic: String, technique: Int) {
        val code = courseCode ?: return

        FirebaseService.getCourseTimeByTechnique(code, topic, technique) { timeHours ->
            lastSavedHoursForTopic = timeHours

            val totalMs = (timeHours * 3600000.0).toLong()
            courseTime = totalMs
            updateStudyTimeLabel(totalMs)

            FirebaseService.getSessions(code, topic, technique) { sessions ->
                sessionCount = sessions
                sessionCountView.text = sessions.toString()
                persistTotals()
            }
        }
    }

    private fun syncTimeToFirebaseIfNeeded() {
        val code = courseCode ?: return
        val topic = topicName ?: return

        val totalMs = getCurrentAccumulatedMs()
        val totalHours = totalMs.toDouble() / 3_600_000.0

        val deltaHours = totalHours - lastSavedHoursForTopic
        if (deltaHours > 0.0) {
            FirebaseService.updateTopicTime(code, deltaHours, topic, currentTechniqueIndex)
            FirebaseService.updateTotalTopicTime(code, deltaHours, topic)
            // per-day stats (drop fractions)
            FirebaseService.updateTotalCourseTime(code, deltaHours)
            FirebaseService.updateDayStudyTime(code, deltaHours)
            FirebaseService.updateUserTime(deltaHours)
            lastSavedHoursForTopic = totalHours
        }
    }
    override fun onResume() {
        super.onResume()
        restoreTimerState()
    }

}
