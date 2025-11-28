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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import java.lang.Exception


class MainPage : AppCompatActivity(),AddCourseFragment.AddCourseDialogListener,
    home_fragment.CourseListProvider{

    private lateinit var courseList: MutableList<Course>
    //Keep track of current fragment
    private var currentFragmentTag: String = "home"

    lateinit var bottomNav : BottomNavigationView
    private val db = FirebaseFirestore.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_page)


        courseList = CourseStorage.loadCourses(this)

        // Bottom Navigation setup
        bottomNav = findViewById(R.id.bottomNav)
        loadFragment(home_fragment(),"home")

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.classAddButton -> {
                    if (currentFragmentTag == "home") {
                        val dialog = AddCourseFragment()
                        dialog.show(supportFragmentManager, AddCourseFragment.TAG)
                    } else {
                        loadFragment(home_fragment(), "home")
                    }
                    true
                }
                R.id.settingButton -> {
                    loadFragment(setting_fragment(), "settings")
                    true
                }
                R.id.rankingButton -> {
                    loadFragment(rank_fragment(), "ranking")
                    true
                }
                else -> false
            }
        }
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true) // <-- This is the key setting
            .build()
        db.firestoreSettings = settings

    }
    private fun updateAddButtonIcon() {
        val menuItem = bottomNav.menu.findItem(R.id.classAddButton)
        if (currentFragmentTag == "home") {
            menuItem.setIcon(R.drawable.ic_add)
        } else {
            menuItem.setIcon(R.drawable.ic_home)
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

            loadFragment(home_fragment(), "home")
        }
    }
    fun openCourseContent(course: Course) {
        val fragment = CourseContentFragment.newInstance(
            course.courseCode,
            course.courseName
        )
        loadFragment(fragment, "courseContent")
    }

    private fun loadFragment(fragment: Fragment, tag: String) {
        currentFragmentTag = tag
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment, tag)
        transaction.commit()
        updateAddButtonIcon()
    }

    fun updateCourse(oldCode: String, newCode: String, newName: String) {
        val index = courseList.indexOfFirst { it.courseCode == oldCode }
        if (index == -1) return

        // Optional: prevent duplicate codes
        if (oldCode != newCode && courseList.any { it.courseCode == newCode }) {
            Toast.makeText(this, "Course code already exists", Toast.LENGTH_SHORT).show()
            return
        }

        // update in memory
        courseList[index].courseCode = newCode
        courseList[index].courseName = newName

        // persist to file
        CourseStorage.saveCourses(this, courseList)

        // refresh home fragment
        val homeFrag = supportFragmentManager.findFragmentByTag("home") as? home_fragment
        homeFrag?.refreshCourseList()
    }

    fun deleteCourse(code: String) {
        val removed = courseList.removeAll { it.courseCode == code }
        if (removed) {
            CourseStorage.saveCourses(this, courseList)
            loadFragment(home_fragment(), "home")

            val homeFrag = supportFragmentManager.findFragmentByTag("home") as? home_fragment
            homeFrag?.refreshCourseList()
        }
    }


}