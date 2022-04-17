package com.example.appportfolio.adapters

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.appportfolio.R
import com.example.appportfolio.data.entities.Voteoption
import com.example.appportfolio.databinding.ItemVoteoptionBinding


class VoteOptionAdapter:RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater= LayoutInflater.from(parent.context)
        return DataBindingUtil.inflate<ItemVoteoptionBinding>(
            layoutInflater,
            R.layout.item_voteoption,
            parent,
            false
        ).let{
            voteoptionViewHolder(it)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val option=options[position]
        (holder as VoteOptionAdapter.voteoptionViewHolder).onbind(option,position)
    }

    override fun getItemCount(): Int {
        return options.size
    }
    inner class voteoptionViewHolder(val binding: ItemVoteoptionBinding):RecyclerView.ViewHolder(binding.root){
        fun onbind(option:Voteoption, position:Int){
            binding.option=option
            binding.edtOption.addTextChangedListener(OptionTextWatcher(position))
        }
    }
    inner class OptionTextWatcher(var position:Int):TextWatcher{
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            options[position].option=p0.toString()
        }

        override fun afterTextChanged(p0: Editable?) {
        }

    }
    private val diffCallback=object: DiffUtil.ItemCallback<Voteoption>(){
        override fun areContentsTheSame(oldItem: Voteoption, newItem:Voteoption): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areItemsTheSame(oldItem:Voteoption, newItem:Voteoption): Boolean {
            return oldItem==newItem
        }
    }

    val differ= AsyncListDiffer(this,diffCallback)

    var options:List<Voteoption>
        get() = differ.currentList
        set(value) = differ.submitList(value)

}