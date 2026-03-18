package com.example.campussaathi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment

class ReviewFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_review, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // 🔥 Drawer setup
        val drawerLayout = view.findViewById<DrawerLayout>(R.id.drawerLayout)
        val drawerView = view.findViewById<View>(R.id.customDrawer)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)

        // 👉 Toolbar click → open drawer
        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // 👉 Custom Drawer Helper
        val drawerHelper = OwnerCustomDrawerHelper(
            requireActivity(),
            drawerLayout,
            drawerView
        )
        drawerHelper.setup()
    }
}