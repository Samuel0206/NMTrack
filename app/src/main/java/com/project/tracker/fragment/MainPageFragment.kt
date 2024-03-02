package com.project.tracker.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.project.tracker.R
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date

class MainPageFragment : Fragment() {
    private lateinit var tvTodayExpense: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var billRef: DatabaseReference
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_main_page_fragment, container, false)

        tvTodayExpense = view.findViewById(R.id.tvTodayExpense)
        billRef = FirebaseDatabase.getInstance().reference.child("bills")
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        getTodaysExpenses()
        getMonthExpense()
        return view
    }

    private fun getTodaysExpenses() {
        val todayDate = getCurrentDate()

        billRef.orderByChild("date").equalTo(todayDate)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var totalExpense = 0.0
                    val currentUserUid = auth.currentUser?.uid

                    for (snapshot in dataSnapshot.children) {
                        val amount = snapshot.child("amount").getValue(Double::class.java)
                        val type = snapshot.child("type").getValue(String::class.java)
                        val billUid = snapshot.child("userId").getValue(String::class.java)

                        Log.d("testamount", amount.toString())
                        Log.d("testtype", type.toString())
                        Log.d("testbillUid", billUid.toString())

                        if (type == "Expenses" && currentUserUid == billUid) {
                            totalExpense += amount ?: 0.0
                        }
                    }

                    // Update the corresponding TextView for expenses
                    tvTodayExpense.text = "Today's Expenses: $totalExpense Baht"
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle the error
                    Log.e("FirebaseError", "Database Error: ${databaseError.message}")
                }
            })
    }

    private fun getMonthExpense() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val tvExpense_Goal = view?.findViewById<TextView>(R.id.tvExpense_Goal)

            val databaseQuery = billRef.orderByChild("userId").equalTo(userId)

            databaseQuery.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var totalExpense = 0

                    for (billSnapshot in snapshot.children) {
                        val type = billSnapshot.child("type").getValue(String::class.java)
                        val amount = billSnapshot.child("amount").getValue(Int::class.java)
                        val date = billSnapshot.child("date").getValue(String::class.java)

                        if (type != null && amount != null && date != null) {
                            val sdf = SimpleDateFormat("MM")
                            val currentMonth = sdf.format(Date())
                            Log.d("CHECKCURRENTMONTH", currentMonth)
                            if (date.substring(5,7) == currentMonth && type == "Expenses"){
                                totalExpense += amount
                            }
                        }
                    }
                    val userRef = database.child("users").child(userId)
                    userRef.addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(userSnapshot: DataSnapshot) {
                            val userGoal = userSnapshot.child("goal").getValue(Int::class.java)
                            if (userGoal != null && userGoal > 0) {
                                tvExpense_Goal?.text = "Balance: $totalExpense/$userGoal"
                                tvExpense_Goal?.visibility = View.VISIBLE
                            } else {
                                tvExpense_Goal?.visibility = View.GONE
                            }
                        }


                        override fun onCancelled(error: DatabaseError) {
                            Log.e("GetGoal", "Failed to Get Goal")
                        }
                    })
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("GetMonthExpense", "Failed to GetMonthExpense")
                }
            })
        }
    }


    // Implement this function to get today's date in the format "yyyy-MM-dd"
    private fun getCurrentDate(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val current = LocalDate.now().format(formatter)
        return current
    }
}