package com.example.mainactivity.ui.home

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mainactivity.R
import com.example.mainactivity.adapter.StateAdapter
import com.example.mainactivity.application.MobiApplication
import com.example.mainactivity.interfaces.StatesInterface
import com.example.mainactivity.repository.StateRepository
import com.example.mainactivity.ui.MainActivity
import com.example.mainactivity.ui.MainWeatherActivity
import com.example.mainactivity.utilities.AppConstants
import com.example.mainactivity.utilities.Extensions.toast
import com.example.mainactivity.utilities.Utils

class HomeFragment : Fragment(), StatesInterface {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        recyclerView = root.findViewById(R.id.states_rv)
        getLocalStates()

        return root
    }

    /** Call States & Cities API*/
    private fun callStatesAPI() {
        when (Utils.checkInternetConnection(activity as Context)) {
            false -> (activity)!!.toast("No Internet Connection")
            else -> {
                val session = MobiApplication.SharedPrefEditorObj.getPreferences()!!
                    .getString(AppConstants.SESSION_TOKEN, "")

                if (session != null) {
                    homeViewModel.getStates(session).observe((activity)!!, Observer { statesData ->
                        //insert into db
                        if (statesData != null) {

                            repo.insertStates(this, statesData, (activity)!!)
                        }
                    })
                }
            }
        }
    }

    var repo: StateRepository = StateRepository()

    override fun stateCount(count: Int) {
        getLocalStates()
    }

    override fun checkCount(count: Int, name: String) {
        TODO("Not yet implemented")
    }

    var pos: Int = -1
    override fun stateFav(flag: Boolean, name: String, pos: Int) {
        this.pos = pos
        repo.updateFav(this, name, flag, (activity)!!)
    }

    override fun onItemClick(name: String) {
        val d = MobiApplication.SharedPrefEditorObj.getPreferencesEditor()!!
        d.putString("name", name).commit()
        val newIntent = Intent(activity, MainWeatherActivity::class.java)
        startActivity(newIntent)
    }

    private fun getLocalStates() {
        homeViewModel.getLocalStates((activity)!!).observe((activity)!!, Observer { statesData ->
            when {
                statesData.isNotEmpty() -> {
                    val dgMeetAdapter = StateAdapter((activity)!!, statesData, this)
                    recyclerView.adapter = dgMeetAdapter
                    recyclerView.layoutManager = LinearLayoutManager(context)
//                    if (pos != -1) {
//                        recyclerView.scrollToPosition(pos)
//                    }
                }
                else -> {
                    callStatesAPI()
                }
            }
        })
    }
}