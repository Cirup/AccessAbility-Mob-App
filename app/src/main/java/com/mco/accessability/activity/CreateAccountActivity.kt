package com.mco.accessability.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.mco.accessability.databinding.RegisterpageBinding
import org.mindrot.jbcrypt.BCrypt

class CreateAccountActivity : AppCompatActivity() {

    private lateinit var binding: RegisterpageBinding

    // initialize firebase db and authentication
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = RegisterpageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginHref.setOnClickListener{
            finish()
        }

        // submit data to Firestore Database
        binding.registerBtn.setOnClickListener{
            // data to be passed in the firestore
            val username = binding.usernameTv.text.toString().trim()
            val email = binding.emailHolder.text.toString().trim()
            val password = binding.passHolder.text.toString()
            val confirmPassword = binding.conPassHolder.text.toString()
            val profileImg = 0

            // Check if all fields are filled
            if (username.isEmpty() || email.isEmpty() || binding.passHolder.text.isEmpty() || binding.conPassHolder.text.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if passwords match
            if (password != confirmPassword) {
                Toast.makeText(this, "Retyped password doesn't match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Firebase email validation regex
            val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
            if (!email.matches(emailPattern.toRegex())) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Password strength validation
            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check for duplicate username
            validateUsername(username,
                onSuccess = {
                    // If username is valid, proceed to register user with Firebase Auth
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful){
                                val user = auth.currentUser
                                saveUserInfoToFirestore(user?.uid, username.toString(), email.toString(), profileImg)
                                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()

                                finish()
                            } else {
                                Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }

                    finish()
                },
                onFailure = { errorMessage ->
                    // Display error if username is already taken
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                })


        }

    }
    private fun saveUserInfoToFirestore(uid: String?, username: String, email: String, profileImg: Int) {
        if (uid == null) return

        val user = hashMapOf(
            "username" to username,
            "email" to email,
            "profileImg" to profileImg,
            "createdAt" to System.currentTimeMillis()
        )


        db.collection("users").document(uid)
            .set(user)
            .addOnSuccessListener {
                Log.d("CreateAccountActivity", "Document ID: $uid")
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save user info: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun validateUsername(username: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // Username does not exist, proceed with registration
                    onSuccess()
                } else {
                    // Username already exists
                    onFailure("Username is already taken. Please choose another one.")
                }
            }
            .addOnFailureListener { e ->
                // Handle database query errors
                onFailure("Error validating username: ${e.message}")
            }
    }
}