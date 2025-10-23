package ca.unb.mobiledev.studyhub

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

object CourseStorage {
    private val FILE_NAME = "courses.txt"

    fun saveCourses(context: Context, courses: List<Course>) {
        try {
            val file = File(context.filesDir, FILE_NAME)
            ObjectOutputStream(FileOutputStream(file)).use { oos ->
                oos.writeObject(courses)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadCourses(context: Context): MutableList<Course> {
        val file = File(context.filesDir, FILE_NAME)
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