package ca.unb.mobiledev.studyhub

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.main_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_page)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


//        val rankingButton: Button = findViewById(R.id.rankingButton)
//        rankingButton.setOnClickListener {
//            val intent = Intent(this@MainPage, RankingPage::class.java)
//            startActivity(intent)
//        }

//        val addClassButton: Button = findViewById(R.id.classAddButton)
//        addClassButton.setOnClickListener {
//
//        }

//        val settingsButton: Button = findViewById(R.id.settingsButton)
//        settingsButton.setOnClickListener {
//            val intent = Intent(this@MainPage, SettingsPage::class.java)
//            startActivity(intent)
//        }
    }
}