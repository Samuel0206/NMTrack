package com.project.tracker.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.project.tracker.BillInputActivity
import com.project.tracker.R
import com.project.tracker.adapter.BillAdapter
import com.project.tracker.data.Bill
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

        loadTodayBills()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadTodayBills()
        getMonthExpense()
    }

    private fun loadTodayBills() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        if (userId != null) {
            val databaseQuery = database.orderByChild("userId").equalTo(userId)
            databaseQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val bills = mutableListOf<Bill>()
                    for (billSnapshot in snapshot.children) {
                        val bill = billSnapshot.getValue(Bill::class.java)
                        bill?.let {
                            if (it.date == today) {
                                bills.add(it)
                                Log.d("Bills", bills.toString())
                            }
                        }
                    }
                    adapter.updateData(bills)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MainPageFragment", "Failed to read value.", error.toException())
                }
            })
        }
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
}