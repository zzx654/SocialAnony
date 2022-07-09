package com.example.appportfolio.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication.Companion.onSingleClick
import com.example.appportfolio.data.entities.Person
import com.example.appportfolio.databinding.ItemPersonsquareBinding

class HotPersonAdapter: ListAdapter<Person, RecyclerView.ViewHolder>(PersonAdapter.diffUtil) {

    companion object{
    val diffUtil=object: androidx.recyclerview.widget.DiffUtil.ItemCallback<Person>(){
        override fun areContentsTheSame(oldItem: Person, newItem: Person): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areItemsTheSame(oldItem: Person, newItem: Person): Boolean {
            return oldItem==newItem
        }
    }
}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater= LayoutInflater.from(parent.context)
        return DataBindingUtil.inflate<ItemPersonsquareBinding>(
            layoutInflater,
            R.layout.item_personsquare,
            parent,
            false
        ).let{
            personViewHolder(it)
        }
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as personViewHolder).onbind(currentList[position])
    }
    inner class personViewHolder(val binding: ItemPersonsquareBinding):RecyclerView.ViewHolder(binding.root) {
        fun onbind(person: Person) {
            binding.person=person
            binding.root.onSingleClick {
                PersonClickListener?.let{ click->
                    click(person)

                }
            }
        }
    }
    var PersonClickListener:((Person)->Unit)?=null

    fun setOnPersonClickListener(listener: (Person) -> Unit){
        PersonClickListener=listener
    }
}