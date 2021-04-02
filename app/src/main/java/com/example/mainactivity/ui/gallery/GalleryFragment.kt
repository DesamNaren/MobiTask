package com.example.mainactivity.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.mainactivity.R
import com.example.mainactivity.ui.home.WeatherViewModel

class GalleryFragment : Fragment() {

    private lateinit var galleryViewModel: WeatherViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        galleryViewModel =
                ViewModelProvider(this).get(WeatherViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_gallery, container, false)
        galleryViewModel.getStates("Andhra Pradesh").observe((activity)!!, Observer { weatherData ->
            //insert into db
            if (weatherData != null) {

            }
        })
        return root
    }
}