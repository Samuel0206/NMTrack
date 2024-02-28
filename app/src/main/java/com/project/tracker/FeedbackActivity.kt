package com.project.tracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.project.tracker.fragment.UserPageFragment

class FeedbackActivity : AppCompatActivity() {

    private lateinit var etFeedback: EditText
    private lateinit var btnSubmitFB: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        // Initialize views and Firebase components
        etFeedback = findViewById(R.id.etFeedback)
        btnSubmitFB = findViewById(R.id.btnSubmitFB)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        btnSubmitFB.setOnClickListener {
            val feedbackText = etFeedback.text.toString().trim()
            if (feedbackText.isNotEmpty()) {
                sendFeedbackToFirebase(feedbackText)
            }
        }
    }

    private fun sendFeedbackToFirebase(feedback: String) {
        // Get current user information
        val currentUser = auth.currentUser
        val userId = currentUser?.uid ?: "" 
        val email = currentUser?.email ?: ""

        // Create a reference to the "feedbacks" node in the database
        val feedbacksRef = database.reference.child("feedbacks")

        // Create a unique key for the new feedback
        val newFeedbackKey = feedbacksRef.push().key

        if (newFeedbackKey != null) {
            // Construct the feedback data
            val feedbackData = hashMapOf(
                "userId" to userId,
                "email" to email,
                "feedback" to feedback
            )

            // Store the feedback in the database
            feedbacksRef.child(newFeedbackKey).setValue(feedbackData)

            Toast.makeText(this, "Submit success! ", Toast.LENGTH_SHORT).show()
            // 注册成功后跳转回登录页
            startActivity(Intent(this, UserPageFragment::class.java))
            finish() // 结束当前页面
        } else {
            Toast.makeText(this, "Submit failed", Toast.LENGTH_SHORT).show()
        }


    }
}