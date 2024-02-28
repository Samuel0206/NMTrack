package com.project.tracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.project.tracker.databinding.ActivityFeedbackBinding

data class Feedback(
    val userId: String?,
    val username: String?,
    val email: String?,
    val feedback: String
)

class ActivityFeedback : AppCompatActivity() {
    private val view: ActivityFeedbackBinding by lazy { ActivityFeedbackBinding.inflate(layoutInflater) }
    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(view.root)

        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val currentUser = mAuth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid
            val username = currentUser.displayName
            val email = currentUser.email

            view.btnSubmit.setOnClickListener {
                val feedbackText = view.etFeedback.text.toString()

                if (feedbackText.isEmpty()) {
                    Toast.makeText(this, "You are not able to Submit Empty", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val feedbackEntry = Feedback(userId, username, email, feedbackText)
                createFeedback(feedbackEntry)
            }
        } else {
            // User is not authenticated, handle accordingly
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createFeedback(feedbackEntry: Feedback) {
        // Assuming "feedbacks" is the node in your Firebase Realtime Database
        val feedbacksRef = database.reference.child("feedbacks")

        // Create a unique key for each feedback entry
        val feedbackKey = feedbacksRef.push().key

        // Push the feedback entry to the "feedbacks" node with the unique key
        feedbackKey?.let {
            feedbacksRef.child(it).setValue(feedbackEntry)
                .addOnSuccessListener {
                    Toast.makeText(this, "Feedback submitted successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to submit feedback", Toast.LENGTH_SHORT).show()
                    Log.e("FeedbackSubmission", "Error submitting feedback", exception)
                }
        } ?: run {
            Toast.makeText(this, "Failed to generate feedback key", Toast.LENGTH_SHORT).show()
            Log.e("FeedbackSubmission", "Failed to generate feedback key")
        }
    }
}