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
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = LoginpageBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        // Initialize FirebaseAuth
//        auth = FirebaseAuth.getInstance()
//
//        // Register hyperlink on click
//        binding.registerHref.setOnClickListener {
//            val intent = Intent(this, CreateAccountActivity::class.java)
//            startActivity(intent)
//        }
//
//        // Login button click listener
//        binding.loginBtn.setOnClickListener {
//            val email = binding.emailHolder.text.toString()
//            val password = binding.passHolder.text.toString()
//
//            if (email.isEmpty() || password.isEmpty()) {
//                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//
//            // Validate login credentials using Firebase Authentication
//             Log.d("LoginDebug", "Email: $email, Password: $password")
//
//            loginUser(email, password)
//        }
//    }
//
//    // Login user with Firebase Authentication
//    private fun loginUser(email: String, password: String) {
//
//        auth.signInWithEmailAndPassword(email, password)
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    // Login successful
//                    val user = auth.currentUser
//
//                    // Save user information in SharedPreferences
//                    val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
//                    val editor = sharedPreferences.edit()
//                    editor.putString("email", user?.email)
//                    editor.putBoolean("is_logged_in", true)
//                    editor.clear()
//                    editor.apply()
//
//                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
//
//                    // Navigate to the home screen
//                    navigateToHomeScreen(user?.uid, user?.email)
//                } else {
//                    // Handle errors
//                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
//                }
//            }
//    }
//
//    // Navigate to the home screen (MapActivity)
//    private fun navigateToHomeScreen(userId: String?, email: String?) {
//        val intent = Intent(this, MapActivity::class.java)
//        intent.putExtra("USER_ID", userId)
//        intent.putExtra("EMAIL", email)
//        startActivity(intent)
//        finish() // Close the login activity so user cannot go back
//    }
}
