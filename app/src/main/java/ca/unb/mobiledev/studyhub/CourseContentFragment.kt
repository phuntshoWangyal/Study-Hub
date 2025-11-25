package ca.unb.mobiledev.studyhub

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Chronometer
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlin.properties.Delegates

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
    }


    override fun onPause(){
        super.onPause()
        FirebaseService.updateTime(courseCode!!, courseTime/3600000)
        FirebaseService.updateDayStudyTime(courseCode!!, courseTime/3600000)
    }

    private fun fetchNotes(code: String) {
        db.collection("courses").document(code)
            .collection("notes")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(12)
            .get()
            .addOnSuccessListener { snap ->
                pdfRow.removeAllViews()
                if (snap.isEmpty) {
                    val tv = TextView(requireContext()).apply { text = "No notes yet" }
                    pdfRow.addView(tv)
                    return@addOnSuccessListener
                }

                for (doc in snap.documents) {
                    val title = doc.getString("title") ?: "PDF"
                    val icon = ImageView(requireContext()).apply {
                        layoutParams = LinearLayout.LayoutParams(dp(30), dp(30)).also {
                            (it as ViewGroup.MarginLayoutParams).marginEnd = dp(12)
                        }
                        setImageResource(R.drawable.pdf_icon)
                        contentDescription = title
                        setOnClickListener {
                            Toast.makeText(requireContext(), "Open $title", Toast.LENGTH_SHORT).show()
                        }
                    }
                    pdfRow.addView(icon)
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load notes", Toast.LENGTH_SHORT).show()
            }
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
