package com.example.appportfolio.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication.Companion.onSingleClick
import com.example.appportfolio.data.entities.Person
import com.example.appportfolio.databinding.ItemUserBinding

class PersonAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater= LayoutInflater.from(parent.context)
        return DataBindingUtil.inflate<ItemUserBinding>(
            layoutInflater,
            R.layout.item_user,
            parent,
            false
        ).let{
            personViewHolder(it)
        }
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val person=persons[position]
        (holder as PersonAdapter.personViewHolder).onbind(person)
    }
    override fun getItemCount(): Int {
        return persons.size
    }

    inner class personViewHolder(val binding: ItemUserBinding):RecyclerView.ViewHolder(binding.root) {
        fun onbind(person: Person) {
            binding.person = person
            binding.ibFollow.onSingleClick {
                FollowClickListener?.let { click ->
                    click(person)
                }
            }
            binding.root.setOnClickListener {
                PersonClickListener?.let { click ->
                    click(person)
                }
            }
        }
    }

    private val diffCallback=object: DiffUtil.ItemCallback<Person>(){
        override fun areContentsTheSame(oldItem: Person, newItem: Person): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areItemsTheSame(oldItem: Person, newItem: Person): Boolean {
            return oldItem==newItem
        }
    }
    val differ= AsyncListDiffer(this,diffCallback)

    var persons:List<Person>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    var PersonClickListener:((Person)->Unit)?=null

    var FollowClickListener:((Person)->Unit)?=null

    fun setOnPersonClickListener(listener: (Person) -> Unit){
        PersonClickListener=listener
    }
    fun setOnFollowClickListener(listener: (Person) -> Unit){
        FollowClickListener=listener
    }
}