package ca.unb.mobiledev.studyhub

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SignupActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_main)

        val errorText: TextView = findViewById(R.id.errorText)
        val signupButton : Button = findViewById(R.id.SignUpButton)

        signupButton.setOnClickListener {
            val username = findViewById<EditText>(R.id.usernameTextField).text.toString()
            val email = findViewById<EditText>(R.id.emailField).text.toString()
            val password = findViewById<EditText>(R.id.passwordField).text.toString()
            val password2 = findViewById<EditText>(R.id.password2Field).text.toString()

            if (password == password2 && !(password.length < 6)){
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
                            },
                            onError = { error ->
                                Log.e("Logging in", "Logging in failed")
                            }
                        )

                        val intent = Intent(this@SignupActivity, LoginPage::class.java)
                        startActivity(intent)
                    },
                    onError = { error ->
                        errorText.text = "Entered email is not valid"
                        errorText.visibility = View.VISIBLE
                        Log.e("Signing up", "Account was not created")
                    }
                )
            }
            else{
                if(password == ""){
                    errorText.text = "Entered password was empty"
                }
                else if(password.length < 6){
                    errorText.text = "Password should be at least 6 symbols long"
                }
                else{
                    errorText.text = "Passwords do not match"
                }
                errorText.visibility = View.VISIBLE
            }
        }
        val loginButton : Button = findViewById(R.id.loginButton)
        loginButton.setOnClickListener{
            val intent = Intent(this@SignupActivity, LoginPage::class.java)
            startActivity(intent)

        }
    }



}