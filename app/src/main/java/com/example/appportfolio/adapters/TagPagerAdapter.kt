package com.example.appportfolio.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.appportfolio.ui.main.fragments.*
import java.lang.IndexOutOfBoundsException
const val TAGHOT_INDEX=1
const val TAGNEW_INDEX=0
class TagPagerAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {
    private val tabFragmentsCreators: Map<Int, () -> Fragment> = mapOf(

        TAGNEW_INDEX to { NewTagFragment() },
        TAGHOT_INDEX to { HotTagFragment()}

    )

    override fun getItemCount()=tabFragmentsCreators.size

    override fun createFragment(position: Int): Fragment {
        return tabFragmentsCreators[position]?.invoke()?:throw IndexOutOfBoundsException()
    }
}