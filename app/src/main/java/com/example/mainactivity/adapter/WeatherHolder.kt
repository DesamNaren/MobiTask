package com.example.mainactivity.adapter

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mainactivity.R

class WeatherHolder(view: View) : RecyclerView.ViewHolder(view) {
    var itemDate: TextView = view.findViewById<View>(R.id.itemDate) as TextView
    var itemTemperature: TextView = view.findViewById<View>(R.id.itemTemperature) as TextView
    var itemDescription: TextView = view.findViewById<View>(R.id.itemDescription) as TextView
    var itemWind: TextView = view.findViewById<View>(R.id.itemWind) as TextView
    var itemPressure: TextView = view.findViewById<View>(R.id.itemPressure) as TextView
    var itemHumidity: TextView = view.findViewById<View>(R.id.itemHumidity) as TextView
    var itemIcon: TextView = view.findViewById<View>(R.id.itemIcon) as TextView
    var lineView: View = view.findViewById(R.id.lineView)

}