package com.example.appportfolio.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.appportfolio.R
import com.example.appportfolio.adapters.*
import com.example.appportfolio.databinding.FragmentHomeBinding
import com.google.android.material.tabs.TabLayoutMediator

class HomeFragment: Fragment(R.layout.fragment_posts) {

    lateinit var binding:FragmentHomeBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding= DataBindingUtil.inflate<FragmentHomeBinding>(inflater,
                R.layout.fragment_home,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.runOnUiThread {
            binding.vp.apply{
                adapter=HomePagerAdapter(childFragmentManager,lifecycle)
                offscreenPageLimit=6
                getChildAt(0).overScrollMode=View.OVER_SCROLL_NEVER
                isUserInputEnabled=false
            }
            TabLayoutMediator(binding.tab,binding.vp){ tab,position->
                tab.text=getTabTitle(position)

            }.attach()
        }

    }
    private fun getTabTitle(position: Int):String?{
        return when(position){
            NEAR_INDEX->requireContext().getString(R.string.near)
            HOT_INDEX->requireContext().getString(R.string.hot)
            NEW_INDEX->requireContext().getString(R.string.newposts)
            TAG_INDEX->requireContext().getString(R.string.tag)
            SEARCH_INDEX->requireContext().getString(R.string.person)
            FOLLOW_INDEX->requireContext().getString(R.string.follow)
            else->null
        }
    }
}