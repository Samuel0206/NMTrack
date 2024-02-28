package com.project.tracker

import android.app.DatePickerDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BillInputActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var amountUsage: AutoCompleteTextView
    private lateinit var inputAmount: EditText
    private lateinit var inputExpenses: RadioButton
    private lateinit var inputIncome: RadioButton
    private lateinit var inputAmountConfirm: Button
    private lateinit var radioGroup: RadioGroup
    private lateinit var dateTextView: TextView
    private lateinit var calendar: Calendar
    private lateinit var datePickerDialog: DatePickerDialog
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bill_input)

        mAuth = FirebaseAuth.getInstance()
        inputAmount = findViewById(R.id.input_amount)
        inputExpenses = findViewById(R.id.input_expenses)
        inputIncome = findViewById(R.id.input_income)
        inputAmountConfirm = findViewById(R.id.input_amount_confirm)
        radioGroup = findViewById(R.id.input_amount_type)
        amountUsage = findViewById(R.id.amount_usage_select)
        dateTextView = findViewById(R.id.date_text_view)

        setupAutoCompleteTextView()
        setDefaultType()

        calendar = Calendar.getInstance()
        updateDateTextView()
        
        dateTextView.setOnClickListener {
            showDatePickerDialog()
        }

        inputAmountConfirm.setOnClickListener {
            saveBillToFirebase()
            startActivity(Intent(this, MainActivity::class.java))
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.input_expenses -> {
                    amountUsage.isEnabled = true
                }
                R.id.input_income -> {
                    amountUsage.setText("")
                    amountUsage.isEnabled = false
                }
            }
        }
    }

    private fun setupAutoCompleteTextView() {
        val options = arrayOf("Food", "Shopping", "Entertainment")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, options)
        amountUsage.setAdapter(adapter)

        amountUsage.isFocusable = false
        amountUsage.isFocusableInTouchMode = false
        amountUsage.isClickable = true
        amountUsage.setOnClickListener {
            amountUsage.showDropDown()
        }
    }

    private fun setDefaultType() {
        inputIncome.isChecked = false
        amountUsage.isEnabled = true
    }

    private fun showDatePickerDialog() {
        datePickerDialog = DatePickerDialog(
            this,
            { _, year, monthOfYear, dayOfMonth ->
                calendar.set(year, monthOfYear, dayOfMonth)
                updateDateTextView()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun updateDateTextView() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        dateTextView.text = dateFormat.format(calendar.time)
    }

    private fun saveBillToFirebase() {
        val amount = inputAmount.text.toString().toDoubleOrNull() ?: return
        val usage = if (inputIncome.isChecked) "Income" else amountUsage.text.toString()
        val type = if (inputExpenses.isChecked) "Expenses" else "Income"
        val date = dateTextView.text.toString()

        currentUser?.uid?.let { userId ->
            val billId = database.child("bills").push().key
            val bill = hashMapOf(
                "id" to billId,
                "amount" to amount,
                "usage" to usage,
                "type" to type,
                "date" to date,
                "userId" to userId
            )

            if (billId != null) {
                database.child("bills").child(billId).setValue(bill)
                    .addOnSuccessListener {
                        Log.d(TAG, "Bill saved to Firebase Realtime Database with ID: $billId")
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error saving bill to Firebase Realtime Database", e)
                    }
            }
        }
    }
}
