package ca.unb.mobiledev.studyhub

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
import com.google.firebase.firestore.Query
import androidx.appcompat.app.AlertDialog
import android.widget.EditText



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



    private lateinit var topicMoreButton: ImageView



    private var courseCode: String? = null
    private var courseName: String? = null

    private var courseTime: Double = 0.0
    private val db = FirebaseFirestore.getInstance()



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


        courseCodeView.text = courseCode ?: "Course Code"
        topicNameView.text = courseName ?: "Topic name"
        studyTimeView.text = "Session Study Time: 00:00:00"
        if (!courseCode.isNullOrEmpty()) {
            fetchTestSummary(courseCode!!)
        }



        leftArrow.setOnClickListener {
            Toast.makeText(requireContext(), "Left arrow clicked", Toast.LENGTH_SHORT).show()
        }
        rightArrow.setOnClickListener {
            Toast.makeText(requireContext(), "Right arrow clicked", Toast.LENGTH_SHORT).show()
        }
        playButton.setOnClickListener {
            if(timerStarted){
                courseTime = courseTime + chronometerStop()
                hours = (courseTime/3600000).toInt()
                minutes = ((courseTime - hours*3600000) / 60000).toInt()
                seconds = ((courseTime - hours*3600000 - minutes*60000)/1000).toInt()
                val hoursStr = String.format("%02d", hours)
                val minStr = String.format("%02d", minutes)
                val secStr = String.format("%02d", seconds)
                studyTimeView.text = "Session Study Time: $hoursStr:$minStr:$secStr"
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

                // update local state + UI
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

    override fun onPause(){
        super.onPause()
        FirebaseService.updateTime(courseCode!!, courseTime/3600000, "Fundamentals", 0)
        FirebaseService.updateDayStudyTime(courseCode!!, courseTime/3600000)
    }

    private fun fetchTestSummary(code: String) {
        db.collection("courses").document(code)
            .collection("tests")
            .limit(1)
            .get()
            .addOnSuccessListener { snap ->
                if (snap.isEmpty) {
                    testTitleView.text = "No tests yet"
                    testTopicsView.text = ""
                    return@addOnSuccessListener
                }

                val doc = snap.documents.first()
                val name = doc.getString("name") ?: "Test"
                val topics = (doc.get("topics") as? List<*>)?.joinToString(", ") ?: "—"

                testTitleView.text = name
                testTopicsView.text = "Includes $topics"
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load tests", Toast.LENGTH_SHORT).show()
            }
    }

    private fun chronometerStart(){
        chronometer.base = SystemClock.elapsedRealtime()
        chronometer.start()
    }

    private fun chronometerStop(): Int{
        chronometer.stop()
        val timeStudied = SystemClock.elapsedRealtime() - chronometer.base
        return timeStudied.toInt()
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
}
