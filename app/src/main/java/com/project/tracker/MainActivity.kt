package com.project.tracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.widget.ImageButton
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.project.tracker.fragment.ListPageFragment
import com.project.tracker.fragment.UserPageFragment

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var buttonToBillInput: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigationView = findViewById(R.id.bottom_navigation)
        buttonToBillInput = findViewById(R.id.btn_bill_input)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                //R.id.action_main_page -> {
                //loadFragment(MainPageFragment())
                //return@setOnNavigationItemSelectedListener true
                //}
                //R.id.action_list_page -> {
                //loadFragment(ListPageFragment())
                //return@setOnNavigationItemSelectedListener true
                //}
                R.id.action_user_page -> {
                    loadFragment(UserPageFragment())
                    return@setOnNavigationItemSelectedListener true
                }
                else -> false
            }
        }

        // 默认加载第一个 Fragment
        //loadFragment(MainPageFragment())
    //}

    //private fun loadFragment(fragment: Fragment) {
        //supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()
        buttonToBillInput.setOnClickListener{
            startActivity(Intent(this, BillInputActivity::class.java))
        }
    }

        private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()
        }
}