package com.example.appportfolio.adapters

import android.content.Context
import android.provider.Telephony.TextBasedSmsColumns.PERSON
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appportfolio.R
import com.example.appportfolio.data.entities.Person
import com.example.appportfolio.databinding.ItemHorizontalrvBinding
import com.example.appportfolio.other.Constants.ITEM

class HorizontalAdapter(val context:Context): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater= LayoutInflater.from(parent.context)
        return DataBindingUtil.inflate<ItemHorizontalrvBinding>(
            layoutInflater,
            R.layout.item_horizontalrv,
            parent,
            false
        ).let {
            HotUserViewHolder(it)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as HotUserViewHolder).onbind()
    }

    override fun getItemViewType(position: Int): Int {
        return ITEM
    }

    override fun getItemCount(): Int=1

    inner class HotUserViewHolder(val binding: ItemHorizontalrvBinding):RecyclerView.ViewHolder(binding.root) {
        fun onbind()
        {

                binding.rvHorizontal.apply {
                    adapter=hotpersonAdapter
                    layoutManager= LinearLayoutManager(context, RecyclerView.HORIZONTAL,false)
                    itemAnimator=null
                }

            hotpersonlist?.let{
                hotpersonAdapter.submitList(it)
            }

        }

    }
    var hotpersonAdapter:HotPersonAdapter=HotPersonAdapter()
    var hotpersonlist:List<Person>?=null

}