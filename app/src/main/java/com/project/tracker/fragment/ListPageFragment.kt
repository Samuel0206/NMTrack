package com.project.tracker.fragment

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.project.tracker.R
import com.project.tracker.adapter.BillAdapter
import com.project.tracker.adapter.FilterAdapter
import com.project.tracker.data.Bill
import com.project.tracker.data.FilterBill

class ListPageFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BillAdapter
    private lateinit var db: DatabaseReference
    private lateinit var userId: String
    private lateinit var filterSpinner: Spinner
    private lateinit var filterAdapter: FilterAdapter
    private lateinit var loadingView: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_list_page_fragment, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = BillAdapter(requireContext(), mutableListOf())
        recyclerView.adapter = adapter

        filterSpinner = view.findViewById(R.id.list_filter)
        setupFilterSpinner()

        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        db = FirebaseDatabase.getInstance().reference.child("bills")

        loadingView = inflater.inflate(R.layout.loading_view, container, false)
        container?.addView(loadingView)

        loadAllBills()

        return view
    }

    private fun setupFilterSpinner() {
        val filters = listOf(
            FilterBill(R.drawable.all, "All"),
            FilterBill(R.drawable.expense, "Expenses"),
            FilterBill(R.drawable.income, "Income")
        )
        filterAdapter = FilterAdapter(requireContext(), filters)
        filterSpinner.adapter = filterAdapter

        filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val filter = filterAdapter.getItem(position)
                filter?.let {
                    when (it.text) {
                        "All" -> loadAllBills()
                        "Expenses" -> loadFilteredBills("Expenses")
                        "Income" -> loadFilteredBills("Income")
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun loadAllBills() {
        loadingView.visibility = View.VISIBLE

        db.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val bills = mutableListOf<Bill>()
                for (billSnapshot in snapshot.children) {
                    val bill = billSnapshot.getValue(Bill::class.java)
                    bill?.let { bills.add(it) }
                }

                bills.sortByDescending { it.date }

                adapter.updateData(bills)
                loadingView.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ListPageFragment", "Failed to read value.", error.toException())
                loadingView.visibility = View.GONE
            }
        })
    }

    private fun loadFilteredBills(type: String) {
        db.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val bills = mutableListOf<Bill>()
                for (billSnapshot in snapshot.children) {
                    val bill = billSnapshot.getValue(Bill::class.java)
                    bill?.let {
                        if (it.type == type) {
                            bills.add(it)
                        }
                    }
                }
                adapter.updateData(bills)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ListPageFragment", "Failed to read value.", error.toException())
            }
        })
    }
}