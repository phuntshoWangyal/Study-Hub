package ca.unb.mobiledev.studyhub

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
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

        val passwordField: EditText = findViewById(R.id.passwordLoginField)
        passwordField.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        FirebaseApp.initializeApp(this)

        val loginButton: Button = findViewById(R.id.loginButton)
        loginButton.setOnClickListener {
            val email = findViewById<EditText>(R.id.emailLoginField).text.toString()
            val password = findViewById<EditText>(R.id.passwordLoginField).text.toString()
            if(email != "" && password != ""){
                FirebaseService.signIn(
                    email,
                    password,
                    onSuccess = {
                        Log.i("Logging in", "Authentification success")
                        val intent = Intent(this@LoginPage, MainPage::class.java)
                        startActivity(intent)
                    },
                    onError = { error ->
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