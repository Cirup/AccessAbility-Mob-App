package com.mco.accessability.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.mco.accessability.databinding.LoginpageBinding
import org.mindrot.jbcrypt.BCrypt

class MainActivity : AppCompatActivity() {

    private lateinit var binding: LoginpageBinding

    // Firebase Firestore instance
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginpageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Register hyperlink on click
        binding.registerHref.setOnClickListener {
            val intent = Intent(this, CreateAccountActivity::class.java)
            startActivity(intent)
        }

        // Login button click listener
        binding.loginBtn.setOnClickListener {
            val email = binding.emailHolder.text.toString()
            val password = binding.passHolder.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate the login credentials using Firebase Firestore
            validateLoginCredentials(email, password)
        }
    }

    // Validate login credentials with Firebase Firestore
    private fun validateLoginCredentials(email: String, password: String) {
        // Query Firestore to find the user with the entered email
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                } else {
                    // If user is found, check if password matches
                    for (document in documents) {
                        val storedPassword = document.getString("password")

                        if (BCrypt.checkpw(password, storedPassword)) {
                            // Successful login
                            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()

                            // Save the login status in SharedPreferences
                            val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putString("username", document.getString("username"))
                            editor.putString("email", email)
                            editor.putString("password", password)
                            editor.putBoolean("is_logged_in", true)
                            editor.apply()

                            // Navigate to the home screen (MapActivity)
                            navigateToHomeScreen()
                        } else {
                            // Invalid password
                            Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                // Handle any errors that occur during Firestore query
                Toast.makeText(this, "Error fetching user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Navigate to the home screen (MapActivity)
    private fun navigateToHomeScreen() {
        val intent = Intent(this, MapActivity::class.java)
        startActivity(intent)
        finish()  // Close the login activity so user cannot go back
    }
}
