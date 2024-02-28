package com.project.tracker

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.project.tracker.databinding.ActivityEditProfileBinding


class EditProfileActivity : AppCompatActivity() {
    private val view: ActivityEditProfileBinding by lazy { ActivityEditProfileBinding.inflate(layoutInflater) }

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(view.root)

        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference

        // Set the current username
        val currentUsername = intent.getStringExtra("currentUsername")
        val currentEmail = intent.getStringExtra("currentEmail")
        val currentPhone = intent.getStringExtra("currentPhone")

        view.etUsername.hint = currentUsername
        view.etEmail.hint = currentEmail
        view.etPhone.hint = currentPhone

        // Handle confirm button click
        view.btnSubmit.setOnClickListener {
            confirmChanges()
        }
    }


    private fun confirmChanges() {
        val newUsername = if (!view.etUsername.text.isNullOrBlank()) view.etUsername.text.toString() else view.etUsername.hint.toString()
        val newEmail = if (!view.etEmail.text.isNullOrBlank()) view.etEmail.text.toString() else view.etEmail.hint.toString()
        val newPhone = if (!view.etPhone.text.isNullOrBlank()) view.etPhone.text.toString() else view.etPhone.hint.toString()

        updateUserInfoInFirebase(newUsername, newEmail, newPhone) {
            // Set the result with the updated username
            val resultIntent = Intent()
            resultIntent.putExtra("updatedUsername", newUsername)
            resultIntent.putExtra("updatedEmail", newEmail)
            resultIntent.putExtra("updatedPhone", newPhone)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }


    }

    private fun updateUserInfoInFirebase(
        newUsername: String,
        newEmail: String,
        newPhone: String,
        onSuccess: () -> Unit
    ) {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        userId?.let {
            val userRef = FirebaseDatabase.getInstance().reference.child("users").child(userId)

            val updateData = mapOf(
                "username" to newUsername,
                "email" to newEmail,
                "phone" to newPhone
            )

            userRef.updateChildren(updateData)
                .addOnCompleteListener(this@EditProfileActivity) { task ->
                    if (task.isSuccessful) {
                        // Update successful
                        Log.d("EditProfileActivity", "User info updated successfully")
                        onSuccess.invoke() // Invoke the callback when the update is successful
                    } else {
                        // Handle error
                        Log.e("EditProfileActivity", "Failed to update user info", task.exception)
                        Toast.makeText(
                            this@EditProfileActivity,
                            "Failed to update user info",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }


}