package ca.unb.mobiledev.studyhub

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class SignupActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_main)

        val loginButton: Button = findViewById(R.id.LogInButton)
        loginButton.setOnClickListener {
            val intent = Intent(this@SignupActivity, LoginPage::class.java)
            startActivity(intent)
        }
    }
}