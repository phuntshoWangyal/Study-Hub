package ca.unb.mobiledev.studyhub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.lang.ClassCastException

class home_fragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CourseAdapter

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
        // Inflate the layout for this fragment (which must contain a RecyclerView with ID R.id.recyclerView)
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)

        // Get the course list from the Activity via the interface
        val courseList = courseListProvider.getCourseList()

        // Setup RecyclerView and Adapter
        adapter = CourseAdapter(courseList) { selectedCourse ->
            Toast.makeText(context, "Clicked ${selectedCourse.courseCode}", Toast.LENGTH_SHORT).show()
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        return view
    }

    /**
     * Public method for the Activity to call to refresh the course list display
     */
    fun refreshCourseList() {
        adapter.notifyDataSetChanged()
    }
}