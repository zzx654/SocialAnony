package com.example.appportfolio.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.appportfolio.R
import com.example.appportfolio.data.entities.SearchResultEntity
import com.example.appportfolio.databinding.ItemLocBinding


class SearchRecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var currentPage = 1
    var currentSearchString = ""
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater= LayoutInflater.from(parent.context)
        return DataBindingUtil.inflate<ItemLocBinding>(
            layoutInflater,
            R.layout.item_loc,
            parent,
            false
        ).let{
            searchViewHolder(it)
        }
    }
    inner class searchViewHolder(val binding:ItemLocBinding):RecyclerView.ViewHolder(binding.root) {
        fun onbind(result: SearchResultEntity) {
            binding.result=result
            binding.root.setOnClickListener {
                itemClickListener?.let{ click->
                    click(result)
                }
            }
        }
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val result=results[position]
        (holder as SearchRecyclerAdapter.searchViewHolder).onbind(result)
    }
    override fun getItemCount(): Int {
        return results.size
    }
    private val diffCallback=object: DiffUtil.ItemCallback<SearchResultEntity>(){
        override fun areContentsTheSame(oldItem: SearchResultEntity, newItem: SearchResultEntity): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areItemsTheSame(oldItem: SearchResultEntity, newItem: SearchResultEntity): Boolean {
            return oldItem==newItem
        }
    }
    val differ= AsyncListDiffer(this,diffCallback)

    var results:List<SearchResultEntity>
        get() = differ.currentList
        set(value) = differ.submitList(value)
    var itemClickListener:((SearchResultEntity)->Unit)?=null

    fun setOnItemClickListener(listener: (SearchResultEntity) -> Unit){
        itemClickListener=listener
    }
}