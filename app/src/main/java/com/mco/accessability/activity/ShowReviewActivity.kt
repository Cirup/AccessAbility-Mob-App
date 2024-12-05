package com.mco.accessability.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mco.accessability.R
import com.mco.accessability.adapter.ShowReviewAdapter
import com.mco.accessability.databinding.ShowReviewLayoutBinding
import com.mco.accessability.fragment.ProfileFragment
import com.mco.accessability.models.CombinedReviewModel
import com.mco.accessability.models.MarkerData
import com.mco.accessability.models.ReviewModel

class ShowReviewActivity : AppCompatActivity() {
    private lateinit var binding: ShowReviewLayoutBinding
    private lateinit var reviewAdapter: ShowReviewAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ShowReviewLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up OnBackPressedCallback
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })

        // Initialize the adapter
        val combinedReviewList = mutableListOf<CombinedReviewModel>()
        reviewAdapter = ShowReviewAdapter(combinedReviewList)

        // Add Divider
        val decoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        binding.reviewRecycler.addItemDecoration(decoration)

        // Set up RecyclerView
        binding.reviewRecycler.layoutManager = LinearLayoutManager(this)
        binding.reviewRecycler.adapter = reviewAdapter

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val currentUser = auth.currentUser
        val currentUserEmail = currentUser?.email ?: "Anonymous"

        Log.d("ShowReviewActivity", "Current User Email: $currentUserEmail")

        fetchReviews(currentUserEmail)
    }

    private fun fetchReviews(currentUserEmail: String) {
        getUsernameFromFirestore(currentUserEmail) { username ->
            if (username == null) {
                Log.e("ShowReviewActivity", "No username found for email: $currentUserEmail")
                Toast.makeText(this, "Unable to retrieve user details", Toast.LENGTH_SHORT).show()
                return@getUsernameFromFirestore
            }

            Log.d("ShowReviewActivity", "Username retrieved: $username")

            // First, get user's reviews
            firestore.collection("review")
                .whereEqualTo("author", username)
                .get()
                .addOnSuccessListener { reviewsSnapshot ->
                    val userReviews = reviewsSnapshot.documents.map { document ->
                        val reviewModel = document.toObject(ReviewModel::class.java)
                        // Attach the snapshot ID to the review
                        reviewModel?.id = document.id
                        reviewModel
                    }.filterNotNull()

                    Log.d("ShowReviewActivity", "Reviews count: ${userReviews.size}")
                    userReviews.forEachIndexed { index, review ->
                        Log.d("ShowReviewActivity", "Review $index - ID: ${review.id}, Notes: ${review.notes}, Author: ${review.author}")
                    }

                    // Then, get all markers
                    firestore.collection("marker")
                        .get()
                        .addOnSuccessListener { markersSnapshot ->
                            val markers = markersSnapshot.toObjects(MarkerData::class.java)

                            Log.d("ShowReviewActivity", "Markers count: ${markers.size}")
                            markers.forEachIndexed { index, marker ->
                                Log.d("ShowReviewActivity", "Marker $index - Place: ${marker.nameOfPlace}, Notes: ${marker.notes}")
                            }

                            // Create combined review list
                            val combinedReviewList = userReviews.flatMap { review ->
                                Log.d("ShowReviewActivity", "Current Review ID: ${review.id}")
                                Log.d("ShowReviewActivity", "Current Review Notes: ${review.notes}")
                                Log.d("ShowReviewActivity", "Current Review Author: ${review.author}")

                                Log.d("ShowReviewActivity", "Total Markers Count: ${markers.size}")

                                markers.forEachIndexed { index, marker ->
                                    Log.d("ShowReviewActivity", "Marker $index Details:")
                                    Log.d("ShowReviewActivity", "Marker Notes: ${marker.notes}")
                                    Log.d("ShowReviewActivity", "Marker Place Name: ${marker.nameOfPlace}")
                                }

                                val matchingMarkers = markers.filter { marker ->
                                    val isMatching = marker.notes.contains(review.id)

                                    Log.d("ShowReviewActivity", "Checking Marker:")
                                    Log.d("ShowReviewActivity", "Marker Notes: ${marker.notes}")
                                    Log.d("ShowReviewActivity", "Review ID: ${review.id}")
                                    Log.d("ShowReviewActivity", "Matching Result: $isMatching")

                                    isMatching
                                }

                                Log.d("ShowReviewActivity", "Matching Markers Count: ${matchingMarkers.size}")




                                matchingMarkers.map { marker ->

                                    val averageRating = if (marker.ratings.isNotEmpty()) {
                                        marker.ratings.map { it.rating }.average().toFloat()
                                    } else {
                                        0f
                                    }

                                    CombinedReviewModel(
                                        placeName = marker.nameOfPlace,
                                        placeRating = averageRating,
                                        reviewNotes = review.notes,
                                        author = review.author
                                    )
                                }
                            }

                            Log.d("ShowReviewActivity", "Combined Reviews count: ${combinedReviewList.size}")

                            // Update the adapter on the main thread
                            runOnUiThread {
                                reviewAdapter.updateList(combinedReviewList)
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("ShowReviewActivity", "Error fetching markers", e)
                            Toast.makeText(this, "Failed to load reviews", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Log.e("ShowReviewActivity", "Error fetching reviews", e)
                    Toast.makeText(this, "Failed to load reviews", Toast.LENGTH_SHORT).show()
                }
        }
    }



    // Username retrieval method remains the same
    private fun getUsernameFromFirestore(email: String, callback: (String?) -> Unit) {
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

//    override fun onBackPressed() {
//        // Check if there are fragments in the back stack
//        if (supportFragmentManager.backStackEntryCount > 0) {
//            // Pop the last fragment from the back stack
//            supportFragmentManager.popBackStack()
//        } else {
//            // No fragments in the back stack, finish the activity
//            super.onBackPressed()
//        }
//    }


}