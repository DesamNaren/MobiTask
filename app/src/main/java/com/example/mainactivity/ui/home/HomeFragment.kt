package com.example.mainactivity.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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


class HomeFragment : Fragment(), StatesInterface {

    private lateinit var listOfData: ArrayList<StatesData>
    private lateinit var homeViewModel: SettingsViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StateAdapter
    private var deletePos: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(SettingsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        recyclerView = root.findViewById(R.id.states_rv)
        listOfData = ArrayList()
        adapter = StateAdapter((activity)!!, listOfData, this)
        getLocalStates()

        return root
    }

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

        val newIntent = Intent(activity, WeatherActivity::class.java)
        newIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(newIntent)
    }

    private fun getLocalStates() {

        homeViewModel.getLocalStates((activity)!!).observe((activity)!!, Observer { statesData ->
            when {

                statesData.isNotEmpty() -> {

                    listOfData.clear()
                    listOfData.addAll(statesData)

                    adapter.notifyDataSetChanged()
                    recyclerView.adapter = adapter
                    recyclerView.layoutManager = LinearLayoutManager(context)
                    if (pos != -1) {
                        recyclerView.scrollToPosition(pos)
                    }

                    ItemTouchHelper(object :
                        ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                        override fun onMove(
                            recyclerView: RecyclerView,
                            viewHolder: RecyclerView.ViewHolder,
                            target: RecyclerView.ViewHolder
                        ): Boolean {

                            return false
                        }

                        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                            deletePos = viewHolder.adapterPosition
                            val deletedCourse: StatesData =
                                listOfData.get(deletePos)
                            listOfData.removeAt(deletePos)
                            adapter.notifyItemRemoved(deletePos)
                            repo.deleteItem(
                                this@HomeFragment,
                                deletedCourse.state_name,
                                requireActivity()
                            )
                        }
                    }).attachToRecyclerView(recyclerView)
                }
                else -> {
                    callStatesAPI()
                }
            }
        })
    }
}