package com.project.tracker.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.project.tracker.databinding.ActivityMainPageFragmentBinding

class MainPageFragment : Fragment() {
    private lateinit var view: ActivityMainPageFragmentBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return view.root
    }
}