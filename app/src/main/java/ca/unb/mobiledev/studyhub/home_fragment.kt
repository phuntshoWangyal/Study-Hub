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



    interface CourseListProvider {
        fun getCourseList(): MutableList<Course>
    }

    private lateinit var courseListProvider: CourseListProvider

    override fun onAttach(context: android.content.Context) {
        super.onAttach(context)
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

        return view
    }



    fun refreshCourseList() {
        adapter.notifyDataSetChanged()
    }
}