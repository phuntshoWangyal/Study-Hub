package ca.unb.mobiledev.studyhub

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.lang.Exception


class MainPage : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CourseAdapter
    private lateinit var courseList: MutableList<Course>


    lateinit var bottomNav : BottomNavigationView

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
        loadFragment(home_fragment())

        // Bottom Navigation setup
        bottomNav = findViewById(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.classAddButton -> {
                    recyclerView.visibility = View.VISIBLE
                    loadFragment(home_fragment())
                    true
                }
                R.id.settingButton -> {
                    recyclerView.visibility = View.GONE
                    loadFragment(setting_fragment())
                    Log.i("1","button pressed")
                    true
                }
                R.id.rankingButton -> {
                    recyclerView.visibility = View.GONE
                    loadFragment(rank_fragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun addCourse(course: Course) {
        courseList.add(course)
        CourseStorage.saveCourses(this, courseList)
        adapter.notifyDataSetChanged()
        // Load default fragment

    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.commit()
    }



}