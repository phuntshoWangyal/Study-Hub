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
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.lang.Exception


class MainPage : AppCompatActivity(),AddCourseFragment.AddCourseDialogListener,
    home_fragment.CourseListProvider{

    private lateinit var courseList: MutableList<Course>


    lateinit var bottomNav : BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_page)


        courseList = CourseStorage.loadCourses(this)




        loadFragment(home_fragment())

        // Bottom Navigation setup
        bottomNav = findViewById(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.classAddButton -> {
                    val dialog = AddCourseFragment()
                    dialog.show(supportFragmentManager, AddCourseFragment.TAG)
                    true
                }
                R.id.settingButton -> {
                    loadFragment(setting_fragment())
                    Log.i("1","button pressed")
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
    override fun getCourseList(): MutableList<Course> {
        return courseList
    }
    override fun onCourseAdded(course: Course) {
        addCourse(course)
        Toast.makeText(this, "Course ${course.courseCode} added!", Toast.LENGTH_LONG).show()
    }
    private fun addCourse(course: Course) {
        courseList.add(course)
        CourseStorage.saveCourses(this, courseList)
        val currentFragment = supportFragmentManager.findFragmentById(R.id.container)

        if (currentFragment is home_fragment) {
            currentFragment.refreshCourseList()
        } else {

            loadFragment(home_fragment())
        }

    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.commit()
    }


}