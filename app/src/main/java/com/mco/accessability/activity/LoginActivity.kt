package com.mco.accessability.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.mco.accessability.databinding.LoginpageBinding

class LoginActivity:AppCompatActivity() {
    private lateinit var binding: LoginpageBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginpageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.registerHref.setOnClickListener {
            startActivity(Intent(this, CreateAccountActivity::class.java))
        }

        binding.loginBtn.setOnClickListener {
            val email = binding.emailHolder.text.toString().trim()
            val password = binding.passHolder.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Show loading indicator if you have one
            // binding.progressBar.visibility = View.VISIBLE

            loginUser(email, password)
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                // Hide loading indicator if you have one
                // binding.progressBar.visibility = View.GONE

                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    navigateToHomeScreen(user?.uid, user?.email)
                } else {
                    val errorMessage = when (task.exception) {
                        is FirebaseAuthInvalidCredentialsException -> "Invalid email or password"
                        is FirebaseAuthInvalidUserException -> "No account found with this email"
                        else -> "Login failed: ${task.exception?.message}"
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun navigateToHomeScreen(userId: String?, email: String?) {
        val intent = Intent(this, MapActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("USER_ID", userId)
            putExtra("EMAIL", email)
        }
        startActivity(intent)
        finish()
    }
}