    package ca.unb.mobiledev.studyhub

    import android.content.Context
    import android.content.SharedPreferences
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

        // Firebase (kept)
        private val db by lazy { FirebaseFirestore.getInstance() }

        // Arguments
        private var courseCode: String? = null
        private var courseName: String? = null

        // Timer state (milliseconds)
        private var courseTimeMs: Long = 0L        // accumulated total time (ms), excludes current running session
        private var sessionCount: Int = 0         // number of paused sessions
        private var timerRunning: Boolean = false // logical running flag
        private var timerStartTsMs: Long = 0L     // wall-clock start timestamp (ms) when session started

        // SharedPrefs (initialized in onCreate)
        private lateinit var prefs: SharedPreferences

        // Spinner data
        private val techniques = listOf("Your own Technique", "Pomodoro Technique", "Feynman Technique")
        private var currentTechnique: String = techniques[0]

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            prefs = requireContext().getSharedPreferences("timer_pref", Context.MODE_PRIVATE)

            arguments?.let {
                courseCode = it.getString("course_code")
                courseName = it.getString("course_name")
            }
        }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            return inflater.inflate(R.layout.course_content, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            // wire views
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
            optionTestButton = view.findViewById(R.id.more_button) // or your actual id

            // UI texts
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

            // restore timer & totals
            restoreTimerState()

            // arrows
            leftArrow.setOnClickListener { Toast.makeText(requireContext(), "Left arrow clicked", Toast.LENGTH_SHORT).show() }
            rightArrow.setOnClickListener { Toast.makeText(requireContext(), "Right arrow clicked", Toast.LENGTH_SHORT).show() }

            // play/pause toggle
            playButton.setOnClickListener {
                if (timerRunning) {
                    // user paused -> stop session and add session to accumulated time
                    val sessionMs = chronometerStop()
                    sessionCount++
                    persistTotals()
                    // update UI showing total (accumulated includes the just-ended session)
                    updateStudyTimeLabel(getCurrentAccumulatedMs())
                    // push totals to Firebase (send hours)
                    courseCode?.let { code ->
                        val hours = getCurrentAccumulatedMs().toDouble() / 3_600_000.0
                        FirebaseService.updateTime(code, hours, "Fundamentals", 0)
                        FirebaseService.updateDayStudyTime(code, hours)
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

            // fetch notes/tests if course provided (left as-is)
            if (!courseCode.isNullOrEmpty()) {
                // fetchNotes(courseCode!!)
                // fetchTestSummary(courseCode!!)
            }
        }

        // ------------------------
        // TIMER (global, timestamp-based)
        // ------------------------

        /**
         * Start a new session.
         * Save wall-clock start timestamp so the session keeps counting if the app is backgrounded / killed.
         */
        private fun chronometerStart() {
            val startTsMs = System.currentTimeMillis()
            timerStartTsMs = startTsMs
            prefs.edit()
                .putBoolean("timer_running", true)
                .putLong("timer_start_ts", startTsMs)
                .apply()

            // For UI display: set chronometer.base such that chronometer shows elapsed = now - startTsMs
            val elapsedSinceStart = System.currentTimeMillis() - startTsMs // typically ~0 at immediate start
            chronometer.base = SystemClock.elapsedRealtime() - elapsedSinceStart
            chronometer.start()

            timerRunning = true

            // update study label in case you want to show accumulated + live
            updateStudyTimeLabel(getCurrentAccumulatedMs())
        }

        /**
         * Stop the current session, return session duration (ms).
         * Adds session duration into courseTimeMs and clears running flag + persisted start ts.
         */
        private fun chronometerStop(): Long {
            chronometer.stop()

            val startTs = prefs.getLong("timer_start_ts", 0L)
            val nowMs = System.currentTimeMillis()
            val sessionMs = if (startTs > 0L) nowMs - startTs else (SystemClock.elapsedRealtime() - chronometer.base).toLong()

            // accumulate into stored total
            courseTimeMs += sessionMs

            // clear running flag & persisted start ts, save accumulated totals
            prefs.edit()
                .putBoolean("timer_running", false)
                .remove("timer_start_ts")
                .putLong("course_time_ms", courseTimeMs)
                .putInt("session_count", sessionCount)
                .apply()

            // reset chronometer display to 0 (base set to now so next start looks fresh)
            chronometer.base = SystemClock.elapsedRealtime()

            timerRunning = false
            timerStartTsMs = 0L

            return sessionMs
        }

        /**
         * Restore timer state when the fragment is created/resumed.
         * If a session was running, compute elapsed = now - startTs and start Chronometer UI to show that elapsed.
         * If not running, set chronometer to 0 and update study label from persisted totals.
         */
        private fun restoreTimerState() {
            courseTimeMs = prefs.getLong("course_time_ms", 0L)
            sessionCount = prefs.getInt("session_count", 0)
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
                timerStartTsMs = startTs
            } else {
                chronometer.stop()
                chronometer.base = SystemClock.elapsedRealtime()
                timerRunning = false
                timerStartTsMs = 0L
            }

            // Update total time label using the accumulated courseTimeMs plus running session if any
            updateStudyTimeLabel(getCurrentAccumulatedMs())
        }

        /**
         * Return total accumulated milliseconds (stored total + if a session is running, include its elapsed).
         */
        private fun getCurrentAccumulatedMs(): Long {
            return if (prefs.getBoolean("timer_running", false) && prefs.getLong("timer_start_ts", 0L) > 0L) {
                val start = prefs.getLong("timer_start_ts", 0L)
                val now = System.currentTimeMillis()
                courseTimeMs + (now - start)
            } else {
                courseTimeMs
            }
        }

        /**
         * Persist totals locally (courseTimeMs and sessionCount).
         */
        private fun persistTotals() {
            prefs.edit()
                .putLong("course_time_ms", courseTimeMs)
                .putInt("session_count", sessionCount)
                .apply()
        }

        override fun onPause() {
            super.onPause()

            // Persist totals locally
            persistTotals()

            // If timer is running we keep it logically running (start_ts is already saved on start).
            // Upload current accumulated total (including running session) to Firebase as hours.
            courseCode?.let { code ->
                val totalMs = getCurrentAccumulatedMs()
                val hours = totalMs.toDouble() / 3_600_000.0
                FirebaseService.updateTime(code, hours, "Fundamentals", 0)
                FirebaseService.updateDayStudyTime(code, hours)
            }
        }

        override fun onResume() {
            super.onResume()
            // ensure UI shows correct state when fragment returns to foreground
            restoreTimerState()
        }

        // --------------- Utility ---------------

        /**
         * Update the HH:MM:SS label using totalMs (milliseconds).
         * totalMs should represent the total time you want shown (accumulated + running if desired).
         */
        private fun updateStudyTimeLabel(totalMs: Long) {
            val totalSeconds = totalMs / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            studyTimeView.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }

        // --------------- Test dialogs (kept as you previously had) ---------------

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

                val selectedTopics = mutableListOf<String>()
                for (i in 0 until topicsContainer.childCount) {
                    val checkBox = topicsContainer.getChildAt(i) as CheckBox
                    if (checkBox.isChecked) selectedTopics.add(checkBox.text.toString())
                }

                FirebaseService.updateTest(courseCode, testName, newName)
                FirebaseService.setGrade(courseCode, newName, grade)
                for (topic in selectedTopics) FirebaseService.addTopic(courseCode, newName, topic)

                Toast.makeText(requireContext(), "Test updated", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }

            cancelButton.setOnClickListener { dialog.dismiss() }
            dialog.show()
        }

        // --------------- Placeholders ---------------

        private fun fetchNotes(code: String) {
            // keep your existing implementation (PDF icons etc.)
        }

        private fun fetchTestSummary(code: String) {
            // keep your existing implementation or call FirebaseService.getTestTopics(...)
        }

        // Utility dp
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
