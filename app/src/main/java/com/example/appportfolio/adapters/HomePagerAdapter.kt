package com.example.appportfolio.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.appportfolio.ui.main.fragments.*
import java.lang.IndexOutOfBoundsException

const val NEAR_INDEX=0
const val HOT_INDEX=1
const val NEW_INDEX=2
const val FOLLOW_INDEX=3
const val TAG_INDEX=4
const val SEARCH_INDEX=5

class HomePagerAdapter(fm: FragmentManager, lifecycle: Lifecycle):FragmentStateAdapter(fm,lifecycle) {
    private val tabFragmentsCreators: Map<Int, () -> Fragment> = mapOf(
        NEAR_INDEX to { NearFragment() },
        HOT_INDEX to { HotFragment() },
        NEW_INDEX to {NewFragment()},
        FOLLOW_INDEX to {FollowPostsFragment()},
        TAG_INDEX to {TagFragment()},
        SEARCH_INDEX to {SearchPersonFragment()}

    )

    override fun getItemCount()=tabFragmentsCreators.size

    override fun createFragment(position: Int): Fragment {
        return tabFragmentsCreators[position]?.invoke()?:throw IndexOutOfBoundsException()
    }
}