package com.mco.accessability.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.mco.accessability.databinding.ItemNotesLayoutBinding
import com.mco.accessability.models.ReviewModel
import android.util.Log
import android.widget.ImageButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.mco.accessability.R

class DialogViewHolder(private val viewBinding: ItemNotesLayoutBinding): RecyclerView.ViewHolder(viewBinding.root) {
    private val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val isLoggedIn = currentUser?.uid != null

    fun bindData(notes: ReviewModel) {
        val reviewRef = db.collection("review").document(notes.id)

        // Listen for real-time updates on this review
        reviewRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("DialogViewHolder", "Failed to listen for updates", error)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val updatedNotes = snapshot.toObject(ReviewModel::class.java)
                if (updatedNotes != null) {
                    // Update UI with the latest upvotes and downvotes
                    updateVoteButtonIcons(
                        viewBinding.btnUpvote,
                        viewBinding.btnDownvote,
                        currentUser?.uid,
                        updatedNotes.upvotes,
                        updatedNotes.downvotes
                    )

                    // Calculate and update vote count
                    val totalVotes = updatedNotes.upvotes.size - updatedNotes.downvotes.size
                    viewBinding.voteCount.text = totalVotes.toString()
                }
            }
        }

        // Existing code for setting up the buttons
        this.viewBinding.author.text = notes.author
        this.viewBinding.notes.text = notes.notes
        this.viewBinding.userImage.setImageResource(notes.imageId)

        viewBinding.btnUpvote.setOnClickListener {
            if (isLoggedIn) {
                handleVote(notes, currentUser?.uid!!, isUpvote = true)
            }
        }

        viewBinding.btnDownvote.setOnClickListener {
            if (isLoggedIn) {
                handleVote(notes, currentUser?.uid!!, isUpvote = false)
            }
        }
    }



    private fun updateVoteButtonIcons(
        btnUpvote: ImageButton,
        btnDownvote: ImageButton,
        currentUserId: String?,
        upvotes: List<String>,
        downvotes: List<String>
    ) {
        // Determine the states
        val isUpvoted = currentUserId != null && upvotes.contains(currentUserId)
        val isDownvoted = currentUserId != null && downvotes.contains(currentUserId)

        // Update the icons based on states
        btnUpvote.setImageResource(if (isUpvoted) R.drawable.upvote_on else R.drawable.upvote_off)
        btnDownvote.setImageResource(if (isDownvoted) R.drawable.downvote_on else R.drawable.downvote_off)
    }

    private fun handleVote(review: ReviewModel, userId: String, isUpvote: Boolean) {
        val reviewRef = db.collection("review").document(review.id)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(reviewRef)
            if (!snapshot.exists()) {
                Log.d("DialogViewHolder", "Review not found")
                return@runTransaction
            }

            val upvotes = snapshot["upvotes"] as? List<String> ?: listOf()
            val downvotes = snapshot["downvotes"] as? List<String> ?: listOf()

            // Remove user from both lists
            val updatedUpvotes = upvotes.toMutableList().apply { remove(userId) }
            val updatedDownvotes = downvotes.toMutableList().apply { remove(userId) }

            // Toggle vote based on current state
            if (isUpvote) {
                if (!upvotes.contains(userId)) {
                    updatedUpvotes.add(userId) // Add upvote if not already upvoted
                }
            } else {
                if (!downvotes.contains(userId)) {
                    updatedDownvotes.add(userId) // Add downvote if not already downvoted
                }
            }

            // Update Firebase
            transaction.update(reviewRef, mapOf(
                "upvotes" to updatedUpvotes,
                "downvotes" to updatedDownvotes
            ))
        }.addOnSuccessListener {
            Log.d("DialogViewHolder", "Vote updated successfully")
        }.addOnFailureListener { e ->
            Log.e("DialogViewHolder", "Error updating vote", e)
        }
    }


}

