package com.project.tracker.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.project.tracker.BillInputActivity
import com.project.tracker.R
import com.project.tracker.adapter.BillAdapter
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date

class MainPageFragment : Fragment() {
    private lateinit var tvTodayExpense: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: BillAdapter
    private lateinit var userId: String
    private lateinit var billRef: DatabaseReference
    private lateinit var database: DatabaseReference
    private lateinit var tvBalance: TextView
    private lateinit var recyclerView: RecyclerView


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.activity_main_page_fragment, container, false)

        recyclerView = view.findViewById(R.id.rvDayBill)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = BillAdapter(requireContext(), mutableListOf())
        recyclerView.adapter = adapter

        tvBalance = view.findViewById(R.id.tvBalance)
        tvTodayExpense = view.findViewById(R.id.tvTodayExpense)
        billRef = FirebaseDatabase.getInstance().reference.child("bills")
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        val button1 = view.findViewById<Button>(R.id.m_to_bi)
        button1.setOnClickListener {
            startActivity(Intent(requireContext(), BillInputActivity::class.java))
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getTodaysExpenses()
        getMonthExpense()
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
            val tvBalance = view?.findViewById<TextView>(R.id.tvBalance)

            val databaseQuery = billRef.orderByChild("userId").equalTo(userId)

            databaseQuery.addListenerForSingleValueEvent(object : ValueEventListener {
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
                            if (date.substring(5, 7) == currentMonth && type == "Expenses") {
                                totalExpense += amount
                            }
                        }
                    }
                    val userRef = database.child("users").child(userId)
                    userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(userSnapshot: DataSnapshot) {
                            val userGoal = userSnapshot.child("goal").getValue(Int::class.java)
                            if (userGoal != null && userGoal > 0) {
                                if (totalExpense <= userGoal) {
                                    tvBalance?.text =
                                        "Monthly Use: $totalExpense / $userGoal Baht \n You have ${userGoal - totalExpense} baht balance"
                                } else {
                                    tvBalance?.text =
                                        "Monthly Use: $totalExpense / $userGoal Baht \n Expenditure has exceeded ${totalExpense - userGoal} baht!"
                                }

                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Handle error
                        }
                    })
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("GetMonthExpense", "Failed to GetMonthExpense")
                }
            })
        }
    }

    private fun getCurrentDate(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val current = LocalDate.now().format(formatter)
        return current
    }
}