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

        val studyTime = "00:00:00"
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

    fun createCourse(name: String){
        val uid = auth.currentUser?.uid
        val ref = realtimeDb.getReference("users/$uid/Courses")
        val userData = mapOf(name to name)
        val reference = realtimeDb.getReference("users/$uid/Courses/$name")
        val hours = 0.0
        val data = mapOf("StudiedTime" to hours)
        ref.updateChildren(userData)
        reference.updateChildren(data)
    }

    fun updateCourse(name: String){
        val uid = auth.currentUser?.uid
        val ref = realtimeDb.getReference("users/$uid/Courses")
        val userData = mapOf(name to name)
        ref.updateChildren(userData)
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

    fun updateTime(name: String, timeAdd: Double){
        val uid = auth.currentUser?.uid
        val ref = realtimeDb.getReference("users/$uid/Courses/$name/StudiedTime")
        ref.get().addOnSuccessListener { snapshot ->
            var time = snapshot.getValue(Double::class.java) ?: 0.0
            time += timeAdd
            val reference = realtimeDb.getReference("users/$uid/Courses/$name")
            val userData = mapOf("StudiedTime" to time)
            reference.updateChildren(userData)
        }.addOnFailureListener { e ->
            Log.e("Time of Course", e.toString())
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
