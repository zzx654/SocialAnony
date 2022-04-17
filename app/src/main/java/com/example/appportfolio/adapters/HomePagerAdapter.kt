package com.example.appportfolio.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.appportfolio.ui.main.fragments.HotFragment
import com.example.appportfolio.ui.main.fragments.NearFragment
import com.example.appportfolio.ui.main.fragments.NewFragment
import com.example.appportfolio.ui.main.fragments.TagFragment
import java.lang.IndexOutOfBoundsException

const val NEAR_INDEX=0
const val HOT_INDEX=1
const val NEW_INDEX=2
const val TAG_INDEX=3
class HomePagerAdapter(fragment: Fragment):FragmentStateAdapter(fragment) {
    private val tabFragmentsCreators: Map<Int, () -> Fragment> = mapOf(
        NEAR_INDEX to { NearFragment() },
        HOT_INDEX to { HotFragment() },
        NEW_INDEX to {NewFragment()},
        TAG_INDEX to {TagFragment()}
    )

    override fun getItemCount()=tabFragmentsCreators.size

    override fun createFragment(position: Int): Fragment {
        return tabFragmentsCreators[position]?.invoke()?:throw IndexOutOfBoundsException()
    }
}