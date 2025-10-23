package ca.unb.mobiledev.studyhub

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainPage : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CourseAdapter
    private lateinit var courseList: MutableList<Course>
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.lang.Exception

class MainPage : AppCompatActivity() {

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
    }

    private fun addCourse(course: Course) {
        courseList.add(course)
        CourseStorage.saveCourses(this, courseList)
        adapter.notifyDataSetChanged()
        // Load default fragment
        loadFragment(home_fragment())

        // Bottom Navigation setup
        bottomNav = findViewById(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.classAddButton -> {
                    loadFragment(home_fragment())
                    true
                }
                R.id.settingButton -> {
                    loadFragment(setting_fragment())
                    true
                }
                R.id.rankingButton -> {
                    loadFragment(rank_fragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.commit()
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