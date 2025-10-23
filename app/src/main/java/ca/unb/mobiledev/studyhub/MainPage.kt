package ca.unb.mobiledev.studyhub

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainPage : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CourseAdapter
    private lateinit var courseList: MutableList<Course>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_page)

        recyclerView = findViewById(R.id.recyclerView)
        courseList = CourseStorage.loadCourses(this)

        courseList.add(Course("CS 2063", "Intro to Mobile App Development"))
        courseList.add(Course("CS 3035", "Building User Interfaces"))


        adapter = CourseAdapter(courseList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun addCourse(course: Course) {
        courseList.add(course)
        CourseStorage.saveCourses(this, courseList)
        adapter.notifyDataSetChanged()
    }

//        val rankingButton: Button = findViewById(R.id.rankingButton)
//        rankingButton.setOnClickListener {
//            val intent = Intent(this@MainPage, RankingPage::class.java)
//            startActivity(intent)
//        }

//        val addClassButton: Button = findViewById(R.id.classAddButton)
//        addClassButton.setOnClickListener {
//
//        }

//        val settingsButton: Button = findViewById(R.id.settingsButton)
//        settingsButton.setOnClickListener {
//            val intent = Intent(this@MainPage, SettingsPage::class.java)
//            startActivity(intent)
//        }

}