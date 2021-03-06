package com.example.appportfolio.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.appportfolio.R
import com.example.appportfolio.data.entities.Noti
import com.example.appportfolio.databinding.ItemNotiBinding
import com.example.appportfolio.databinding.NetworkStateItemBinding

class NotiAdapter: ListAdapter<Noti, RecyclerView.ViewHolder>(diffUtil) {

    private val NOTI_VIEW_TYPE=1
    private val LOADING_VIEW_TYPE=2
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater= LayoutInflater.from(parent.context)
        return if(viewType==NOTI_VIEW_TYPE){
            DataBindingUtil.inflate<ItemNotiBinding>(
                layoutInflater,
                R.layout.item_noti,
                parent,
                false
            ).let{
                notiViewHolder(it)
            }
        }else{
            DataBindingUtil.inflate<NetworkStateItemBinding>(
                layoutInflater,
                R.layout.network_state_item,
                parent,
                false
            ).let{
                NetworkStateItemViewHolder(it)
            }
        }
    }
    inner class notiViewHolder(val binding:ItemNotiBinding):RecyclerView.ViewHolder(binding.root){
        fun onbind(noti: Noti)
        {
            binding.noti=noti

            binding.root.setOnClickListener {
                NotiClickListener?.let{ click->
                    click(noti)
                }
            }
        }
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(getItemViewType(position)!=LOADING_VIEW_TYPE)
            (holder as NotiAdapter.notiViewHolder).onbind(currentList[position])
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
    override fun getItemViewType(position: Int): Int {
        return if(currentList[position].notiid==null)
            LOADING_VIEW_TYPE
        else
            NOTI_VIEW_TYPE
    }
    override fun getItemCount(): Int {
        return currentList.size
    }

    companion object{
        val diffUtil=object: DiffUtil.ItemCallback<Noti>(){
            override fun areContentsTheSame(oldItem: Noti, newItem: Noti): Boolean {
                return oldItem.hashCode() == newItem.hashCode()
            }

            override fun areItemsTheSame(oldItem: Noti, newItem: Noti): Boolean {
                return oldItem==newItem
            }
        }
    }

    var NotiClickListener:((Noti)->Unit)?=null

    fun setOnNotiClickListener(listener: (Noti) -> Unit){
        NotiClickListener=listener
    }
    inner class NetworkStateItemViewHolder(val binding: NetworkStateItemBinding):RecyclerView.ViewHolder(binding.root)

}