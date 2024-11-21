package com.mco.accessability.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.mco.accessability.databinding.RegisterpageBinding

class CreateAccountActivity : AppCompatActivity() {

    private lateinit var binding: RegisterpageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // initialize db
        val db = FirebaseFirestore.getInstance()

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
            val password = binding.passHolder.text
            val profileImg = 0

            val user: MutableMap<String, Any?> = HashMap()
            user["username"] = username.toString()
            user["email"] = email.toString()
            user["password"] = password.toString()
            user["profileImg"] = profileImg

            // TODO: set the right values for success handling messages
            // TODO: create TAG companion object (standard)
            db.collection("users")
                .add(user)
                .addOnSuccessListener { documentReference ->
                    Log.d("CreateAccountActivity", "Document ID: " + documentReference.id)
                }
                .addOnFailureListener{ e -> Log.w("CreateAccountActivity", "Error adding document", e) }
            finish()
        }

    }
}