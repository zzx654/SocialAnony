package com.example.appportfolio.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.appportfolio.R
import com.example.appportfolio.databinding.ItemDividerBinding
import com.example.appportfolio.other.Constants

class DividerAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater= LayoutInflater.from(parent.context)
        return DataBindingUtil.inflate<ItemDividerBinding>(
            layoutInflater,
            R.layout.item_divider,
            parent,
            false
        ).let {
            DividerViewHolder(it)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    }

    override fun getItemCount()=1

    override fun getItemViewType(position: Int): Int {
        return Constants.DIVIDER
    }
    inner class DividerViewHolder(val binding: ItemDividerBinding): RecyclerView.ViewHolder(binding.root)
}