package com.project.tracker

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.project.tracker.databinding.ActivitySettingBinding


class ActivitySetting : AppCompatActivity() {
    private val view: ActivitySettingBinding by lazy { ActivitySettingBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(view.root)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener { showLogoutConfirmationDialog() }

        view.btnEditPassword.setOnClickListener {
            startActivity(Intent(this, ModifyPasswordActivity::class.java))
        }
    }



    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Logout Confirmation")
        builder.setMessage("Are you sure you want to logout?")
        builder.setPositiveButton(
            "Logout"
        ) { dialogInterface, i -> // User clicked the Logout button
            // Perform logout and navigate back to the login page
            navigateToLoginPage()
        }
        builder.setNegativeButton(
            "Cancel"
        ) { dialogInterface, i ->
            // User clicked the Cancel button, do nothing
        }
        builder.show()
    }

    private fun navigateToLoginPage() {
        // Add code to perform logout actions
        // For example, clearing user session or preferences
        // Then navigate back to the login page
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish() // Optional: Finish the current activity to prevent going back with the back button
    }
}