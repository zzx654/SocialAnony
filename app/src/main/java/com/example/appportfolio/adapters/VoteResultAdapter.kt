package com.example.appportfolio.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.appportfolio.R
import com.example.appportfolio.data.entities.Voteresult
import com.example.appportfolio.databinding.ItemVoteresultBinding

class VoteResultAdapter: ListAdapter<Voteresult,RecyclerView.ViewHolder>(diffUtil) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return DataBindingUtil.inflate<ItemVoteresultBinding>(
            layoutInflater,
            R.layout.item_voteresult,
            parent,
            false
        ).let {
            voteresultViewHolder(it)
        }
    }
    override fun getItemCount(): Int {
        return currentList.size
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val result = currentList[position]
        (holder as VoteResultAdapter.voteresultViewHolder).onbind(result, position)
    }

    inner class voteresultViewHolder(val binding: ItemVoteresultBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onbind(result: Voteresult, position: Int) {
            binding.voteresult = result
        }
    }
    companion object{
        val diffUtil=object: DiffUtil.ItemCallback<Voteresult>(){
            override fun areContentsTheSame(oldItem: Voteresult, newItem:Voteresult): Boolean {
                return oldItem.hashCode() == newItem.hashCode()
            }

            override fun areItemsTheSame(oldItem:Voteresult, newItem:Voteresult): Boolean {
                return oldItem==newItem
            }
        }
    }
}