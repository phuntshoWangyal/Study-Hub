package ca.unb.mobiledev.studyhub

import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class CourseContentFragment : Fragment() {

    // UI
    private lateinit var courseCodeView: TextView
    private lateinit var testTitleView: TextView
    private lateinit var testTopicsView: TextView
    private lateinit var topicNameView: TextView
    private lateinit var studyTimeView: TextView
    private lateinit var pdfRow: LinearLayout
    private lateinit var leftArrow: ImageView
    private lateinit var rightArrow: ImageView
    private lateinit var playButton: ImageView
    private lateinit var chronometer: Chronometer
    private lateinit var techniqueSpinner: Spinner
    private lateinit var optionTestButton: ImageView

    // Firebase (kept from your code)
    private val db by lazy { FirebaseFirestore.getInstance() }

    // arguments
    private var courseCode: String? = null
    private var courseName: String? = null

    // TIME state (milliseconds)
    private var courseTimeMs: Long = 0L         // accumulated total (ms)
    private var sessionCount: Int = 0           // count of paused sessions
    private var timerRunning: Boolean = false   // logical running flag
    private val prefs by lazy { requireContext().getSharedPreferences("timer_pref", Context.MODE_PRIVATE) }

    // Spinner data
    private val techniques = listOf("Your own Technique", "Pomodoro Technique", "Feynman Technique")
    private var currentTechnique: String = techniques[0]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            courseCode = it.getString("course_code")
            courseName = it.getString("course_name")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.course_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // init views
        courseCodeView = view.findViewById(R.id.courseContentCode)
        testTitleView = view.findViewById(R.id.testTitle)
        testTopicsView = view.findViewById(R.id.testTopics)
        topicNameView = view.findViewById(R.id.topicName)
        studyTimeView = view.findViewById(R.id.studyTime)
        pdfRow = view.findViewById(R.id.pdfRow)
        leftArrow = view.findViewById(R.id.leftArrow)
        rightArrow = view.findViewById(R.id.rightArrow)
        playButton = view.findViewById(R.id.playButton)
        chronometer = view.findViewById(R.id.chronometer)
        techniqueSpinner = view.findViewById(R.id.techniqueSpinner)
        optionTestButton = view.findViewById(R.id.more_button)

        // UI text
        courseCodeView.text = courseCode ?: "Course Code"
        topicNameView.text = courseName ?: "Topic name"

        // spinner setup
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, techniques)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        techniqueSpinner.adapter = spinnerAdapter
        techniqueSpinner.setSelection(0)
        techniqueSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                val selected = techniques[position]
                // Don't allow switching technique while timer is running
                if (timerRunning && selected != currentTechnique) {
                    Toast.makeText(requireContext(), "Stop the timer before changing technique", Toast.LENGTH_SHORT).show()
                    val idx = techniques.indexOf(currentTechnique)
                    if (idx >= 0) techniqueSpinner.setSelection(idx)
                    return
                }
                currentTechnique = selected
                // optionally load technique-specific stats here
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // restore persisted timer/state
        restoreTimerState()

        // arrows
        leftArrow.setOnClickListener { Toast.makeText(requireContext(), "Left arrow clicked", Toast.LENGTH_SHORT).show() }
        rightArrow.setOnClickListener { Toast.makeText(requireContext(), "Right arrow clicked", Toast.LENGTH_SHORT).show() }

        // play/pause toggle
        playButton.setOnClickListener {
            if (timerRunning) {
                // stop session (user paused)
                val sessionMs = chronometerStop()
                sessionCount++
                persistTotals()
                // update UI label
                updateStudyTimeLabel(courseTimeMs)
                // push totals to Firebase in hours
                courseCode?.let { code ->
                  //  FirebaseService.updateTime(code, , "Fundamentals", 0)
                    //FirebaseService.updateDayStudyTime(code, )
                }
                playButton.setImageResource(R.drawable.outline_arrow_right_24)
            } else {
                // start session
                chronometerStart()
                playButton.setImageResource(R.drawable.pause)
            }
        }

        // test options popup
        optionTestButton.setOnClickListener {
            val popup = PopupMenu(requireContext(), optionTestButton)
            popup.menuInflater.inflate(R.menu.course_content_test_option, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.optionEditTest -> {
                        val testName = testTitleView.text.toString()
                        showEditTestDialog(courseCode ?: return@setOnMenuItemClickListener true, testName)
                        true
                    }
                    R.id.optionAddTest -> {
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

        // fetch notes/tests if course provided
        if (!courseCode.isNullOrEmpty()) {
            // fetchNotes(courseCode!!)
            // fetchTestSummary(courseCode!!)
        }
    }

    // ------------------------
    // TIMER: Timestamp-based approach (works across fragment/activity/app restarts)
    // ------------------------

    private fun chronometerStart() {
        // Save start timestamp (wall-clock). Use System.currentTimeMillis() so it survives process death & reboot accounting is acceptable.
        val startTsMs = System.currentTimeMillis()
        prefs.edit()
            .putBoolean("timer_running", true)
            .putLong("timer_start_ts", startTsMs)
            .apply()

        // For display: compute elapsed since start (should be zero at start). Use elapsedRealtime base to drive Chronometer UI.
        val elapsedSinceStart = System.currentTimeMillis() - startTsMs // normally 0
        chronometer.base = SystemClock.elapsedRealtime() - elapsedSinceStart
        chronometer.start()
        timerRunning = true
    }

    /**
     * Stops current session and returns session duration (ms).
     * Adds session duration to courseTimeMs and clears running flag.
     */
    private fun chronometerStop(): Long {
        chronometer.stop()

        // Determine session duration using wall-clock: compute now - saved start timestamp
        val startTs = prefs.getLong("timer_start_ts", 0L)
        val nowMs = System.currentTimeMillis()
        val sessionMs = if (startTs > 0L) nowMs - startTs else (SystemClock.elapsedRealtime() - chronometer.base).toLong()

        // accumulate
        courseTimeMs += sessionMs

        // clear running flag and start timestamp
        prefs.edit()
            .putBoolean("timer_running", false)
            .remove("timer_start_ts")
            .putLong("course_time_ms", courseTimeMs)
            .putInt("session_count", sessionCount)
            .apply()

        // reset chronometer base to now (so it will show 0 next start)
        chronometer.base = SystemClock.elapsedRealtime()
        timerRunning = false

        return sessionMs
    }

    /**
     * Restore timer state at fragment creation.
     * If a session was running, compute elapsed = now - startTs and start chronometer to show that elapsed.
     * If not running, set chronometer to 0 and update study label from persisted totals.
     */
    private fun restoreTimerState() {
        // Load persisted totals
        courseTimeMs = prefs.getLong("course_time_ms", courseTimeMs)
        sessionCount = prefs.getInt("session_count", sessionCount)

        val running = prefs.getBoolean("timer_running", false)
        val startTs = prefs.getLong("timer_start_ts", 0L)

        if (running && startTs > 0L) {
            // compute elapsed since start in wall-clock
            val nowMs = System.currentTimeMillis()
            val elapsedSessionMs = nowMs - startTs

            // chronometer displays elapsed = elapsedRealtimeNow - base
            chronometer.base = SystemClock.elapsedRealtime() - elapsedSessionMs
            chronometer.start()
            timerRunning = true
            // do not add elapsedSessionMs to courseTimeMs yet (will add when paused)
        } else {
            chronometer.stop()
            chronometer.base = SystemClock.elapsedRealtime()
            timerRunning = false
        }

        // Update total time label using the accumulated courseTimeMs (doesn't include current running session)
        updateStudyTimeLabel(courseTimeMs)
    }

    // Persist totals and (if desired) upload to Firebase in onPause
    override fun onPause() {
        super.onPause()
        // Persist totals
        prefs.edit()
            .putLong("course_time_ms", courseTimeMs)
            .putInt("session_count", sessionCount)
            .apply()

        // If timer is running, we keep it logically running: we already saved timer_start_ts at start.
        // We should still upload current accumulated total (excluding the running session) if you want:
        courseCode?.let { code ->
            val hours = courseTimeMs.toDouble() / 3_600_000.0
            //FirebaseService.updateTime(code, hours, "Fundamentals", 0)
           // FirebaseService.updateDayStudyTime(code, hours)
        }
    }

    // Helper to update HH:MM:SS label from milliseconds
    private fun updateStudyTimeLabel(totalMs: Long) {
    }

    private fun persistTotals() {
        prefs.edit()
            .putLong("course_time_ms", courseTimeMs)
            .putInt("session_count", sessionCount)
            .apply()
    }

    // ------------------------
    // Test add/edit dialogs (kept as you asked)
    // ------------------------

    private fun showAddTestDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.add_test_dialog, null)
        val nameInput = dialogView.findViewById<EditText>(R.id.edit_test_name)
        val confirmButton = dialogView.findViewById<Button>(R.id.btn_add_test)
        val cancelButton = dialogView.findViewById<Button>(R.id.btn_cancel)
        val dialog = builder.setView(dialogView).create()

        confirmButton.setOnClickListener {
            val testName = nameInput.text.toString().trim()
            if (testName.isEmpty()) {
                Toast.makeText(requireContext(), "Enter test name", Toast.LENGTH_SHORT).show()
            } else {
                courseCode?.let { FirebaseService.createTest(it, testName) }
                Toast.makeText(requireContext(), "Test created", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
        cancelButton.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showEditTestDialog(courseCode: String, testName: String) {
        val builder = AlertDialog.Builder(requireContext())
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.edit_test_dialog, null)
        val nameInput = dialogView.findViewById<EditText>(R.id.edit_test_name)
        val gradeInput = dialogView.findViewById<EditText>(R.id.edit_test_grade)
        val topicsContainer = dialogView.findViewById<LinearLayout>(R.id.layout_topics_list)
        val confirmButton = dialogView.findViewById<Button>(R.id.btn_save_edit_test)
        val cancelButton = dialogView.findViewById<Button>(R.id.btn_cancel_edit_test)

        nameInput.setText(testName)
        val dialog = builder.setView(dialogView).create()

        // Load topics
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
                if (checkBox.isChecked) selectedTopics.add(checkBox.text.toString())
            }

            // Update test name & grade & add selected topics
            FirebaseService.updateTest(courseCode, testName, newName)
            FirebaseService.setGrade(courseCode, newName, grade)
            for (topic in selectedTopics) {
                FirebaseService.addTopic(courseCode, newName, topic)
            }

            Toast.makeText(requireContext(), "Test updated", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        cancelButton.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    // ------------------------
    // placeholders for your other existing functions
    // ------------------------

    private fun fetchNotes(code: String) {
        // keep your existing implementation (PDF icons etc.)
    }

    private fun fetchTestSummary(code: String) {
        // if you store test topics in realtime DB, load them here
        // Example:
        // FirebaseService.getTestTopics(code, testName) { topics -> ... }
    }

    // Utility
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
}
