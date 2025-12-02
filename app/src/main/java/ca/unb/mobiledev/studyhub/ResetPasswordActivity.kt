package ca.unb.mobiledev.studyhub

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var emailField: EditText
    private lateinit var sendButton: Button
    private lateinit var infoText: TextView

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        emailField = findViewById(R.id.resetEmailField)
        sendButton = findViewById(R.id.sendResetButton)
        infoText = findViewById(R.id.resetInfoText)

        sendButton.setOnClickListener {
            val email = emailField.text.toString().trim()

            if (email.isEmpty()) {
                emailField.error = "Enter your email"
                return@setOnClickListener
            }

            sendButton.isEnabled = false
            sendButton.alpha = 0.7f
            infoText.visibility = View.GONE

            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    sendButton.isEnabled = true
                    sendButton.alpha = 1f

                    if (task.isSuccessful) {
                        infoText.text = "If an account exists for this email, you'll receive a reset link shortly."
                        infoText.visibility = View.VISIBLE
                        Toast.makeText(
                            this,
                            "Password reset email sent",
                            Toast.LENGTH_SHORT
                        ).show()

                    } else {
                        val message = task.exception?.localizedMessage ?: "Something went wrong"
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
