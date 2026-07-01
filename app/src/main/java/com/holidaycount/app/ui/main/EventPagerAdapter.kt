package com.holidaycount.app.ui.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class EventPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return EventListFragment.newInstance(
            when (position) {
                0 -> EventListFragment.TYPE_UPCOMING
                1 -> EventListFragment.TYPE_ALL
                2 -> EventListFragment.TYPE_EXPIRED
                else -> EventListFragment.TYPE_ALL
            }
        )
    }
}
