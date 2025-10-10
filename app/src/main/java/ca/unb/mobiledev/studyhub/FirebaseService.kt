package ca.unb.mobiledev.studyhub

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.database.FirebaseDatabase

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

}
