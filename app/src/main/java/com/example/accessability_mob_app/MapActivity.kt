package com.example.accessability_mob_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class MapActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mappage)

        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            // Go back to MainActivity
            finish()
        }
    }
}
