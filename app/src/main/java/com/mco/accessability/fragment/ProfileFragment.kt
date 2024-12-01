package com.mco.accessability.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mco.accessability.R
import com.mco.accessability.activity.LoginActivity
import com.mco.accessability.activity.ShowReviewActivity
import com.mco.accessability.databinding.FragmentProfileBinding
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var profileBinding: FragmentProfileBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        profileBinding = FragmentProfileBinding.inflate(layoutInflater)
        val view = profileBinding.root

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()


        val currentUser = auth.currentUser
        val currentUserEmail = currentUser?.email ?: "Anonymous"

        getUsernameFromFirestore(currentUserEmail) { username ->
            if (username == null) {
                Toast.makeText(requireContext(), "Failed to retrieve username", Toast.LENGTH_SHORT)
                    .show()
                return@getUsernameFromFirestore
            } else {
                profileBinding.profileUsername.text = username.toString()
            }
        }

        profileBinding.profileEmail.text = currentUserEmail

        profileBinding.logoutButtonLayout.setOnClickListener {
            logoutUser()
        }

        profileBinding.reviewButtonLayout.setOnClickListener{
            if (currentUser != null) { // if already logged in go to maps
                val intent = Intent(requireContext(), ShowReviewActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            } else { // if not logged in go to login page
                val intent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
        }

        return view
    }

    fun getUsernameFromFirestore(email: String, callback: (String?) -> Unit) {
        firestore.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val username = querySnapshot.documents.firstOrNull()?.getString("username")
                Log.d("ShowReviewActivity", "Username query result: $username")
                callback(username)
            }
            .addOnFailureListener {
                Log.e("ShowReviewActivity", "Error fetching username", it)
                callback(null)
            }
    }

    private fun logoutUser() {
        try {
            auth.signOut()
            Toast.makeText(requireContext(), "Logged out successfully!", Toast.LENGTH_SHORT).show()
            navigateToLoginScreen()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error logging out: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun navigateToLoginScreen() {
        val intent = Intent(requireContext(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }


}