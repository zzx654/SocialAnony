package com.example.appportfolio.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.appportfolio.R
import com.example.appportfolio.data.entities.TagResult
import com.example.appportfolio.databinding.ItemTagBinding


class TagAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater= LayoutInflater.from(parent.context)
        return DataBindingUtil.inflate<ItemTagBinding>(
            layoutInflater,
            R.layout.item_tag,
            parent,
            false
        ).let{
            postViewHolder(it)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val tag=tags[position]
        (holder as TagAdapter.postViewHolder).onbind(tag)
    }

    override fun getItemCount(): Int {
        return tags.size
    }

    inner class postViewHolder(val binding: ItemTagBinding):RecyclerView.ViewHolder(binding.root){
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

    private val diffCallback=object: DiffUtil.ItemCallback<TagResult>(){
        override fun areContentsTheSame(oldItem: TagResult, newItem:TagResult): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areItemsTheSame(oldItem:TagResult, newItem:TagResult): Boolean {
            return oldItem==newItem
        }
    }
    val differ= AsyncListDiffer(this,diffCallback)

    var tags:List<TagResult>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    var FavoriteClickListener:((TagResult)->Unit)? = null

    var TagClickListener:((TagResult)->Unit)? = null

    fun setOnTagClickListener(listener:(TagResult)->Unit){
        TagClickListener=listener
    }

    fun setOnFavoriteClickListener(listener: (TagResult) -> Unit){
        FavoriteClickListener=listener
    }

}