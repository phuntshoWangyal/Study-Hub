package ca.unb.mobiledev.studyhub

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.EditText

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

        val connection = DatabaseManager.connect()

        val activityTwoButton: Button = findViewById(R.id.loginButton)
        activityTwoButton.setOnClickListener {
            val name = findViewById<EditText>(R.id.nameField).text.toString()
            val getNameStatement = "SELECT * FROM users WHERE name = ?"

            val statement = connection?.prepareStatement(getNameStatement)
            statement?.setString(1, name)

            val result = statement?.executeQuery();

            if(result != null && result.next()) {
                Log.e("Name check", "Name found")
            }
            else{
                Log.e("Name check", "Name not found")
            }


            val intent = Intent(this@LoginPage, MainPage::class.java)
            startActivity(intent)
        }
        val signupButton: Button = findViewById(R.id.SignUpButton)
        signupButton.setOnClickListener {
            val intent = Intent(this@LoginPage, MainPage::class.java)
            startActivity(intent)
        }
    }
}