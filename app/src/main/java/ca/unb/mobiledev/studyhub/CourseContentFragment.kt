package ca.unb.mobiledev.studyhub

import android.os.Bundle
import android.os.SystemClock
import android.view.*
import android.widget.Chronometer
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
                        true
                    }
                    R.id.optionAddTest -> {
                        Toast.makeText(requireContext(), "Add Test clicked", Toast.LENGTH_SHORT).show()
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
