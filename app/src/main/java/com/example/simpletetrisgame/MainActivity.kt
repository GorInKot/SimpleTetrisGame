package com.example.simpletetrisgame

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.simpletetrisgame.storage.AppPreferences
import com.google.android.material.snackbar.Snackbar
import org.w3c.dom.Text

class MainActivity : AppCompatActivity() {

    var tvHighScore: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        val btnNewGame: Button = findViewById<Button>(R.id.btn_new_game)
        val btnResetGame: Button = findViewById<Button>(R.id.btn_reset_game)
        val btnExit: Button = findViewById<Button>(R.id.btn_exit)

        tvHighScore = findViewById<TextView>(R.id.tv_high_score)

        btnNewGame.setOnClickListener(this::onBtnNewGame)

        btnResetGame.setOnClickListener(this::onBtnResetGame)

        btnExit.setOnClickListener(this::handleExitEvent)


    }

    fun onBtnNewGame(view: View) {
        val intent = Intent(this, GameActivity::class.java)
        startActivity(intent)
    }

    fun onBtnResetGame(view: View) {
        val preferences = AppPreferences(this)
        preferences.clearHighScore()
        Snackbar.make(view, "Score successfully reset", Snackbar.LENGTH_SHORT).show()
        tvHighScore?.text = "High Score: ${preferences.getHighScore()}"
    }

    fun handleExitEvent(view: View) {
        System.exit(0)
    }
}