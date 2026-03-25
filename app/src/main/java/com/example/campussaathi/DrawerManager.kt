package com.example.campussaathi.utils

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.campussaathi.AboutActivity
import com.example.campussaathi.LoginActivity
import com.example.campussaathi.R
import com.example.campussaathi.SavedActivity
import com.example.campussaathi.StudentProfileActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import com.example.campussaathi.SupportActivity
import com.example.campussaathi.VolunteerActivity

object DrawerManager {

    fun setupDrawer(
        activity: Activity,
        drawerLayout: DrawerLayout,
        drawerView: View
    ) {
        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser

        val profileImage = drawerView.findViewById<CircleImageView>(R.id.profileImage)
        val tvUserName = drawerView.findViewById<TextView>(R.id.tvUserName)
        val profileSection = drawerView.findViewById<View>(R.id.profileSection)

        // Use the common loader for profile image to get real-time updates
        ProfileImageLoader.loadProfile(profileImage)

        // 🔹 FETCH USER DATA (Real-time)
        user?.uid?.let { uid ->
            db.collection("users")
                .document(uid)
                .addSnapshotListener { doc, error ->
                    if (error != null) {
                        Log.e("DrawerManager", "Listen failed.", error)
                        return@addSnapshotListener
                    }

                    if (doc != null && doc.exists()) {
                        val name = doc.getString("fullName")
                        tvUserName.text = name ?: "Student"
                    }
                }
        }

        // 🔹 PROFILE SECTION CLICK (image + name)
        profileSection.setOnClickListener {
            activity.startActivity(Intent(activity, StudentProfileActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        // 🔹 HOME
        drawerView.findViewById<View>(R.id.menuHome).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        // 🔹 PROFILE MENU
        drawerView.findViewById<View>(R.id.menuProfile).setOnClickListener {
            activity.startActivity(Intent(activity, StudentProfileActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        // 🔹 HELP & SUPPORT
        drawerView.findViewById<View>(R.id.menuHelp).setOnClickListener {
            activity.startActivity(Intent(activity, SupportActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        // 🔹 VOLUNTEER PROGRAM
        drawerView.findViewById<View>(R.id.menuVolunteer).setOnClickListener {
            activity.startActivity(Intent(activity, VolunteerActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        // 🔹 ABOUT CAMPUSSAATHI
        drawerView.findViewById<View>(R.id.menuAbout).setOnClickListener {
            activity.startActivity(Intent(activity, AboutActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        // 🔹 LOGOUT
        drawerView.findViewById<View>(R.id.menuLogout).setOnClickListener {
            MaterialAlertDialogBuilder(activity)
                .setTitle("Log out of your account?")
                .setPositiveButton("Log Out") { _, _ ->
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(activity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    activity.startActivity(intent)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        //saved services
        drawerView.findViewById<View>(R.id.menuSaved).setOnClickListener {
            activity.startActivity(Intent(activity, SavedActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }
}
