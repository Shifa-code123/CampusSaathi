package com.example.campussaathi

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class OwnerPagerAdapter(activity: AppCompatActivity) :
    FragmentStateAdapter(activity) {

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