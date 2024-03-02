package com.project.tracker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.project.tracker.databinding.ActivityModifyPasswordBinding

class ModifyPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityModifyPasswordBinding
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModifyPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnUpdatePassword.setOnClickListener {
            val users = FirebaseAuth.getInstance().currentUser
            if (users != null) {
                val currentEmail = users.email.toString()
                val uid = users.uid
                databaseReference = FirebaseDatabase.getInstance().getReference("users")
                val passwordReference = databaseReference.child(uid).child("password")

                databaseReference.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            val currentPassword = dataSnapshot.child("password").getValue(String::class.java)
                            Log.d("Test Current Email", currentEmail)
                            Log.d("Test Current Password", currentPassword.toString())
                            Log.d("Test Old password typed", binding.etOldPassword.text.toString())

                            if (currentPassword.toString() == binding.etOldPassword.text.toString()) {
                                val newPassword = binding.etNewPassword.text.toString()

                                // Update the password manually in the database
                                passwordReference.setValue(newPassword).addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(
                                            this@ModifyPasswordActivity,
                                            "Password Update Successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        Log.d("Test New Password", newPassword)
                                        val credential = EmailAuthProvider.getCredential(currentEmail, newPassword)
//                                        users.reauthenticate(credential).addOnCompleteListener {
//                                        }
//                                    } else {
//                                        Toast.makeText(
//                                            this@ModifyPasswordActivity,
//                                            "Password Update Failed: ${task.exception?.message}",
//                                            Toast.LENGTH_SHORT
//                                        ).show()
//                                    }
                                    } else {
                                        Toast.makeText(
                                            this@ModifyPasswordActivity,
                                            "Password Update Failed: ${task.exception?.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            } else {
                                Toast.makeText(
                                    this@ModifyPasswordActivity,
                                    "Please Enter Correct old password!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle error
                    }
                })
            }
        }
    }
}



//class ModifyPasswordActivity : AppCompatActivity() {
//    private lateinit var binding: ActivityModifyPasswordBinding
//    private lateinit var databaseReference: DatabaseReference
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityModifyPasswordBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        binding.btnUpdatePassword.setOnClickListener {
//            val users = FirebaseAuth.getInstance().currentUser
//            if (users != null) {
//                val currentEmail = users.email.toString()
//                val uid = users.uid
//                databaseReference = FirebaseDatabase.getInstance().getReference("users")
//                databaseReference.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
//                    override fun onDataChange(dataSnapshot: DataSnapshot) {
//                        if (dataSnapshot.exists()) {
//                            val currentPassword = dataSnapshot.child("password").getValue(String::class.java)
//                            Log.d("Test Current Email", currentEmail)
//                            Log.d("Test Current Password", currentPassword.toString())
//                            Log.d("Test Old password typed", binding.etOldPassword.text.toString())
//
//                            if (currentPassword.toString() == binding.etOldPassword.text.toString()) {
//                                val newPassword = binding.etNewPassword.text.toString()
//                                Log.d("Test New Password", newPassword)
//                                // Update the password
//                                users.updatePassword(newPassword).addOnCompleteListener { task ->
//                                    if (task.isSuccessful) {
//                                        Toast.makeText(
//                                            this@ModifyPasswordActivity,
//                                            "Password Update Successfully",
//                                            Toast.LENGTH_SHORT
//                                        ).show()
//                                        // Reauthenticate after updating the password
//                                        val credential = EmailAuthProvider.getCredential(currentEmail, newPassword)
//                                        users.reauthenticate(credential).addOnCompleteListener {
//                                        }
//                                    } else {
//                                        Toast.makeText(
//                                            this@ModifyPasswordActivity,
//                                            "Password Update Failed: ${task.exception?.message}",
//                                            Toast.LENGTH_SHORT
//                                        ).show()
//                                    }
//                                }
//                            } else {
//                                Toast.makeText(
//                                    this@ModifyPasswordActivity,
//                                    "Please Enter Correct old password!",
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                            }
//                        }
//                    }
//
//                    override fun onCancelled(error: DatabaseError) {
//                        // Handle error
//                    }
//                })
//            }
//        }
//    }
//}


