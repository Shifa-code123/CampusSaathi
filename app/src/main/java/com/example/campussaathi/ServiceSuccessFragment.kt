package com.example.campussaathi

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class ServiceSuccessFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_owner_submission_list1, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnDashboard = view.findViewById<Button>(R.id.btnDashboard)
        val btnAddAnother = view.findViewById<Button>(R.id.addanotherservice)

        btnDashboard.setOnClickListener {
            // Navigate to Dashboard (Index 0)
            (activity as? OwnerMainActivity)?.navigateTo(R.id.nav_home)
        }

        btnAddAnother.setOnClickListener {
            // Reset state and navigate back to AddServiceFragment
            val sharedPref = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            sharedPref.edit().putBoolean("isServicePending", false).apply()

            // We need to refresh the ViewPager to show AddServiceFragment instead of this one
            // One way is to tell the activity to refresh or just navigate to the same tab
            // but since PagerAdapter checks the state, we might need a way to force refresh.
            // For now, let's try just switching tabs or notifying activity.
            (activity as? OwnerMainActivity)?.let { mainActivity ->
                mainActivity.recreatePager() // I will add this method to OwnerMainActivity
                mainActivity.navigateTo(R.id.nav_add)
            }
        }
    }
}
