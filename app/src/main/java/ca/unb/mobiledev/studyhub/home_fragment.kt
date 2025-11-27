package ca.unb.mobiledev.studyhub

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.lang.ClassCastException

import androidx.appcompat.app.AlertDialog

class home_fragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CourseAdapter

    private lateinit var techniqueRecyclerView: RecyclerView
    private lateinit var techniqueAdapter: StudyTechniqueAdapter

    // Interface to communicate with the hosting Activity (MainPage)
    interface CourseListProvider {
        fun getCourseList(): MutableList<Course>
    }

    private lateinit var courseListProvider: CourseListProvider

    override fun onAttach(context: android.content.Context) {
        super.onAttach(context)
        // Ensure the hosting Activity implements the CourseListProvider interface
        try {
            courseListProvider = context as CourseListProvider
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement CourseListProvider")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)

        val courseList = courseListProvider.getCourseList()

        adapter = CourseAdapter(courseList) { selectedCourse ->
            (activity as? MainPage)?.openCourseContent(selectedCourse)
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        techniqueRecyclerView = view.findViewById(R.id.techniqueRecyclerView)

        val techniques = listOf(
            StudyTechnique(
                "Pomodoro Technique",
                "25-minute focus sessions",
                "The Pomodoro Technique is a time management method where you break work into 25-minute focused intervals, called \"Pomodoros,\" separated by short breaks. To use it, you first select a task, set a timer for 25 minutes, and then work with intense focus on only that task until the timer rings. Once your Pomodoro is complete, you take a short, 5-minute break to rest your mind, and after completing four Pomodoros, you take a longer, more restorative break of 15–30 minutes before starting the cycle again. This structure helps improve concentration, reduce mental fatigue, and break large tasks into manageable segments.",
                R.drawable.pomodoro   // image for Pomodoro

            ),
            StudyTechnique(
                "The Feynman Technique",
                "Explaining a concept simply to reveal and close knowledge gaps.",
                "now the The Feynman Technique\n" +
                        "\n" +
                        "The Feynman Technique is a powerful method for deep learning that relies on the idea that if you can't explain a concept simply, you don't truly understand it. The process has four main steps: first, write down the topic you want to learn on a blank piece of paper; second, explain the concept in simple language as if you were teaching it to a child, avoiding jargon and using your own words; third, identify any gaps or shaky points in your explanation where you got stuck or had to use complex terms, and then go back to the source material to fill in those gaps; and finally, simplify and refine your explanation until it is clear, concise, and easy for anyone to understand, demonstrating a solid mastery of the material.",
                R.drawable.feynman
            )
        )

        techniqueAdapter = StudyTechniqueAdapter(techniques) { technique ->
            showTechniqueDetailsDialog(technique)
        }

        techniqueRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        techniqueRecyclerView.adapter = techniqueAdapter

        return view
    }
    private fun showTechniqueDetailsDialog(technique: StudyTechnique) {
        AlertDialog.Builder(requireContext())
            .setTitle(technique.title)
            .setMessage(technique.description)
            .setPositiveButton("OK", null)
            .show()
    }

    /**
     * Public method for the Activity to call to refresh the course list display
     */
    fun refreshCourseList() {
        adapter.notifyDataSetChanged()
    }
}