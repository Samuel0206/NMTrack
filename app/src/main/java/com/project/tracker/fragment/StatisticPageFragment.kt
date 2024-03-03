package com.project.tracker.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.project.tracker.R
import com.project.tracker.YearlyReportActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class StatisticPageFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_statisic_page_fragment, container, false)

        auth = Firebase.auth
        database = FirebaseDatabase.getInstance().reference

        val userId = auth.currentUser?.uid
        if (userId != null) {
            setupMonthSpinner(view)
            val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
            val spinner = view.findViewById<Spinner>(R.id.month_choose)
            spinner.setSelection(currentMonth - 1) // Adjust for 0-based index
        }

        val toYearlyButton = view.findViewById<Button>(R.id.to_yearly_report)
        toYearlyButton.setOnClickListener{
            startActivity(Intent(context, YearlyReportActivity::class.java))
        }

        return view
    }

    private fun setupMonthSpinner(view: View) {
        val spinner = view.findViewById<Spinner>(R.id.month_choose)

        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.months_array,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedMonth = position + 1
                fetchMonthlyData(selectedMonth)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun fetchMonthlyData(selectedMonth: Int) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val totalIncomeTextView = view?.findViewById<TextView>(R.id.total_income_month)
            val totalExpenseTextView = view?.findViewById<TextView>(R.id.total_expense_month)
            val barChart = view?.findViewById<BarChart>(R.id.bar_chart_month)
            val averageDailyExpenseTextView = view?.findViewById<TextView>(R.id.average_daily_expense)
            val balanceTextView = view?.findViewById<TextView>(R.id.balance_this_month)


            val databaseQuery = database.child("bills").orderByChild("userId").equalTo(userId)

            databaseQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var totalIncome = 0
                    var totalExpense = 0
                    var totalDays = 0
                    val expensesByRange = Array(6) { 0 }

                    for (billSnapshot in snapshot.children) {
                        val type = billSnapshot.child("type").getValue(String::class.java)
                        val amount = billSnapshot.child("amount").getValue(Int::class.java)
                        val date = billSnapshot.child("date").getValue(String::class.java)

                        if (type != null && amount != null && date != null) {
                            val calendar = Calendar.getInstance()
                            calendar.time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)
                            val month = calendar.get(Calendar.MONTH) + 1 // Adding 1 because January is 0

                            if (month == selectedMonth) {

                                totalDays++
                                if (type == "Income") {
                                    totalIncome += amount
                                } else {
                                    totalExpense += amount
                                    val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
                                    val rangeIndex = (dayOfMonth - 1) / 5
                                    expensesByRange[rangeIndex] += amount
                                }
                            }
                        }
                    }

                    totalIncomeTextView?.text = "Total Income: $totalIncome"
                    totalExpenseTextView?.text = "Total Expense: $totalExpense"

                    val averageDailyExpense = if (totalDays > 0) totalExpense.toFloat() / totalDays else 0f
                    averageDailyExpenseTextView?.text = "Average Daily Expense: $averageDailyExpense"

                    // Prepare data for BarChart
                    val entries = ArrayList<BarEntry>()
                    for (i in expensesByRange.indices) {
                        entries.add(BarEntry(i.toFloat(), expensesByRange[i].toFloat()))
                    }

                    val dataSet = BarDataSet(entries, "Expense Trend")
                    val barData = BarData(dataSet)
                    barChart?.data = barData

                    // Customize BarChart
                    val xAxis = barChart?.xAxis
                    xAxis?.valueFormatter = DayRangeXAxisValueFormatter()
                    xAxis?.position = XAxis.XAxisPosition.BOTTOM
                    xAxis?.setDrawGridLines(false)
                    xAxis?.granularity = 1f

                    barChart?.axisRight?.isEnabled = false
                    barChart?.description?.isEnabled = false
                    barChart?.invalidate()

                    val userRef = database.child("users").child(userId)
                    userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(userSnapshot: DataSnapshot) {
                            val userGoal = userSnapshot.child("goal").getValue(Int::class.java)
                            if (userGoal != null && userGoal > 0) {
                                balanceTextView?.text
                                balanceTextView?.visibility = View.VISIBLE
                            } else {
                                balanceTextView?.visibility = View.GONE
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Handle error
                        }
                    })
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }

    private class DayRangeXAxisValueFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            val rangeStart = (value * 5 + 1).toInt()
            val rangeEnd = if (rangeStart + 4 <= 31) rangeStart + 4 else 31
            return "$rangeStart-$rangeEnd"
        }
    }
}
