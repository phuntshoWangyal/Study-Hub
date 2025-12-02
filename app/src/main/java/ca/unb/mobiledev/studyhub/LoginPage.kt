package ca.unb.mobiledev.studyhub

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.FirebaseApp

class LoginPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login_page)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_page)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val errorText: TextView = findViewById(R.id.errorText)
        val passwordField: EditText = findViewById(R.id.passwordLoginField)
        passwordField.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        FirebaseApp.initializeApp(this)

        val loginButton: Button = findViewById(R.id.loginButton)
        loginButton.setOnClickListener {
            errorText.visibility = View.INVISIBLE
            loginButton.isEnabled = false
            loginButton.alpha = 0.7f
            val email = findViewById<EditText>(R.id.emailLoginField).text.toString()
            val password = findViewById<EditText>(R.id.passwordLoginField).text.toString()
            if(email != "" && password != ""){
                FirebaseService.signIn(
                    email,
                    password,
                    onSuccess = {
                        Log.i("Logging in", "Authentication success")

                        val intent = Intent(this@LoginPage, MainPage::class.java)

                        // after sign-in, UID is available
                        val uid = FirebaseService.auth.currentUser?.uid ?: "guest"

                        FirebaseService.getCourseList { remoteCodes ->
                            Log.i("list", remoteCodes.toString())

                            // load existing local courses for THIS user
                            val existingCourses = CourseStorage.loadCourses(this, uid)
                            val existingCodes = existingCourses.map { it.courseCode }

                            val mergedCourses = existingCourses.toMutableList()

                            // how many new courses we need to add locally
                            val newCodes = remoteCodes.filter { it !in existingCodes }

                            if (newCodes.isEmpty()) {
                                // nothing new to sync, just go to main page
                                startActivity(intent)
                            } else {
                                // create Course objects using code as both code + name (for now)
                                for (code in newCodes) {
                                    val newCourse = Course(code, code) // name = code
                                    mergedCourses.add(newCourse)
                                }

                                // save merged list and go to main page
                                CourseStorage.saveCourses(this, uid, mergedCourses)
                                startActivity(intent)
                            }
                            val intent = Intent(this, MainPage::class.java)
                            startActivity(intent)
                            finish()
                        }

                    },
                    onError = { error ->
                        errorText.text = "Entered Username or Password is incorrect"
                        errorText.visibility = View.VISIBLE
                        loginButton.isEnabled = true
                        loginButton.alpha = 1f
                        Log.e("Logging in", "Authentification fail")
                    }
                )
            }
        }

        val signupButton: Button = findViewById(R.id.SignUpButton)
        signupButton.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        val forgotPasswordButton: Button = findViewById(R.id.forgotPasswordButton)
        forgotPasswordButton.setOnClickListener {
            val intent = Intent(this, ResetPasswordActivity::class.java)
            startActivity(intent)
        }
    }

}