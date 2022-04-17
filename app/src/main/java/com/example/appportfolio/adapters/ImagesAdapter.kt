package com.example.appportfolio.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.appportfolio.R
import com.example.appportfolio.databinding.ItemContentimgBinding

class ImagesAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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

        val image=imgUris[position]


        (holder as ContentViewHolder).onbind(image)
    }

    inner class ContentViewHolder(private val binding:ItemContentimgBinding): RecyclerView.ViewHolder(binding.root){
        fun onbind(image:String)
        {
            binding.imageUri=image
        }
    }

    override fun getItemCount(): Int {
        return imgUris.size
    }

    private val diffCallback=object: DiffUtil.ItemCallback<String>(){
        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem==newItem
        }
    }
    val differ= AsyncListDiffer(this,diffCallback)

    var imgUris:List<String>
        get() = differ.currentList
        set(value) = differ.submitList(value)

}