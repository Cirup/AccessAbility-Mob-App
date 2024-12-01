package com.mco.accessability.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.mco.accessability.activity.LoginActivity
import com.mco.accessability.databinding.LoginpageBinding

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth // Firebase Authentication instance

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) { // if already logged in go to maps
            val intent = Intent(this, MapActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("USER_ID", currentUser.uid)
                putExtra("EMAIL", currentUser.email)
            }
            startActivity(intent)
            finish()
        } else { // if not logged in go to login page
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
    }
}
