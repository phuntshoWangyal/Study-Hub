package ca.unb.mobiledev.studyhub

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import java.io.*

object CourseStorage {

    private fun getFileName(): String {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        return if (uid != null) "courses_$uid.txt" else "courses_guest.txt"
    }

    fun saveCourses(context: Context, courses: List<Course>) {
        try {
            val file = File(context.filesDir, getFileName())
            ObjectOutputStream(FileOutputStream(file)).use { oos ->
                oos.writeObject(courses)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearCourses(context: Context) {
        saveCourses(context, emptyList())
    }

    fun loadCourses(context: Context): MutableList<Course> {
        val file = File(context.filesDir, getFileName())
        if (!file.exists()) return mutableListOf()

        return try {
            ObjectInputStream(FileInputStream(file)).use { ois ->
                @Suppress("UNCHECKED_CAST")
                ois.readObject() as MutableList<Course>
            }
        } catch (e: Exception) {
            e.printStackTrace()
            mutableListOf()
        }
    }
}
