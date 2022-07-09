package com.example.appportfolio.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.appportfolio.R
import com.example.appportfolio.data.entities.TagResult
import com.example.appportfolio.databinding.ItemTagBinding
import com.example.appportfolio.other.Constants.ITEM


class TagAdapter: ListAdapter<TagResult,RecyclerView.ViewHolder>(diffUtil) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater= LayoutInflater.from(parent.context)
        return DataBindingUtil.inflate<ItemTagBinding>(
            layoutInflater,
            R.layout.item_tag,
            parent,
            false
        ).let{
            TagViewHolder(it)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val tag=currentList[position]
        (holder as TagAdapter.TagViewHolder).onbind(tag)
    }


    override fun getItemViewType(position: Int): Int {
        return ITEM
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    inner class TagViewHolder(val binding: ItemTagBinding):RecyclerView.ViewHolder(binding.root){
        fun onbind(tag: TagResult)
        {
            binding.tag=tag
            binding.ibFav.setOnClickListener {
                FavoriteClickListener?.let{ click->
                    click(tag)
                }
            }
            binding.root.setOnClickListener {
                TagClickListener?.let{ click->
                    click(tag)
                }
            }
        }
    }

    companion object{
        val diffUtil=object: DiffUtil.ItemCallback<TagResult>(){
            override fun areContentsTheSame(oldItem: TagResult, newItem:TagResult): Boolean {
                return oldItem.hashCode() == newItem.hashCode()
            }

            override fun areItemsTheSame(oldItem:TagResult, newItem:TagResult): Boolean {
                return oldItem==newItem
            }
        }
    }
    var FavoriteClickListener:((TagResult)->Unit)? = null

    var TagClickListener:((TagResult)->Unit)? = null

    fun setOnTagClickListener(listener:(TagResult)->Unit){
        TagClickListener=listener
    }

    fun setOnFavoriteClickListener(listener: (TagResult) -> Unit){
        FavoriteClickListener=listener
    }

}