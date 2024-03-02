package com.project.tracker

import androidx.fragment.app.Fragment

class YearlyReportFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_yearly_report, container, false)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val userId = auth.currentUser?.uid
        if (userId != null) {
            fetchYearlyData(userId, view)
        }

        return view
    }

    private fun fetchYearlyData(userId: String, view: View) {
        val yearlyExpenseTextView = view.findViewById<TextView>(R.id.yearly_expense)
        val yearlyIncomeTextView = view.findViewById<TextView>(R.id.yearly_income)
        val yearlyMostSpendTextView = view.findViewById<TextView>(R.id.yearly_most_spend)
        val pieChart = view.findViewById<PieChart>(R.id.pieChart)
        val barChart = view.findViewById<BarChart>(R.id.barChart)

        val databaseQuery = database.reference.child("bills").orderByChild("userId").equalTo(userId)

        databaseQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var yearlyExpense = 0
                var yearlyIncome = 0
                val expenseByUsage = HashMap<String, Int>()
                val monthlyData = Array(12) { Pair(0, 0) } // Pair of total expense and total income for each month

                for (billSnapshot in snapshot.children) {
                    val type = billSnapshot.child("type").getValue(String::class.java)
                    val amount = billSnapshot.child("amount").getValue(Int::class.java)
                    val usage = billSnapshot.child("usage").getValue(String::class.java)
                    val date = billSnapshot.child("date").getValue(String::class.java)

                    if (type != null && amount != null && usage != null && date != null) {
                        val calendar = Calendar.getInstance()
                        calendar.time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)
                        val year = calendar.get(Calendar.YEAR)
                        val month = calendar.get(Calendar.MONTH)

                        if (year == Calendar.getInstance().get(Calendar.YEAR)) {
                            if (type == "Income") {
                                yearlyIncome += amount
                            } else {
                                yearlyExpense += amount
                                expenseByUsage[usage] = (expenseByUsage[usage] ?: 0) + amount
                            }
                            monthlyData[month] = Pair(monthlyData[month].first + amount,
                                monthlyData[month].second + (if (type == "Income") amount else 0))
                        }
                    }
                }

                // Set yearly expense and income
                yearlyExpenseTextView.text = "Yearly Expense: $yearlyExpense"
                yearlyIncomeTextView.text = "Yearly Income: $yearlyIncome"

                // Set pie chart for expense by usage
                setupPieChart(pieChart, expenseByUsage)

                // Set most spend usage
                val mostSpendUsage = expenseByUsage.maxByOrNull { it.value }?.key ?: ""
                yearlyMostSpendTextView.text = "Yearly Most Spend: $mostSpendUsage"

                // Set bar chart for monthly expense and income
                setupBarChart(barChart, monthlyData)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun setupPieChart(pieChart: PieChart, expenseByUsage: Map<String, Int>) {
        val entries = ArrayList<PieEntry>()
        for ((usage, amount) in expenseByUsage) {
            entries.add(PieEntry(amount.toFloat(), usage))
        }

        val dataSet = PieDataSet(entries, "Expense by Usage")
        dataSet.colors = ColorTemplate.COLORFUL_COLORS.asList()

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(pieChart))
        pieChart.data = data

        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        pieChart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        pieChart.legend.orientation = Legend.LegendOrientation.VERTICAL
        pieChart.setDrawEntryLabels(false)
        pieChart.invalidate()
    }

    private fun setupBarChart(barChart: BarChart, monthlyData: Array<Pair<Int, Int>>) {
        val entriesExpense = ArrayList<BarEntry>()
        val entriesIncome = ArrayList<BarEntry>()

        for ((index, data) in monthlyData.withIndex()) {
            entriesExpense.add(BarEntry(index.toFloat(), data.first.toFloat()))
            entriesIncome.add(BarEntry(index.toFloat(), data.second.toFloat()))
        }

        val dataSetExpense = BarDataSet(entriesExpense, "Expense")
        dataSetExpense.color = resources.getColor(R.color.red)
        val dataSetIncome = BarDataSet(entriesIncome, "Income")
        dataSetIncome.color = resources.getColor(R.color.green)

        val data = BarData(dataSetExpense, dataSetIncome)
        data.barWidth = 0.35f

        barChart.data = data
        barChart.setFitBars(true)
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.xAxis.granularity = 1f
        barChart.xAxis.valueFormatter = MonthXAxisValueFormatter()

        barChart.invalidate()
    }
}