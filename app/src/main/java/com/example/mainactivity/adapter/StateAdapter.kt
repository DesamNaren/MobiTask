package com.example.mainactivity.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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
                context.resources
                    .getDrawable(android.R.drawable.star_big_on)
            )
        }else {
            holder.listItemBinding.favIv.setImageDrawable(
                context.resources
                    .getDrawable(android.R.drawable.star_big_off)
            )
        }
        holder.listItemBinding.favIv.setOnClickListener {
            if(!dataModel.fav) {
                statesInterface.stateFav(true, dataModel.state_name, i)
            }else{
                statesInterface.stateFav(false, dataModel.state_name, i)
            }
        }
        holder.bind()
    }

    override fun getItemCount(): Int {
        return if (list != null && list.isNotEmpty()) list.size else 0
    }

    class ItemHolder(listItemBinding: StatesItemBinding) :
        RecyclerView.ViewHolder(listItemBinding.root) {
        var listItemBinding: StatesItemBinding
        fun bind() {
            listItemBinding.executePendingBindings()
        }

        init {
            this.listItemBinding = listItemBinding
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

}
