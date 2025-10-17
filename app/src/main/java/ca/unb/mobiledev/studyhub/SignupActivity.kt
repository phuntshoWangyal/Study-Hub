package ca.unb.mobiledev.studyhub

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class SignupActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_main)

        val signupButton : Button = findViewById(R.id.SignUpButton)

        signupButton.setOnClickListener {
            val username = findViewById<EditText>(R.id.usernameTextField).text.toString()
            val email = findViewById<EditText>(R.id.emailField).text.toString()
            val password = findViewById<EditText>(R.id.passwordField).text.toString()
            val password2 = findViewById<EditText>(R.id.password2Field).text.toString()

            if (password == password2){
                FirebaseService.signUp(
                    email,
                    password2,
                    onSuccess = {
                       Log.i("Signing up", "Account was created")
                        FirebaseService.signIn(
                            email,
                            password2,
                            onSuccess = {
                                FirebaseService.timeStamp()
                                FirebaseService.usernameChange(username)
                                FirebaseService.studyTime()
                                FirebaseService.verifyEmail()
                            },
                            onError = { error ->
                                Log.e("Logging in", "Logging in failed")
                            }
                        )

                        val intent = Intent(this@SignupActivity, LoginPage::class.java)
                        startActivity(intent)
                    },
                    onError = { error ->
                        Log.e("Signing up", "Account was not created")
                    }
                )
            }
        }
        val loginButton : Button = findViewById(R.id.loginButton)
        loginButton.setOnClickListener{
            val intent = Intent(this@SignupActivity, LoginPage::class.java)
            startActivity(intent)

        }
    }



}