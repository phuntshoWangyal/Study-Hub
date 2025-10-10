package ca.unb.mobiledev.studyhub

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
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
}
