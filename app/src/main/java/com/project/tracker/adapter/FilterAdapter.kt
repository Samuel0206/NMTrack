package com.project.tracker.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.project.tracker.R
import com.project.tracker.data.FilterBill

class FilterAdapter(private val context: Context, private val filters: List<FilterBill>) :
    ArrayAdapter<FilterBill>(context, 0, filters) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createFilterView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createFilterView(position, convertView, parent)
    }

    private fun createFilterView(position: Int, convertView: View?, parent: ViewGroup): View {
        val filter = getItem(position)
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_bill_filter, parent, false)

        val iconImageView = view.findViewById<ImageView>(R.id.icon_filter)
        val textView = view.findViewById<TextView>(R.id.item_filter)

        filter?.let {
            iconImageView.setImageResource(it.iconRes)
            textView.text = it.text
        }

        return view
    }
}