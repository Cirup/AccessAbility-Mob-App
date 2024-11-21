package com.mco.accessability.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.mco.accessability.databinding.LoginpageBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.mco.accessability.data.SharedViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: LoginpageBinding

    private val sharedViewModel: SharedViewModel by viewModels()

    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = FirebaseFirestore.getInstance()
        binding = LoginpageBinding.inflate(layoutInflater)

        setContentView(binding.root)
        binding.registerHref.setOnClickListener {
            // Start the Register Activity
            finish()
        }

        binding.loginBtn.setOnClickListener {

            // ToDO change email to username
            val username = binding.emailHolder.text.toString()
            val password = binding.passHolder.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            db.collection("users")
                .whereEqualTo("username", username)
                .whereEqualTo("password", password)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
                    } else {
                        // User authenticated successfully
                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                        // Navigate to the next activity or home screen
                        val intent = Intent(this, MapActivity::class.java)
                        startActivity(intent)

                        //startActivity(Intent(this, HomeActivity::class.java))
                        // finish()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("LoginActivity", "Error logging in", e)
                    Toast.makeText(this, "An error occurred. Please try again.", Toast.LENGTH_SHORT).show()
                }
        }
        //replaceFragment(MapFragment(sharedViewModel))

        //login button on click listener
        //login()
        //create account button on click listener
        createAccount()
    }

    private fun createAccount(){
        val textView = binding.registerHref

        //when clicked, it will lead to the register page
        textView.setOnClickListener{
            val intent = Intent(this, CreateAccountActivity::class.java)
            startActivity(intent)
        }
    }

    private fun login(){
        val button = binding.loginBtn

        button.setOnClickListener{
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }
    }


}
