package ca.unb.mobiledev.studyhub

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class RankingPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.ranking)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.ranking_page)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        val homeButton: ImageButton = findViewById(R.id.home_button)
        homeButton.setOnClickListener {
            val intent = Intent(this@RankingPage, MainPage::class.java)
            startActivity(intent)
        }

        val settingsButton: Button = findViewById(R.id.settingsButton)
        settingsButton.setOnClickListener {
            val intent = Intent(this@RankingPage, SettingsPage::class.java)
            startActivity(intent)
        }

    }
}