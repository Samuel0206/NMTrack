package com.project.tracker

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.project.tracker.fragment.StatisticPageFragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class YearlyReportActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var YBackToMainButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_yearly_report)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        YBackToMainButton = findViewById<Button>(R.id.yearly_back_to_main)
        YBackToMainButton.setOnClickListener{
            startActivity(Intent(this, StatisticPageFragment::class.java))
        }

        fetchYearlyData()
    }

    private fun fetchYearlyData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val yearlyExpenseTextView = findViewById<TextView>(R.id.yearly_expense)
            val yearlyIncomeTextView = findViewById<TextView>(R.id.yearly_income)
            val yearlyMostSpendTextView = findViewById<TextView>(R.id.yearly_most_spend)
            val pieChart = findViewById<PieChart>(R.id.pie_chart_yearly)
            val barChart = findViewById<BarChart>(R.id.bar_chart_yearly)
            val yearlyMostExpenseMonthTextView = findViewById<TextView>(R.id.yearly_most_expense_month)
            val yearlyMostIncomeMonthTextView = findViewById<TextView>(R.id.yearly_most_income_month)

            val databaseQuery = database.child("bills").orderByChild("userId").equalTo(userId)

            databaseQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var yearlyExpense = 0
                    var yearlyIncome = 0
                    val expenseByUsage = HashMap<String, Int>()
                    val monthlyData = Array(12) { Pair(0, 0) } // Pair of (expense, income) for each month

                    for (billSnapshot in snapshot.children) {
                        val type = billSnapshot.child("type").getValue(String::class.java)
                        val amount = billSnapshot.child("amount").getValue(Int::class.java)
                        val date = billSnapshot.child("date").getValue(String::class.java)
                        val usage = billSnapshot.child("usage").getValue(String::class.java)

                        if (type != null && amount != null && date != null && usage != null) {
                            val calendar = Calendar.getInstance()
                            calendar.time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)
                            val year = calendar.get(Calendar.YEAR)

                            if (year == Calendar.getInstance().get(Calendar.YEAR)) {
                                if (type == "Income") {
                                    yearlyIncome += amount
                                } else {
                                    yearlyExpense += amount
                                    expenseByUsage[usage] = (expenseByUsage[usage] ?: 0) + amount
                                }

                                val month = calendar.get(Calendar.MONTH)
                                monthlyData[month] = Pair(monthlyData[month].first + amount, if (type == "Income") monthlyData[month].second + amount else monthlyData[month].second)
                            }
                        }
                    }

                    yearlyExpenseTextView.text = "$yearlyExpense"
                    yearlyIncomeTextView.text = "$yearlyIncome"

                    if (expenseByUsage.isNotEmpty()) {
                        val mostSpendUsage = expenseByUsage.maxByOrNull { it.value }?.key
                        yearlyMostSpendTextView.text = "Your have spent most money on $mostSpendUsage"
                    }

                    // Pie Chart
                    val pieEntries = ArrayList<PieEntry>()
                    expenseByUsage.forEach { (usage, amount) ->
                        pieEntries.add(PieEntry(amount.toFloat(), usage))
                    }

                    val pieDataSet = PieDataSet(pieEntries, "Expense by Usage")
                    pieDataSet.setColors(*ColorTemplate.COLORFUL_COLORS)
                    pieDataSet.valueTextColor = Color.WHITE
                    pieDataSet.valueTextSize = 12f

                    val pieData = PieData(pieDataSet)
                    pieData.setValueFormatter(PercentFormatter(pieChart))
                    pieChart.data = pieData
                    pieChart.description.isEnabled = false
                    pieChart.setEntryLabelTextSize(10f)
                    pieChart.legend.orientation = Legend.LegendOrientation.HORIZONTAL
                    pieChart.legend.textSize = 12f
                    pieChart.animateY(1000)

                    var mostExpenseMonth = 0
                    var mostIncomeMonth = 0
                    var maxExpense = monthlyData[0].first
                    var maxIncome = monthlyData[0].second

                    for (i in 1 until monthlyData.size) {
                        if (monthlyData[i].first > maxExpense) {
                            mostExpenseMonth = i
                            maxExpense = monthlyData[i].first
                        }
                        if (monthlyData[i].second > maxIncome) {
                            mostIncomeMonth = i
                            maxIncome = monthlyData[i].second
                        }
                    }

                    yearlyMostExpenseMonthTextView.text = "You spent the most money in ${getMonthName(mostExpenseMonth)}"
                    yearlyMostIncomeMonthTextView.text = "But you got a lot of money in ${getMonthName(mostIncomeMonth)}"

                    // Bar Chart
                    val barEntries = ArrayList<BarEntry>()
                    for (i in monthlyData.indices) {
                        val monthData = monthlyData[i]
                        // 添加支出数据
                        barEntries.add(BarEntry(i.toFloat(), monthData.first.toFloat()))
                        // 添加收入数据
                        barEntries.add(BarEntry(i.toFloat(), monthData.second.toFloat()))
                    }

                    val barDataSet = BarDataSet(barEntries, "Monthly Data")
                    barDataSet.colors = listOf(Color.parseColor("#FF6347"), Color.parseColor("#3CB371"))
                    barDataSet.stackLabels = arrayOf("Expense", "Income")
                    barDataSet.valueTextColor = Color.BLACK

                    val barData = BarData(barDataSet)
                    barData.barWidth = 0.3f

                    barChart.data = barData
                    barChart.description.isEnabled = false
                    barChart.setFitBars(true)
                    barChart.animateY(1000)

                    val xAxis = barChart.xAxis
                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                    xAxis.setDrawGridLines(false)
                    xAxis.labelCount = 12
                    xAxis.valueFormatter = IndexAxisValueFormatter(arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"))

                    barChart.invalidate()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }

                private fun getMonthName(month: Int): String {
                    return SimpleDateFormat("MMMM", Locale.getDefault()).format(Calendar.getInstance().apply { set(Calendar.MONTH, month) }.time)
                }
            })
        }
    }
}

