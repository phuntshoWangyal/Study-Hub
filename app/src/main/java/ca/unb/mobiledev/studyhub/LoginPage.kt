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
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ca.unb.mobiledev.studyhub.AddCourseFragment.AddCourseDialogListener
import com.google.firebase.FirebaseApp


private lateinit var courseList: List<Course>
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
        passwordField.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
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
                        Log.i("Logging in", "Authentification success")
                        val intent = Intent(this@LoginPage, MainPage::class.java)
                        val list: List<String>
                        FirebaseService.getCourseList { list ->
                            Log.i("list", list.toString())
                            courseList = CourseStorage.loadCourses(this)
                            val courseCodes = courseList.map { it.courseCode }
                            var newCourse: Course
                            val courseList: MutableList<Course> = mutableListOf()
                            var pending = list.count { it !in courseCodes }
                            var name: String
                            if(list.isEmpty()){
                                startActivity(intent)
                            }else if (pending == 0) {
                                startActivity(intent)
                            }
                            else{
                                for (code in list) {
                                    if(code !in courseCodes){
                                        FirebaseService.getCourseName(code){ name ->
                                            Log.i("Checking name", name)
                                            newCourse = Course(code, name)
                                            courseList.add(newCourse)
                                            Log.i("New list", courseList.toString())

                                            pending--
                                            if (pending == 0) {
                                                CourseStorage.saveCourses(this, courseList)
                                                startActivity(intent)
                                            }
                                        }
                                    }
                                }
                            }
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
        val signupButton : Button = findViewById(R.id.SignUpButton)

        signupButton.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }
}