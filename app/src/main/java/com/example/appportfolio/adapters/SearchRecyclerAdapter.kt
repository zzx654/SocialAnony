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
import com.example.appportfolio.databinding.NetworkStateItemBinding


class SearchRecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var currentPage = 1
    var currentSearchString = ""
    private val LOADING_VIEW_TYPE=2
    private val LOCATION_VIEW_TYPE=1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater= LayoutInflater.from(parent.context)
        return if(viewType==LOADING_VIEW_TYPE)
        {
            DataBindingUtil.inflate<NetworkStateItemBinding>(
                layoutInflater,
                R.layout.network_state_item,
                parent,
                false
            ).let{
                NetworkStateItemViewHolder(it)
            }
        }
        else{
            DataBindingUtil.inflate<ItemLocBinding>(
                layoutInflater,
                R.layout.item_loc,
                parent,
                false
            ).let{
                searchViewHolder(it)
            }
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
        if(getItemViewType(position)!=LOADING_VIEW_TYPE)
            (holder as SearchRecyclerAdapter.searchViewHolder).onbind(results[position])
    }
    override fun getItemCount(): Int {
        return results.size
    }

    override fun getItemViewType(position: Int): Int {
        return if(results[position].fullAddress==null)
            LOADING_VIEW_TYPE
        else
            LOCATION_VIEW_TYPE

    }
    private val diffCallback=object: DiffUtil.ItemCallback<SearchResultEntity>(){
        override fun areContentsTheSame(oldItem: SearchResultEntity, newItem: SearchResultEntity): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areItemsTheSame(oldItem: SearchResultEntity, newItem: SearchResultEntity): Boolean {
            return oldItem==newItem
        }
    }
    inner class NetworkStateItemViewHolder(val binding: NetworkStateItemBinding):RecyclerView.ViewHolder(binding.root)
    val differ= AsyncListDiffer(this,diffCallback)

    var results:List<SearchResultEntity>
        get() = differ.currentList
        set(value) = differ.submitList(value)
    var itemClickListener:((SearchResultEntity)->Unit)?=null

    fun setOnItemClickListener(listener: (SearchResultEntity) -> Unit){
        itemClickListener=listener
    }
}