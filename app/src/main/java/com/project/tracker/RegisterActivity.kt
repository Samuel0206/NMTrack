package com.project.tracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var rePasswordEditText: EditText
    private lateinit var toLoginButton: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        usernameEditText = findViewById(R.id.username_register)
        emailEditText = findViewById(R.id.email_register)
        phoneEditText = findViewById(R.id.phone_register)
        passwordEditText = findViewById(R.id.password_register)
        registerButton = findViewById(R.id.register_button)
        rePasswordEditText = findViewById(R.id.rePassword_register)
        toLoginButton = findViewById(R.id.register_to_login_button)

        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val email = emailEditText.text.toString()
            val phone = phoneEditText.text.toString()
            val password = passwordEditText.text.toString()

            registerButton.setOnClickListener {
                val username = usernameEditText.text.toString()
                val email = emailEditText.text.toString()
                val phone = phoneEditText.text.toString()
                val password = passwordEditText.text.toString()
                val rePassword = rePasswordEditText.text.toString()

                if (username.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || rePassword.isEmpty()) {
                    Toast.makeText(this, "All fields are required! ", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (password != rePassword) {
                    Toast.makeText(this, "The entered passwords are inconsistent! ", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // 创建用户并写入数据库
                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val userId = mAuth.currentUser?.uid
                            val userRef = database.reference.child("users").child(userId!!)
                            val userData = hashMapOf(
                                "username" to username,
                                "email" to email,
                                "phone" to phone,
                                "password" to password,
                                "avatarurl" to ""
                            )
                            userRef.setValue(userData)
                            Toast.makeText(this, "Register success! ", Toast.LENGTH_SHORT).show()
                            // 注册成功后跳转回登录页
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish() // 结束当前页面
                        } else {
                            Toast.makeText(
                                this, "Register failed : ${task.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }

        }
        toLoginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}