package com.example.campussaathi

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class OwnerPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 5

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> OwnerHomeFragment()
            1 -> MyServicesFragment()
            2 -> AddServiceFragment()
            3 -> ReviewFragment()
            4 -> BusinessProfileFragment()
            else -> OwnerHomeFragment()
        }
    }
}