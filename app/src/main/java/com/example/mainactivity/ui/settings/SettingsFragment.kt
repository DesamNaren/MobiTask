package com.example.mainactivity.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mainactivity.R
import com.example.mainactivity.adapter.StateAdapter
import com.example.mainactivity.application.MobiApplication
import com.example.mainactivity.interfaces.StatesInterface
import com.example.mainactivity.repository.StateRepository
import com.example.mainactivity.source.StatesData
import com.example.mainactivity.ui.WeatherActivity
import com.example.mainactivity.utilities.AppConstants
import com.example.mainactivity.utilities.Extensions.toast
import com.example.mainactivity.utilities.Utils


class SettingsFragment : Fragment() , StatesInterface{

    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var reset: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        settingsViewModel =
            ViewModelProvider(this).get(SettingsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_settings, container, false)
        reset = root.findViewById(R.id.reset_btn)

        reset.setOnClickListener {
           repo.resetFav(activity, this)
        }
        return root
    }

    var repo: StateRepository = StateRepository()
    override fun stateCount(count: Int) {
        Toast.makeText(activity, "Reset Done" , Toast.LENGTH_SHORT).show()
    }

    override fun checkCount(count: Int, name: String) {
        TODO("Not yet implemented")
    }

    override fun stateFav(flag: Boolean, name: String, pos: Int) {
        TODO("Not yet implemented")
    }

    override fun onItemClick(name: String) {
        TODO("Not yet implemented")
    }
}