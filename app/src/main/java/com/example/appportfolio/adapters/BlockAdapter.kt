package com.example.appportfolio.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.appportfolio.R
import com.example.appportfolio.data.entities.Block
import com.example.appportfolio.databinding.ItemBlockBinding

class BlockAdapter: ListAdapter<Block, RecyclerView.ViewHolder>(diffUtil) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater= LayoutInflater.from(parent.context)
        return DataBindingUtil.inflate<ItemBlockBinding>(
            layoutInflater,
            R.layout.item_block,
            parent,
            false
        ).let{
            blockViewHolder(it)
        }
    }
    inner class blockViewHolder(val binding:ItemBlockBinding):RecyclerView.ViewHolder(binding.root) {
        fun onbind(block: Block) {
            binding.block=block
            binding.root.setOnClickListener {
                itemClickListener?.let{ click->
                    click(block)
                }
            }
        }
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val block=currentList[position]
        (holder as BlockAdapter.blockViewHolder).onbind(block)
    }

    override fun getItemCount(): Int {
        return currentList.size
    }
    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<Block>() {
            override fun areContentsTheSame(oldItem: Block, newItem: Block): Boolean {
                return oldItem.hashCode() == newItem.hashCode()
            }

            override fun areItemsTheSame(oldItem: Block, newItem: Block): Boolean {
                return oldItem == newItem
            }
        }
    }

    var itemClickListener:((Block)->Unit)?=null

    fun setOnItemClickListener(listener: (Block) -> Unit){
        itemClickListener=listener
    }

}