package com.example.appportfolio.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.appportfolio.R
import com.example.appportfolio.databinding.ItemContentimgBinding

class ImagesAdapter: ListAdapter<String,RecyclerView.ViewHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater= LayoutInflater.from(parent.context)
        return DataBindingUtil.inflate<ItemContentimgBinding>(
            layoutInflater,
            R.layout.item_contentimg,
            parent,
            false
        ).let{
            ContentViewHolder(it)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val image=currentList[position]


        (holder as ContentViewHolder).onbind(image)
    }

    inner class ContentViewHolder(private val binding:ItemContentimgBinding): RecyclerView.ViewHolder(binding.root){
        fun onbind(image:String)
        {
            binding.imageUri=image
        }
    }

    override fun getItemCount(): Int {
        return currentList.size
    }


    companion object{
        val diffUtil=object: DiffUtil.ItemCallback<String>(){
            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem.hashCode() == newItem.hashCode()
            }

            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem==newItem
            }
        }
    }
}