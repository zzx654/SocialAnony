package com.example.appportfolio.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.appportfolio.R
import com.example.appportfolio.data.entities.MessageData
import com.example.appportfolio.databinding.*
import com.example.appportfolio.other.ChatType
import java.io.File

class ChatAdapter: ListAdapter<MessageData, RecyclerView.ViewHolder>(diffUtil) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater= LayoutInflater.from(parent.context)
        if(viewType==ChatType.LEFT_MESSAGE){
            return DataBindingUtil.inflate<ChatLefttextBinding>(
                layoutInflater,
                R.layout.chat_lefttext,
                parent,
                false
            ).let{
                LeftTextViewHolder(it)
            }
        }else if(viewType==ChatType.LEFT_IMAGE){
            return DataBindingUtil.inflate<ChatLeftImageBinding>(
                layoutInflater,
                R.layout.chat_left_image,
                parent,
                false
            ).let{
                LeftImageViewHolder(it)
            }
        }else if(viewType==ChatType.RIGHT_MESSAGE){
            return DataBindingUtil.inflate<ChatRighttextBinding>(
                layoutInflater,
                R.layout.chat_righttext,
                parent,
                false
            ).let {
                RightTextViewHolder(it)
            }
        }
        else if(viewType==ChatType.RIGHT_IMAGE){
            return DataBindingUtil.inflate<ChatRightImageBinding>(
                layoutInflater,
                R.layout.chat_right_image,
                parent,
                false
            ).let {
                RightImageViewHolder(it)
            }
        }
        else if(viewType==ChatType.LEFT_LOCATION){
            return DataBindingUtil.inflate<ChatLeftLocationBinding>(
                layoutInflater,
                R.layout.chat_left_location,
                parent,
                false
            ).let {
                LeftLocationViewHolder(it)
            }
        }
        else if(viewType==ChatType.RIGHT_LOCATION){
            return DataBindingUtil.inflate<ChatRightLocationBinding>(
                layoutInflater,
                R.layout.chat_right_location,
                parent,
                false
            ).let {
                RightLocationViewHolder(it)
            }
        }
        else{
            return DataBindingUtil.inflate<ChatCenteritemBinding>(
                layoutInflater,
                R.layout.chat_centeritem,
                parent,
                false
            ).let{
                CenterViewHolder(it)
            }
        }
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val chat=currentList[position]
        if(getItemViewType(position)==ChatType.LEFT_MESSAGE)
        {
            (holder as LeftTextViewHolder).onbind(chat)
        }else if(getItemViewType(position)==ChatType.RIGHT_MESSAGE)
        {
            (holder as RightTextViewHolder).onbind(chat)
        }else if(getItemViewType(position)==ChatType.LEFT_IMAGE)
        {
            (holder as LeftImageViewHolder).onbind(chat)
        }else if(getItemViewType(position)==ChatType.RIGHT_IMAGE)
        {
            (holder as RightImageViewHolder).onbind(chat)
        }else if(getItemViewType(position)==ChatType.LEFT_LOCATION)
        {
            (holder as LeftLocationViewHolder).onbind(chat)
        }else if(getItemViewType(position)==ChatType.RIGHT_LOCATION)
        {
            (holder as RightLocationViewHolder).onbind(chat)
        }
        else{
            (holder as CenterViewHolder).onbind(chat)
        }
    }
    override fun getItemCount(): Int {
        return currentList.size
    }
    override fun getItemViewType(position: Int): Int {
        if(currentList[position].type.equals("DATE")||currentList[position].type.equals("EXIT"))
        {
            return ChatType.CENTER
        }
        else if(currentList[position].type.equals("IMAGE"))
        {
            if(currentList[position].senderid!!.equals(myId))
            {
                return ChatType.RIGHT_IMAGE
            }
            else{
                return ChatType.LEFT_IMAGE
            }
        }
        else if(currentList[position].type.equals("LOCATION"))
        {
            if(currentList[position].senderid!!.equals(myId))
            {
                return ChatType.RIGHT_LOCATION
            }
            else{
                return ChatType.LEFT_LOCATION
            }
        }
        else {
            if(currentList[position].senderid!!.equals(myId))
            {
                return ChatType.RIGHT_MESSAGE
            }
            else{
                return ChatType.LEFT_MESSAGE
            }

        }
    }
    inner class LeftTextViewHolder(private val binding:ChatLefttextBinding):
        RecyclerView.ViewHolder(binding.root){
        fun onbind(chat: MessageData){
            binding.chat=chat
            binding.imgProfile.setOnClickListener {
                profileimgClickListener?.let{ click->
                    click(chat)
                }
            }
        }
    }
    inner class LeftLocationViewHolder(private val binding: ChatLeftLocationBinding):
        RecyclerView.ViewHolder(binding.root){
        fun onbind(chat:MessageData){
            binding.chat=chat
            binding.locationlinear.setOnClickListener {
                locationClickListener?.let{ click->
                    click(chat)
                }
            }
            binding.imgProfile.setOnClickListener {
                profileimgClickListener?.let{ click->
                    click(chat)
                }
            }

        }
    }
    inner class LeftImageViewHolder(private val binding: ChatLeftImageBinding):
        RecyclerView.ViewHolder(binding.root){
        fun onbind(chat:MessageData){
            binding.chat=chat
            binding.imageView.setOnClickListener {
                imageClickListener?.let{ click->
                    click(chat)
                }
            }
            binding.imgProfile.setOnClickListener {
                profileimgClickListener?.let{ click->
                    click(chat)
                }
            }

        }
    }
    inner class RightImageViewHolder(private val binding: ChatRightImageBinding):
        RecyclerView.ViewHolder(binding.root){
        fun onbind(chat:MessageData){
            binding.chat=chat
            binding.imageView.setOnClickListener {
                imageClickListener?.let{ click->
                    click(chat)
                }
            }
        }
    }
    inner class RightTextViewHolder(private val binding: ChatRighttextBinding):
        RecyclerView.ViewHolder(binding.root){
        fun onbind(chat:MessageData){
            binding.chat=chat
        }
    }
    inner class RightLocationViewHolder(private val binding: ChatRightLocationBinding):
        RecyclerView.ViewHolder(binding.root){
        fun onbind(chat:MessageData){
            binding.chat=chat
            binding.locationlinear.setOnClickListener {
                locationClickListener?.let{ click->
                    click(chat)
                }
            }
        }
    }

    inner class CenterViewHolder(private val binding:ChatCenteritemBinding):
        RecyclerView.ViewHolder(binding.root){
        fun onbind(chat: MessageData){
            binding.content=chat
        }
    }
    companion object{
        val diffUtil=object: DiffUtil.ItemCallback<MessageData>(){
            override fun areContentsTheSame(oldItem: MessageData, newItem: MessageData): Boolean {
                return oldItem.hashCode()==newItem.hashCode()
            }
            override fun areItemsTheSame(oldItem: MessageData, newItem: MessageData): Boolean {
                return (oldItem.num==newItem.num)&&(oldItem.nickname==newItem.nickname)
            }
        }
    }
    private var myId:Int?=null
    fun setMyId(myid:Int)
    {
        myId=myid
    }
    var imageClickListener:((MessageData)->Unit)?=null

    var profileimgClickListener:((MessageData)->Unit)?=null
    var locationClickListener:((MessageData)->Unit)?=null

    fun setOnProfileImageClickListener(listener: (MessageData) -> Unit){
        profileimgClickListener=listener
    }
    fun setOnImageClickListener(listener: (MessageData) -> Unit){
        imageClickListener=listener
    }
    fun setOnLocationClickListener(listener:(MessageData)->Unit){
        locationClickListener=listener
    }
}