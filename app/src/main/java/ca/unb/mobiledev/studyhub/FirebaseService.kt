package ca.unb.mobiledev.studyhub

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Date
import java.util.Locale

object FirebaseService {
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    val realtimeDb: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }

    fun signUp(email: String, password: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun signIn(email: String, password: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun signOut(){
        auth.signOut()
    }

    fun timeStamp(){
        val uid = auth.currentUser?.uid
        val ref = realtimeDb.getReference("users/$uid")

        val currentDate = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date())
        Log.e("Current Date", currentDate)
        val userData = mapOf("joinedDate" to currentDate)

        ref.updateChildren(userData)

    }//createdAt

    fun studyTime(){
        val uid = auth.currentUser?.uid
        val ref = realtimeDb.getReference("users/$uid")
        val studyTime = 0.0
        val userData = mapOf("studyTime" to studyTime)
        ref.updateChildren(userData)
    }

    fun usernameChange(username: String){
        val uid = auth.currentUser?.uid
        val ref = realtimeDb.getReference("users/$uid")

        val userData = mapOf("username" to username)

        ref.updateChildren(userData)
    }
    fun passwordChange(password: String) {
        val user = auth.currentUser

        user?.updatePassword(password)?.addOnSuccessListener {
            Log.i("Password", "Password was changed")
        }
            ?.addOnFailureListener {
            Log.e("Password", "Password was not changed")
        }
    }

    fun getDate(callback: (String?) -> Unit){
        val uid = auth.currentUser?.uid
        val ref = realtimeDb.getReference("users/$uid/joinedDate")

        ref.get().addOnSuccessListener { snapshot ->
            val date = snapshot.getValue(String::class.java)
            callback(date)
        }.addOnFailureListener {
            Log.e("Date check", "Could not receive date from database")
            callback(null)
        }
    }

    fun getName(callback: (String?) -> Unit){
        val uid = auth.currentUser?.uid
        val ref = realtimeDb.getReference("users/$uid/username")

        ref.get().addOnSuccessListener { snapshot ->
            val name = snapshot.getValue(String::class.java)
            callback(name)
        }.addOnFailureListener {
            Log.e("Username", "Could not receive date from database")
            callback(null)
        }
    }

    fun getTotalTime(callback: (Double) -> Unit){
        val uid = auth.currentUser?.uid
        val ref = realtimeDb.getReference("users/$uid/studyTime")

        ref.get().addOnSuccessListener { snapshot ->
            val time = snapshot.getValue(Double::class.java) ?: 0.0
            callback(time)
        }.addOnFailureListener {
            Log.e("Username", "Could not receive date from database")
            callback(0.0)
        }
    }

    fun getEmail(callback: (String?) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            val email = user.email
            callback(email)
        } else {
            Log.e("Email", "Could not receive data from database")
            callback(null)
        }
    }

    fun verifyEmail(){
        val user = auth.currentUser
        user?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.i("EmailVerification", "Verification email sent.")
                } else {
                    Log.e("EmailVerification", "Failed to send email.")
                }
            }
    }

    fun createCourse(code: String, name: String){
        val uid = auth.currentUser?.uid
        val ref = realtimeDb.getReference("users/$uid/Courses")
        val userData = mapOf(code to code)
        val reference = realtimeDb.getReference("users/$uid/Courses/$code")
        val data = mapOf("StudiedTime" to 0.0)
        ref.updateChildren(userData)
        reference.updateChildren(data)
        val reference2 = realtimeDb.getReference("users/$uid/Courses/$code")
        val data2 = mapOf("CourseName" to name)
        reference2.updateChildren(data2)
    }

    fun getCourseName(code: String, callback: (String) -> Unit){
        val uid = auth.currentUser?.uid
        val ref = realtimeDb.getReference("users/$uid/Courses/$code/CourseName")

        ref.get().addOnSuccessListener { snapshot ->
            val name = snapshot.getValue(String::class.java)
            callback(name!!)
        }.addOnFailureListener {
            Log.e("Getting course code", "Could not receive time from database")
            callback("Something went wrong")
        }
    }

    fun getTopics(courseCode: String, callback: (List<String>) -> Unit) {
        val uid = auth.currentUser?.uid
        val coursesRef = FirebaseDatabase.getInstance()
            .getReference("users/$uid/Courses/$courseCode/Topics")
        coursesRef.get()
            .addOnSuccessListener { snapshot ->
                val courseNames = mutableListOf<String>()
                for (snapshot in snapshot.children) {
                    courseNames.add(snapshot.key!!)
                }
                callback(courseNames)
            }
            .addOnFailureListener { e ->
                Log.e("Topics Fetch", "Failed to fetch courses")
            }
    }

    fun getTests(courseCode: String, callback: (List<String>) -> Unit) {
        val uid = auth.currentUser?.uid
        val coursesRef = FirebaseDatabase.getInstance()
            .getReference("users/$uid/Courses/$courseCode/Tests")
        coursesRef.get()
            .addOnSuccessListener { snapshot ->
                val courseNames = mutableListOf<String>()
                for (snapshot in snapshot.children) {
                    courseNames.add(snapshot.key!!)
                }
                callback(courseNames)
            }
            .addOnFailureListener { e ->
                Log.e("Tests Fetch", "Failed to fetch courses")
            }
    }

    fun createTest(courseName: String, testName: String){
        val uid = auth.currentUser?.uid
        val ref = realtimeDb.getReference("users/$uid/Courses/$courseName/Tests/$testName")
        val userData = mapOf("Grade" to 0.0)
        ref.updateChildren(userData)
    }

    fun createTopic(courseName: String, topicName: String) {
        val uid = auth.currentUser?.uid
        val ref = realtimeDb.getReference("users/$uid/Courses/$courseName/Topics/$topicName")
        val userData = mapOf("timeOfStudy" to 0.0)
        ref.updateChildren(userData)
    }

    fun addTopic(courseName: String, testName: String, topicName: String){
        val uid = auth.currentUser?.uid
        val ref = realtimeDb.getReference("users/$uid/Courses/$courseName/Tests/$testName/Topics")
        val userData = mapOf(topicName to topicName)
        ref.updateChildren(userData)
    }

    fun updateTest(name: String, testName: String, newTestName: String){
        val uid = auth.currentUser?.uid
        val ref = realtimeDb.getReference("users/$uid/Courses/$name/Tests/$testName")
        ref.setValue(newTestName)
    }

    fun updateTopic(name: String, topicName: String, newTopicName: String){
        val uid = auth.currentUser?.uid
        val ref = realtimeDb.getReference("users/$uid/Courses/$name/Tests/$topicName")
        ref.setValue(newTopicName)
    }

    fun setGrade(courseName:String, testName: String, grade: Double){
        val uid = auth.currentUser?.uid
        val ref = realtimeDb.getReference("users/$uid/Courses/$courseName/Tests/$testName")
        val userData = mapOf("Grade" to grade)
        ref.updateChildren(userData)
    }

    fun deleteTest(courseName: String, testName: String){
        val uid = auth.currentUser?.uid ?: return
        val ref = realtimeDb.getReference("users/$uid/Courses/$courseName/Tests/$testName")
        ref.removeValue()
            .addOnSuccessListener {
                Log.i("Test Deleting", "Course was deleted from database")
            }
            .addOnFailureListener { e ->
                Log.e("Test deleting", e.toString())
            }
    }


    fun deleteTopic(courseName: String, topicName: String){
        val uid = auth.currentUser?.uid ?: return
        val ref = realtimeDb.getReference("users/$uid/Courses/$courseName/Topics/$topicName")
        ref.removeValue()
            .addOnSuccessListener {
                Log.i("Test Deleting", "Course was deleted from database")
            }
            .addOnFailureListener { e ->
                Log.e("Test deleting", e.toString())
            }
    }

    fun deleteTopicFromTest(courseName: String, topicName: String, testName: String){
        val uid = auth.currentUser?.uid ?: return
        val ref = realtimeDb.getReference("users/$uid/Courses/$courseName/Tests/$testName/Topics/$topicName")
        ref.removeValue()
            .addOnSuccessListener {
                Log.i("Test Deleting", "Course was deleted from database")
            }
            .addOnFailureListener { e ->
                Log.e("Test deleting", e.toString())
            }
    }

    fun updateCourse(name: String){
        val uid = auth.currentUser?.uid
        val ref = realtimeDb.getReference("users/$uid/Courses")
        val userData = mapOf(name to name)
        ref.updateChildren(userData)
    }



    fun getCourseList(callback: (List<String>) -> Unit){
        val uid = auth.currentUser?.uid
        val coursesRef = FirebaseDatabase.getInstance()
            .getReference("users/$uid/Courses")

        coursesRef.get()
            .addOnSuccessListener { snapshot ->
                val courseNames = mutableListOf<String>()
                for (snapshot in snapshot.children) {
                    courseNames.add(snapshot.key!!)
                }
                callback(courseNames)
            }
            .addOnFailureListener { e ->
                Log.e("Course Fetch", "Failed to fetch courses")
            }
    }


    fun getWeeklyTime(name: String, year: String, week: String, callback: (List<Double>) -> Unit){
        val uid = auth.currentUser?.uid
        val ref = realtimeDb.getReference("users/$uid/Courses/$name/$year/$week")


        ref.get().addOnSuccessListener { snapshot ->
            val list = mutableListOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)

            val dayIndex = mapOf(
                "Sunday" to 0,
                "Monday" to 1,
                "Tuesday" to 2,
                "Wednesday" to 3,
                "Thursday" to 4,
                "Friday" to 5,
                "Saturday" to 6
            )

            for (snapshot in snapshot.children) {
                val dayName = snapshot.key
                val value = snapshot.getValue(Double::class.java)?: 0.0
                val index = dayIndex[dayName]
                if (index != null) {
                    list[index] = value
                }
            }
            Log.i("list", list.toString())
            callback(list)
        }.addOnFailureListener {
            Log.e("Getting time", "Could not receive time from database")
            callback(emptyList())
        }

    }

    fun getCourseTime(name: String?, callback: (Double) -> Unit){
        if(name == null){
            callback(0.0)
            return
        }
        val uid = auth.currentUser?.uid
        val ref = realtimeDb.getReference("users/$uid/Courses/$name/StudiedTime")

        ref.get().addOnSuccessListener { snapshot ->
            val time = snapshot.getValue(Double::class.java)
            callback(time!!)
        }.addOnFailureListener {
            Log.e("Getting time", "Could not receive time from database")
            callback(0.0)
        }
    }

    fun updateTime(name: String, timeAdd: Double, topic: String, technique: Int){
        val uid = auth.currentUser?.uid
        val ref = realtimeDb.getReference("users/$uid/Courses/$name/StudiedTime")
        ref.get().addOnSuccessListener { snapshot ->
            var time = snapshot.getValue(Double::class.java) ?: 0.0
            time += timeAdd
            val reference = realtimeDb.getReference("users/$uid/Courses/$name")
            val userData = mapOf("StudiedTime" to time)
            reference.updateChildren(userData)
            updateUserTime(timeAdd)
            updateTopicTime(name, timeAdd, topic, technique)
        }.addOnFailureListener { e ->
            Log.e("Time of Course", e.toString())
        }
    }

    fun updateUserTime(timeAdd: Double){
        val uid = auth.currentUser?.uid
        val ref = realtimeDb.getReference("users/$uid/studyTime")
        ref.get().addOnSuccessListener { snapshot ->
            var time = snapshot.getValue(Double::class.java) ?: 0.0
            time += timeAdd
            val ref2 = realtimeDb.getReference("users/$uid")
            val userData = mapOf("studyTime" to time)
            ref2.updateChildren(userData)
        }
    }
    fun updateTopicTime(name: String, timeAdd: Double, topic: String, technique: Int){
        val uid = auth.currentUser?.uid
        val technique1 = technique.toString()
        val ref = realtimeDb.getReference("users/$uid/Courses/$name/Topics/$topic/$technique1")
        ref.get().addOnSuccessListener { snapshot ->
            var time = snapshot.getValue(Double::class.java) ?: 0.0
            time += timeAdd
            ref.setValue(time)
        }
    }

    fun updateDayStudyTime(name: String, timeAdd: Double){
        val year = getCurrentYear()
        val week = getCurrentWeek()
        val day = getCurrentDayOfWeek()
        val uid = auth.currentUser?.uid
        val ref = realtimeDb.getReference("users/$uid/Courses/$name/$year/$week/$day")
        ref.get().addOnSuccessListener { snapshot ->
            var time = snapshot.getValue(Double::class.java) ?: 0.0
            time += timeAdd
            val reference = realtimeDb.getReference("users/$uid/Courses/$name/$year/$week/")
            val userData = mapOf(day to time)
            reference.updateChildren(userData)
        }.addOnFailureListener { e ->
            Log.e("Updating day time", e.toString())
        }
    }

    fun deleteCourse(name: String) {
        val uid = auth.currentUser?.uid ?: return
        val ref = realtimeDb.getReference("users/$uid/Courses/$name")
        ref.removeValue()
            .addOnSuccessListener {
                Log.i("Course Deleting", "Course was deleted from database")
            }
            .addOnFailureListener { e ->
                Log.e("Course deleting", e.toString())
            }
    }

    fun getCurrentDayOfWeek(): String {
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getCurrentWeek(): String {
        val sdf = SimpleDateFormat("w", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getCurrentYear(): String {
        val sdf = SimpleDateFormat("yyyy", Locale.getDefault())
        val year = sdf.format(Date())
        return year
    }
}
