package com.example.mainactivity.ui.home

import android.content.Context
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
        callStatesAPI()
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


                            val repo = StateRepository()

                            repo.insertStates(this, statesData, (activity)!!)
                        }
                    })
                }
            }
        }
    }

    override fun stateCount(count: Int) {
        Toast.makeText((activity)!!, "" + count, Toast.LENGTH_SHORT).show()


        homeViewModel.getLocalStates((activity)!!).observe((activity)!!, Observer { statesData ->
                val dgMeetAdapter = StateAdapter((activity)!!, statesData)
                recyclerView.adapter = dgMeetAdapter
                recyclerView.layoutManager = LinearLayoutManager(context)
            })

    }
}