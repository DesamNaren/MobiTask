package com.example.mainactivity.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.mainactivity.R
import com.example.mainactivity.databinding.StatesItemBinding
import com.example.mainactivity.interfaces.StatesInterface
import com.example.mainactivity.source.StatesData

class StateAdapter(var context: Context, private val list: List<StatesData>?, private var statesInterface: StatesInterface) :
    RecyclerView.Adapter<StateAdapter.ItemHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val listItemBinding: StatesItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.states_item, parent, false
        )
        return ItemHolder(listItemBinding)
    }

    override fun onBindViewHolder(holder: ItemHolder, i: Int) {
        val dataModel = list!![i]
        holder.listItemBinding.stateData = dataModel
        if(dataModel.fav) {
            holder.listItemBinding.favIv.setImageDrawable(
                ResourcesCompat.getDrawable(context.resources, android.R.drawable.star_big_on, null)
            )
        }else {
            holder.listItemBinding.favIv.setImageDrawable(
                ResourcesCompat.getDrawable(context.resources, android.R.drawable.star_big_off, null)

            )
        }
        holder.listItemBinding.favIv.setOnClickListener {
            if(!dataModel.fav) {
                statesInterface.stateFav(true, dataModel.state_name, i)
            }else{
                statesInterface.stateFav(false, dataModel.state_name, i)
            }
        }

        holder.listItemBinding.rvItem.setOnClickListener {
            if(dataModel.fav) {
                statesInterface.onItemClick(dataModel.state_name)
            }else{
                Toast.makeText(context, "Mark city as favourite", Toast.LENGTH_SHORT).show()
            }
        }
        holder.bind()
    }

    override fun getItemCount(): Int {
        return if (list != null && list.isNotEmpty()) list.size else 0
    }

    class ItemHolder(var listItemBinding: StatesItemBinding) :
        RecyclerView.ViewHolder(listItemBinding.root) {
        fun bind() {
            listItemBinding.executePendingBindings()
        }

    }


    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }


}
