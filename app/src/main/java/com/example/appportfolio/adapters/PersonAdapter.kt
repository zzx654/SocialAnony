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
import com.example.appportfolio.databinding.NetworkStateItemBinding

class PersonAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val PERSON_VIEW_TYPE=1
    private val LOADING_VIEW_TYPE=2
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater= LayoutInflater.from(parent.context)
        return if(viewType==PERSON_VIEW_TYPE)
        {
            DataBindingUtil.inflate<ItemUserBinding>(
                layoutInflater,
                R.layout.item_user,
                parent,
                false
            ).let{
                personViewHolder(it)
            }
        }
        else{
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
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(getItemViewType(position)!=LOADING_VIEW_TYPE)
            (holder as PersonAdapter.personViewHolder).onbind(persons[position])
    }
    override fun getItemCount(): Int {
        return persons.size
    }

    override fun getItemViewType(position: Int): Int {
        return if(persons[position].userid==null)
            LOADING_VIEW_TYPE
        else
            PERSON_VIEW_TYPE
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
    inner class NetworkStateItemViewHolder(val binding: NetworkStateItemBinding):RecyclerView.ViewHolder(binding.root)

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