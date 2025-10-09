package ca.unb.mobiledev.studyhub

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.EditText
import java.sql.Connection

class LoginPage : AppCompatActivity(), ConnectionCallback {

    private var connection: Connection? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_page)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        DatabaseManager.connect(this)



        val activityTwoButton: Button = findViewById(R.id.loginButton)
        activityTwoButton.setOnClickListener {
            val name = findViewById<EditText>(R.id.nameField).text.toString()
            val getNameStatement = "SELECT * FROM users WHERE name = ?"

            val statement = connection?.prepareStatement(getNameStatement)
            statement?.setString(1, name)

            val result = statement?.executeQuery()

            if(result != null && result.next()) {
                Log.e("Name check", "Name found")
            }
            else{
                Log.e("Name check", "Name was not found")
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
    override fun onConnected(connection: Connection) {
        this.connection = connection
        Log.d("LoginActivity", "Connected to MySQL")

    }
}