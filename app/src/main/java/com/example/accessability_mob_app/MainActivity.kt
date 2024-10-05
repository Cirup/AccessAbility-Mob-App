package com.example.accessability_mob_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homescreen)

        val mapsButton = findViewById<Button>(R.id.mapsButton)
        mapsButton.setOnClickListener {
            // Navigate to MapActivity
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }
    }
}
