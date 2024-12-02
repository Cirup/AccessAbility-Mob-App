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

        // Initialize FirebaseAuth and Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val currentUser = auth.currentUser
        val currentUserEmail = currentUser?.email ?: "Anonymous"

        // Fetch username and profileImg from Firestore
        getUsernameAndProfileImgFromFirestore(currentUserEmail) { username, profileImg ->
            if (username == null) {
                Toast.makeText(requireContext(), "Failed to retrieve username", Toast.LENGTH_SHORT).show()
                return@getUsernameAndProfileImgFromFirestore
            } else {
                profileBinding.profileUsername.text = username
                // Set profile image based on profileImg number
                val imageResId = profileImg ?: R.drawable.placeholder // Replace with your default image resource
                setProfileImage(imageResId)
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

    fun getUsernameAndProfileImgFromFirestore(email: String, callback: (String?, Int?) -> Unit) {
        firestore.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val document = querySnapshot.documents.firstOrNull()
                val username = document?.getString("username")
                val profileImg = document?.getLong("profileImg")?.toInt() ?: 0 // Default to 0 if not found
                Log.d("ProfileFragment", "Username: $username, ProfileImg: $profileImg")
                callback(username, profileImg)
            }
            .addOnFailureListener {
                Log.e("ProfileFragment", "Error fetching username and profileImg", it)
                callback(null, null)
            }
    }

    private fun setProfileImage(profileImg: Int) {
        // Map profileImg number to a drawable resource
        val imageRes = when (profileImg) {
            0 -> R.drawable.placeholder
            1 -> R.drawable.armin
            2 -> R.drawable.levi
            3 -> R.drawable.mikasa
            else -> R.drawable.placeholder // Fallback to default if invalid
        }
        profileBinding.imageView3.setImageResource(imageRes)
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
