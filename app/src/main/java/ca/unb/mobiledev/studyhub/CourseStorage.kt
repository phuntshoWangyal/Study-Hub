package ca.unb.mobiledev.studyhub

import android.content.Context
import java.io.*

object CourseStorage {

    private fun getFileName(userId: String): String {
        return if (userId == "guest") {
            "courses_guest.txt"
        } else {
            "courses_${userId}.txt"
        }
    }

    fun saveCourses(context: Context, userId: String, courses: List<Course>) {
        try {
            val file = File(context.filesDir, getFileName(userId))
            ObjectOutputStream(FileOutputStream(file)).use { oos ->
                oos.writeObject(courses)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearCourses(context: Context, userId: String) {
        saveCourses(context, userId, emptyList())
    }

    fun loadCourses(context: Context, userId: String): MutableList<Course> {
        val file = File(context.filesDir, getFileName(userId))
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
