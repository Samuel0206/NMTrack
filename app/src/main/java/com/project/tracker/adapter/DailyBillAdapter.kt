package com.project.tracker.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.project.tracker.data.DailyBill
import com.project.tracker.databinding.ItemBillBinding

class DailyBillAdapter (private val context: Context, private var bills: MutableList<DailyBill>)
    : RecyclerView.Adapter<DailyBillAdapter.DailyBillViewHolder>() {

        class DailyBillViewHolder(val binding: ItemBillBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemCount(): Int = bills.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyBillViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val binding = ItemBillBinding.inflate(layoutInflater, parent, false)

        return DailyBillViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DailyBillViewHolder, position: Int) {
        val bill = bills[position]

        holder.binding.amountList.text = bill.amount.toString()
        holder.binding.usageList.text = bill.usage
        holder.binding.typeList.text = bill.type
        holder.binding.dateList.text = bill.date
    }

    }

