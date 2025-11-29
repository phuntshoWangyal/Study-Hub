package ca.unb.mobiledev.studyhub

import android.app.AlertDialog
import android.os.Bundle
import android.os.SystemClock
import android.view.*
import android.widget.Button
import android.widget.CheckBox
import android.widget.Chronometer
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

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
    private lateinit var chronometer: Chronometer

    private val db by lazy { FirebaseFirestore.getInstance() }

    private var courseCode: String? = null
    private var courseName: String? = null
    private var courseTime: Double = 0.0

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

        courseCodeView.text = courseCode ?: "Course Code"
        topicNameView.text = courseName ?: "Topic name"
        studyTimeView.text = "Session Study Time: 00:00:00"

        if (!courseCode.isNullOrEmpty()) {
            fetchNotes(courseCode!!)
            fetchTestSummary(courseCode!!)
        }

        leftArrow.setOnClickListener {
            Toast.makeText(requireContext(), "Left arrow clicked", Toast.LENGTH_SHORT).show()
        }

        rightArrow.setOnClickListener {
            Toast.makeText(requireContext(), "Right arrow clicked", Toast.LENGTH_SHORT).show()
        }

        playButton.setOnClickListener {
            if (timerStarted) {
                courseTime += chronometerStop()
                hours = (courseTime / 3600000).toInt()
                minutes = ((courseTime - hours*3600000) / 60000).toInt()
                seconds = ((courseTime - hours*3600000 - minutes*60000) / 1000).toInt()

                studyTimeView.text = "Session Study Time: %02d:%02d:%02d".format(hours, minutes, seconds)

                timerStarted = false
                playButton.setImageResource(R.drawable.outline_arrow_right_24)
            } else {
                chronometerStart()
                timerStarted = true
                playButton.setImageResource(R.drawable.pause)
            }
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
    private fun showEditTestDialog(courseCode: String, testName: String) {

        val builder = AlertDialog.Builder(requireContext())
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.edit_test_option, null)

        val nameInput = dialogView.findViewById<EditText>(R.id.edit_test_name)
        val gradeInput = dialogView.findViewById<EditText>(R.id.edit_test_grade)
        val topicsContainer = dialogView.findViewById<LinearLayout>(R.id.topics_list_layout)

        val confirmButton = dialogView.findViewById<Button>(R.id.btn_confirm_edit_test)
        val cancelButton = dialogView.findViewById<Button>(R.id.btn_cancel_edit_test)

        // Pre-fill existing name
        nameInput.setText("Enter new test name")

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



    override fun onPause() {
        super.onPause()
        FirebaseService.updateTime(courseCode!!, courseTime/3600000, "Fundamentals", 0)
        FirebaseService.updateDayStudyTime(courseCode!!, courseTime/3600000)
    }

    private fun fetchNotes(code: String) { /* your original code */ }

    private fun fetchTestSummary(code: String) { /* your original code */ }

    private fun chronometerStart(){
        chronometer.base = SystemClock.elapsedRealtime()
        chronometer.start()
    }

    private fun chronometerStop(): Int{
        chronometer.stop()
        return (SystemClock.elapsedRealtime() - chronometer.base).toInt()
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
}
