package com.project.tracker.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.DatePicker
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.project.tracker.R
import com.project.tracker.data.Bill
import android.widget.EditText as EditText

class BillAdapter(private val context: Context, private var bills: MutableList<Bill>) :
    RecyclerView.Adapter<BillAdapter.BillViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_bill, parent, false)
        return BillViewHolder(view)
    }

    override fun onBindViewHolder(holder: BillViewHolder, position: Int) {
        val bill = bills[position]
        holder.bind(bill)
    }

    override fun getItemCount(): Int = bills.size

    fun updateData(newBills: List<Bill>) {
        bills.clear()
        bills.addAll(newBills)
        notifyDataSetChanged()
    }

    fun removeBill(bill: Bill) {
        val position = bills.indexOf(bill)
        if (position != -1) {
            bills.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    inner class BillViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val amountList: TextView = itemView.findViewById(R.id.amount_list)
        private val usageList: TextView = itemView.findViewById(R.id.usage_list)
        private val dateList: TextView = itemView.findViewById(R.id.date_list)
        private val typeList: TextView = itemView.findViewById(R.id.type_list)

        fun bind(bill: Bill) {
            amountList.text = context.getString(R.string.amount_format, bill.amount)
            usageList.text = bill.usage
            dateList.text = bill.date
            typeList.text = bill.type

            // 长按弹出删除对话框
            itemView.setOnLongClickListener {
                showDeleteDialog(bill)
                true
            }
        }

        private fun showDeleteDialog(bill: Bill) {
            val options = arrayOf("Delete", "Modify")
            AlertDialog.Builder(context)
                .setTitle("Action")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> deleteBill(bill)
                        1 -> showModifyDialog(bill)
                    }
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        private fun showModifyDialog(bill: Bill) {
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_modify_bill, null)
            val amountInput = view.findViewById<EditText>(R.id.edit_amount)
            val usageInput = view.findViewById<AutoCompleteTextView>(R.id.edit_usage)
            val datePicker = view.findViewById<DatePicker>(R.id.edit_date)

            amountInput.setText(bill.amount.toString())
            usageInput.setText(bill.usage)

            val options = arrayOf("Food", "Shopping", "Entertainment", "Travel", "Bills", "Other")
            val adapter = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, options)
            usageInput.setAdapter(adapter)

            AlertDialog.Builder(context)
                .setTitle("Modify Bill")
                .setView(view)
                .setPositiveButton("Update") { _, _ ->
                    val newAmount = amountInput.text.toString().toDouble()
                    val newUsage = usageInput.text.toString()
                    val newDate = String.format("%04d-%02d-%02d", datePicker.year, datePicker.month + 1, datePicker.dayOfMonth)

                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    val db = FirebaseDatabase.getInstance().reference
                    val billRef = db.child("bills").child(bill.id)


                    val updatedBill = Bill(bill.id, newAmount, newDate, bill.type, newUsage, userId)
                    billRef.setValue(updatedBill)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Bill updated successfully", Toast.LENGTH_SHORT).show()
                            notifyDataSetChanged()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Failed to update bill", Toast.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        private fun deleteBill(bill: Bill) {
            val db = FirebaseDatabase.getInstance().reference
            val billRef = db.child("bills").child(bill.id)
            billRef.removeValue()
                .addOnSuccessListener {
                    Toast.makeText(context, "Bill deleted successfully", Toast.LENGTH_SHORT).show()
                    removeBill(bill)
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to delete bill", Toast.LENGTH_SHORT).show()
                }
        }
    }
}


