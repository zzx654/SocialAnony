package com.example.appportfolio.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.appportfolio.R
import com.example.appportfolio.data.entities.ChatRequests
import com.example.appportfolio.databinding.ItemChatrequestBinding
class ChatRequestsAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater= LayoutInflater.from(parent.context)
        return DataBindingUtil.inflate<ItemChatrequestBinding>(
            layoutInflater,
            R.layout.item_chatrequest,
            parent,
            false
        ).let{
            ChatRequestsViewHolder(it)
        }
    }
    inner class ChatRequestsViewHolder(val binding:ItemChatrequestBinding):RecyclerView.ViewHolder(binding.root){
        fun onbind(chatrequest: ChatRequests)
        {
            binding.chatrequest=chatrequest

           binding.btnaccept.setOnClickListener {
             AcceptClickListener?.let{ click->
                    click(chatrequest)
               }
            }
            binding.btnrefuse.setOnClickListener {
                RefuseClickListener?.let{ click->
                    click(chatrequest)
                }
            }
        }
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chatrequest=chatrequests[position]
        (holder as ChatRequestsAdapter.ChatRequestsViewHolder).onbind(chatrequest)
    }

    override fun getItemCount(): Int {
        return chatrequests.size
    }
    private val diffCallback=object: DiffUtil.ItemCallback<ChatRequests>(){
        override fun areContentsTheSame(oldItem: ChatRequests, newItem:ChatRequests): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areItemsTheSame(oldItem: ChatRequests, newItem: ChatRequests): Boolean {
            return oldItem==newItem
        }
    }
    val differ= AsyncListDiffer(this,diffCallback)

    var chatrequests:List<ChatRequests>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    var AcceptClickListener:((ChatRequests)->Unit)?=null

   fun setOnAcceptClickListener(listener: (ChatRequests) -> Unit){
       AcceptClickListener=listener
    }
    var RefuseClickListener:((ChatRequests)->Unit)?=null

    fun setOnRefuseClickListener(listener: (ChatRequests) -> Unit){
        RefuseClickListener=listener
    }

}