package com.project.tracker.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.project.tracker.ActivitySetting
import com.project.tracker.EditProfileActivity
import com.project.tracker.FeedbackActivity
import com.project.tracker.R


class UserPageFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var currentEmail: String? = null
    private var currentPhone: String? = null
    // 添加视图绑定
    private lateinit var imgAvatar: ImageView
    private lateinit var tvUsername: TextView
    private lateinit var btnEditProfile: Button
    private lateinit var etGoal: EditText
    private lateinit var btnUpdateGoal: Button
    private lateinit var btnFeedBack: Button
    private lateinit var btnProfileSetting: Button

    // Define a constant for the request code
    private val EDIT_PROFILE_REQUEST_CODE = 1001

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_user_page_fragment, container, false)

        // 初始化视图绑定
        imgAvatar = view.findViewById(R.id.imgAvatar)
        tvUsername = view.findViewById(R.id.tvUsername)
        btnEditProfile = view.findViewById(R.id.btnEditProfile)
        etGoal = view.findViewById(R.id.etGoal)
        btnUpdateGoal = view.findViewById(R.id.btnUpdateGoal)
        btnFeedBack = view.findViewById(R.id.btnFeedBack)
        btnProfileSetting = view.findViewById(R.id.btnProfileSetting)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Fetch and display the username from the database
        getCurrentUserDisplayNameFromDatabase()

        // Set the avatar image (you can customize this based on your requirements)
        imgAvatar.setImageResource(R.drawable.defaultavatar)

        // Clicking the edit button to start the profile edit activity/fragment
        btnEditProfile.setOnClickListener {
            // 在按钮点击时执行的逻辑
            val intent = Intent(requireContext(), EditProfileActivity::class.java)

            // Pass the current username, email, and phone as extras to EditProfileActivity
            val currentUsername = tvUsername.text.toString()
            intent.putExtra("currentUsername", currentUsername)
            intent.putExtra("currentEmail", currentEmail)
            intent.putExtra("currentPhone", currentPhone)

               // Start the EditProfileActivity with a result code
            startActivityForResult(intent, EDIT_PROFILE_REQUEST_CODE)
        }

        fetchAndSetInitialGoalValue()

        btnUpdateGoal.setOnClickListener {
            updateGoal()
            hideKeyboard()
        }

        btnFeedBack.setOnClickListener {
            startActivity(Intent(requireContext(), FeedbackActivity::class.java))
        }

        btnProfileSetting.setOnClickListener {
            startActivity(Intent(requireContext(), ActivitySetting::class.java))
        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDIT_PROFILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Retrieve the updated username from the result
            val updatedUsername = data?.getStringExtra("updatedUsername")
            val updatedEmail = data?.getStringExtra("updatedEmail")
            val updatedPhone = data?.getStringExtra("updatedPhone")
            // Update tvUsername with the new username
            if (!updatedUsername.isNullOrBlank()) {
                tvUsername.text = updatedUsername
            }
            if (!updatedEmail.isNullOrBlank()) {
                currentEmail = updatedEmail
            }
            if (!updatedPhone.isNullOrBlank()) {
                currentPhone = updatedPhone
            }
        }
    }

    private fun getCurrentUserDisplayNameFromDatabase() {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            val userRef = database.child("users").child(userId)

            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val username = snapshot.child("username").getValue(String::class.java)
                    currentEmail = snapshot.child("email").getValue(String::class.java)
                    currentPhone = snapshot.child("phone").getValue(String::class.java)
                    // Check if the retrieved username is not null or empty before updating tvUsername
                    if (!username.isNullOrBlank()) {
                        tvUsername.text = username

                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            }

            userRef.addListenerForSingleValueEvent(valueEventListener)
        }
    }

    private fun fetchAndSetInitialGoalValue() {
        val userId = auth.currentUser?.uid

        userId?.let {
            val userGoalRef = database.child("users").child(userId).child("goal")

            userGoalRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val initialGoalValue = snapshot.getValue(Int::class.java)
                    etGoal.hint = initialGoalValue?.toString() ?: "Enter Goal"
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }

    private fun updateGoal() {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid
        val goalValue = etGoal.text.toString().toIntOrNull() ?: 0  // Parse as integer, default to 0 if not a valid integer

        if (userId != null) {
            // Reference to the user's goal in the database
            val userGoalRef = database.child("users").child(userId).child("goal")

            // Update the goal value in Firebase
            userGoalRef.setValue(goalValue)
                .addOnSuccessListener {
                    // Update successful
                    Toast.makeText(requireContext(), "Goal updated successfully", Toast.LENGTH_SHORT).show()

                    etGoal.clearFocus()
                }
                .addOnFailureListener {
                    // Handle error
                    Toast.makeText(requireContext(), "Failed to update goal", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        // Find the currently focused view, giving us the view that was used to provide the focus
        // when the user touched the screen
        val currentFocusedView = requireActivity().currentFocus

        // If no view is focused, an NPE will be thrown
        currentFocusedView?.let {
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

}