package com.mco.accessability.Activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.mco.accessability.databinding.RegisterpageBinding

class CreateAccountActivity : AppCompatActivity() {

    private lateinit var binding: RegisterpageBinding

    // Firebase Firestore instance
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RegisterpageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Navigate back to login page
        binding.loginHref.setOnClickListener {
            finish()
        }

        // Handle the registration button click
        binding.registerBtn.setOnClickListener {
            val username = binding.usernameTv.text.toString()
            val email = binding.emailHolder.text.toString()
            val password = binding.passHolder.text.toString()
            val passwordRetype = binding.conPassHolder.text.toString()

            // Check if all fields are filled
            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || passwordRetype.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if passwords match
            if (password != passwordRetype) {
                Toast.makeText(this, "Retyped password doesn't match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // input validation

            // Save the user data locally using SharedPreferences
            saveUserData(username, email, password)

            // Save user data to Firebase Firestore
            saveUserToFirestore(username, email, password)
        }
    }

    // Save user data in SharedPreferences
    private fun saveUserData(username: String, email: String, password: String) {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("username", username)
        editor.putString("email", email)
        editor.putString("password", password)
        editor.putBoolean("is_logged_in", false)  // Not logged in yet
        editor.apply()

        // Show success message and go back to login page
        Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
        finish() // Close the Register Activity and go back to Login Activity
    }

    // Save user data to Firebase Firestore
    private fun saveUserToFirestore(username: String, email: String, password: String) {
        // hash password

        // Create a new user map



        val user = hashMapOf(
            "username" to username,
            "email" to email,
            "password" to password, // In a real app, do not store passwords in plain text!
            "profileImg" to 0
        )

        // Add user to Firestore's "users" collection
        db.collection("users")
            .add(user)
            .addOnSuccessListener {
                Toast.makeText(this, "User added to Firebase", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error adding user: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
