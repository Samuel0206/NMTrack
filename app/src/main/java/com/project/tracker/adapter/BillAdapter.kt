package com.project.tracker.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.project.tracker.R
import com.project.tracker.data.Bill

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
            AlertDialog.Builder(context)
                .setTitle("Delete Bill")
                .setMessage("Are you sure you want to delete this bill?")
                .setPositiveButton("Delete") { _, _ ->
                    deleteBill(bill)
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
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to delete bill", Toast.LENGTH_SHORT).show()
                }
        }
    }
}


