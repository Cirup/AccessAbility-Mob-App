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
            val username = binding.usernameTv.text
            val email = binding.emailHolder.text
            val profileImg = 0

            // Check if all fields are filled
            if (username.isEmpty() || email.isEmpty() || binding.passHolder.text.isEmpty() || binding.conPassHolder.text.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if passwords match
            if (binding.passHolder.text != binding.conPassHolder.text) {
                Toast.makeText(this, "Retyped password doesn't match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Add firebase authentication
            auth.createUserWithEmailAndPassword(email.toString(), binding.passHolder.toString())
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
        }

    }
    private fun saveUserInfoToFirestore(uid: String?, username: String, email: String, profileImg: Int) {
        if (uid == null) return

        val user: MutableMap<String, Any?> = HashMap()
        user["username"] = username.toString()
        user["email"] = email.toString()
        user["profileImg"] = profileImg

        db.collection("users").document(uid)
            .set(user)
            .addOnSuccessListener {
                Log.d("CreateAccountActivity", "Document ID: $uid")
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save user info: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}