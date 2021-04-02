package com.example.mainactivity.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.mainactivity.R
import com.example.mainactivity.databinding.StatesItemBinding
import com.example.mainactivity.source.StatesData

class StateAdapter(context: Context, private val list: List<StatesData>?) :
    RecyclerView.Adapter<StateAdapter.ItemHolder>() {
//    var statesInterface: StatesInterface = context as StatesInterface
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
        holder.bind()
    }

    override fun getItemCount(): Int {
        return if (list != null && list.isNotEmpty()) list.size else 0
    }

    class ItemHolder(listItemBinding: StatesItemBinding) :
        RecyclerView.ViewHolder(listItemBinding.getRoot()) {
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
