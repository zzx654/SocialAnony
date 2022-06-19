package com.example.appportfolio.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication.Companion.onSingleClick
import com.example.appportfolio.databinding.FragmentPostdetailsBinding
import com.example.appportfolio.databinding.ItemTextheaderBinding

class TextHeaderAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater= LayoutInflater.from(parent.context)
        return DataBindingUtil.inflate<ItemTextheaderBinding>(
            layoutInflater,
            R.layout.item_textheader,
            parent,
            false
        ).let {
            TextViewHolder(it)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as TextViewHolder).onbind()
    }

    override fun getItemCount()=1

    inner class TextViewHolder(val binding:ItemTextheaderBinding):RecyclerView.ViewHolder(binding.root) {
        fun onbind()
        {
            binding.tvgroup.text=title
            binding.guideContainer.visibility=if(tvContainerVis) View.VISIBLE else View.GONE
            binding.loadmore.visibility=if(loadmoreVis) View.VISIBLE else View.GONE
            binding.tvguide.text=guideText
            binding.loadmore.onSingleClick {
                loadmoreClickListener?.let { click->
                    click()
                }
            }
        }

    }
    var title=""
    var tvContainerVis=false
    var guideText=""
    var loadmoreVis=false
    var loadmoreClickListener:(()->Unit)?=null

    fun setloadmoreClickListener(listener:()->Unit){
        loadmoreClickListener=listener
    }
}