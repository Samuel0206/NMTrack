package com.project.tracker

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import android.widget.ToggleButton
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

        val passwordEditText1 = findViewById<EditText>(R.id.etOldPassword)
        val toggleButton1 = findViewById<ToggleButton>(R.id.tbOldPassword)
        val passwordEditText2 = findViewById<EditText>(R.id.etNewPassword)
        val toggleButton2 = findViewById<ToggleButton>(R.id.tbNewPassword)
        val passwordEditText3 = findViewById<EditText>(R.id.etConfirmNewPassword)
        val toggleButton3 = findViewById<ToggleButton>(R.id.tbConfirmNewPassword)


// 设置初始的输入类型为密码
        passwordEditText1.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        passwordEditText2.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        passwordEditText3.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

// 监听ToggleButton的状态改变
        toggleButton1.setOnCheckedChangeListener { _, isChecked ->
            // 根据ToggleButton的状态切换密码输入框的可见性
            passwordEditText1.inputType = if (isChecked) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }

            // 将光标移动到文本末尾
            passwordEditText1.setSelection(passwordEditText1.text.length)
        }

        toggleButton2.setOnCheckedChangeListener { _, isChecked ->
            // 根据ToggleButton的状态切换密码输入框的可见性
            passwordEditText2.inputType = if (isChecked) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }

            // 将光标移动到文本末尾
            passwordEditText2.setSelection(passwordEditText2.text.length)
        }

        toggleButton3.setOnCheckedChangeListener { _, isChecked ->
            // 根据ToggleButton的状态切换密码输入框的可见性
            passwordEditText3.inputType = if (isChecked) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }

            // 将光标移动到文本末尾
            passwordEditText3.setSelection(passwordEditText3.text.length)
        }


        binding.btnUpdatePassword.setOnClickListener {
            val users = FirebaseAuth.getInstance().currentUser
            if (users != null) {
                val currentEmail = users.email.toString()
                val uid = users.uid
                databaseReference = FirebaseDatabase.getInstance().getReference("users")
                databaseReference.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            val currentPassword = dataSnapshot.child("password").getValue(String::class.java)
                            Log.d("Test Current Email", currentEmail)
                            Log.d("Test Current Password", currentPassword.toString())
                            Log.d("Test Old password typed", binding.etOldPassword.text.toString())

                            if (currentPassword.toString() == binding.etOldPassword.text.toString()) {
                                val newPassword = binding.etNewPassword.text.toString()
                                Log.d("Test New Password", newPassword)
                                // Update the password
                                users.updatePassword(newPassword).addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(
                                            this@ModifyPasswordActivity,
                                            "Password Update Successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        // update to firebase
                                        databaseReference.child(uid).child("password").setValue(newPassword)

                                        // Reauthenticate after updating the password
                                        val credential = EmailAuthProvider.getCredential(currentEmail, newPassword)
                                        users.reauthenticate(credential)
                                        startActivity(Intent(this@ModifyPasswordActivity, ActivitySetting::class.java))
                                        finish()
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