//package com.project.tracker
//
//import androidx.fragment.app.Fragment
//
//class YearlyReportFragment : Fragment() {
//    private lateinit var auth: FirebaseAuth
//    private lateinit var database: FirebaseDatabase
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val view = inflater.inflate(R.layout.fragment_yearly_report, container, false)
//
//        auth = FirebaseAuth.getInstance()
//        database = FirebaseDatabase.getInstance()
//
//        val userId = auth.currentUser?.uid
//        if (userId != null) {
//            fetchYearlyData(userId, view)
//        }
//
//        return view
//    }
//
//    private fun fetchYearlyData(userId: String, view: View) {
//        val yearlyExpenseTextView = view.findViewById<TextView>(R.id.yearly_expense)
//        val yearlyIncomeTextView = view.findViewById<TextView>(R.id.yearly_income)
//        val yearlyMostSpendTextView = view.findViewById<TextView>(R.id.yearly_most_spend)
//        val pieChart = view.findViewById<PieChart>(R.id.pieChart)
//        val barChart = view.findViewById<BarChart>(R.id.barChart)
//
//        val databaseQuery = database.reference.child("bills").orderByChild("userId").equalTo(userId)
//
//        databaseQuery.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                var yearlyExpense = 0
//                var yearlyIncome = 0
//                val expenseByUsage = HashMap<String, Int>()
//                val monthlyData = Array(12) { Pair(0, 0) } // Pair of total expense and total income for each month
//
//                for (billSnapshot in snapshot.children) {
//                    val type = billSnapshot.child("type").getValue(String::class.java)
//                    val amount = billSnapshot.child("amount").getValue(Int::class.java)
//                    val usage = billSnapshot.child("usage").getValue(String::class.java)
//                    val date = billSnapshot.child("date").getValue(String::class.java)
//
//                    if (type != null && amount != null && usage != null && date != null) {
//                        val calendar = Calendar.getInstance()
//                        calendar.time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)
//                        val year = calendar.get(Calendar.YEAR)
//                        val month = calendar.get(Calendar.MONTH)
//
//                        if (year == Calendar.getInstance().get(Calendar.YEAR)) {
//                            if (type == "Income") {
//                                yearlyIncome += amount
//                            } else {
//                                yearlyExpense += amount
//                                expenseByUsage[usage] = (expenseByUsage[usage] ?: 0) + amount
//                            }
//                            monthlyData[month] = Pair(monthlyData[month].first + amount,
//                                monthlyData[month].second + (if (type == "Income") amount else 0))
//                        }
//                    }
//                }
//
//                // Set yearly expense and income
//                yearlyExpenseTextView.text = "Yearly Expense: $yearlyExpense"
//                yearlyIncomeTextView.text = "Yearly Income: $yearlyIncome"
//
//                // Set pie chart for expense by usage
//                setupPieChart(pieChart, expenseByUsage)
//
//                // Set most spend usage
//                val mostSpendUsage = expenseByUsage.maxByOrNull { it.value }?.key ?: ""
//                yearlyMostSpendTextView.text = "Yearly Most Spend: $mostSpendUsage"
//
//                // Set bar chart for monthly expense and income
//                setupBarChart(barChart, monthlyData)
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                // Handle error
//            }
//        })
//    }
//
//    private fun setupPieChart(pieChart: PieChart, expenseByUsage: Map<String, Int>) {
//        val entries = ArrayList<PieEntry>()
//        for ((usage, amount) in expenseByUsage) {
//            entries.add(PieEntry(amount.toFloat(), usage))
//        }
//
//        val dataSet = PieDataSet(entries, "Expense by Usage")
//        dataSet.colors = ColorTemplate.COLORFUL_COLORS.asList()
//
//        val data = PieData(dataSet)
//        data.setValueFormatter(PercentFormatter(pieChart))
//        pieChart.data = data
//
//        pieChart.setUsePercentValues(true)
//        pieChart.description.isEnabled = false
//        pieChart.legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
//        pieChart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
//        pieChart.legend.orientation = Legend.LegendOrientation.VERTICAL
//        pieChart.setDrawEntryLabels(false)
//        pieChart.invalidate()
//    }
//
//    private fun setupBarChart(barChart: BarChart, monthlyData: Array<Pair<Int, Int>>) {
//        val entriesExpense = ArrayList<BarEntry>()
//        val entriesIncome = ArrayList<BarEntry>()
//
//        for ((index, data) in monthlyData.withIndex()) {
//            entriesExpense.add(BarEntry(index.toFloat(), data.first.toFloat()))
//            entriesIncome.add(BarEntry(index.toFloat(), data.second.toFloat()))
//        }
//
//        val dataSetExpense = BarDataSet(entriesExpense, "Expense")
//        dataSetExpense.color = resources.getColor(R.color.red)
//        val dataSetIncome = BarDataSet(entriesIncome, "Income")
//        dataSetIncome.color = resources.getColor(R.color.green)
//
//        val data = BarData(dataSetExpense, dataSetIncome)
//        data.barWidth = 0.35f
//
//        barChart.data = data
//        barChart.setFitBars(true)
//        barChart.description.isEnabled = false
//        barChart.legend.isEnabled = false
//        barChart.xAxis.granularity = 1f
//        barChart.xAxis.valueFormatter = MonthXAxisValueFormatter()
//
//        barChart.invalidate()
//    }
//}
