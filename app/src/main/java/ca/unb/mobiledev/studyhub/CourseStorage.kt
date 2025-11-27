package ca.unb.mobiledev.studyhub

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

object CourseStorage {
    private fun fileNameForUser(uid: String) = "courses_$uid.txt"

    fun saveCourses(context: Context, uid: String, courses: List<Course>) {
        try {
            val file = File(context.filesDir, fileNameForUser(uid))
            ObjectOutputStream(FileOutputStream(file)).use { it.writeObject(courses) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadCourses(context: Context, uid: String): MutableList<Course> {
        val file = File(context.filesDir, fileNameForUser(uid))
        if (!file.exists()) return mutableListOf()
        return try {
            ObjectInputStream(FileInputStream(file)).use { @Suppress("UNCHECKED_CAST") it.readObject() as MutableList<Course> }
        } catch (e: Exception) {
            e.printStackTrace()
            mutableListOf()
        }
    }

    fun clearCourses(context: Context, uid: String) {
        saveCourses(context, uid, mutableListOf())
    }
}
