package com.example.campussaathi

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class OwnerPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    private val sharedPref = fragmentActivity.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

    override fun getItemCount(): Int = 5

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> OwnerHomeFragment()
            1 -> MyServicesFragment()
            2 -> {
                val isServicePending = sharedPref.getBoolean("isServicePending", false)
                if (isServicePending) ServiceSuccessFragment() else AddServiceFragment()
            }
            3 -> ReviewFragment()
            4 -> BusinessProfileFragment()
            else -> OwnerHomeFragment()
        }
    }
}
