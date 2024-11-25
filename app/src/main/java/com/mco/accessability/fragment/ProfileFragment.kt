package com.mco.accessability.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.mco.accessability.R
import com.mco.accessability.activity.LoginActivity

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Set click listener for the logout button
        view.findViewById<View>(R.id.logoutButtonLayout).setOnClickListener {
            logoutUser()
        }

        return view
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