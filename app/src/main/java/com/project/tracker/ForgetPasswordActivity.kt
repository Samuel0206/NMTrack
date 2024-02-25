package com.project.tracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ForgetPasswordActivity : AppCompatActivity(){

    private lateinit var toLoginPage : ImageView
    private lateinit var emailInput : EditText
    private lateinit var recoverButton : Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forget_password)

        auth = FirebaseAuth.getInstance()

        toLoginPage = findViewById(R.id.back_to_login)
        emailInput = findViewById(R.id.email_recover)
        recoverButton = findViewById(R.id.recover_button)

        recoverButton.setOnClickListener {
            val email = emailInput.text.toString()
            if (email.isNotEmpty()) {
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                this,
                                "Reset link has already sent to your email, please check! ",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this,
                                "Email sent failed, please try again.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please enter your email. ", Toast.LENGTH_SHORT).show()
            }
        }

        toLoginPage.setOnClickListener{
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}