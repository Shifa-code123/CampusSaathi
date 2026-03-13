package com.example.campussaathi.utils

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.example.campussaathi.LoginActivity
import com.example.campussaathi.R
import com.example.campussaathi.StudentProfileActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import kotlin.jvm.java
import com.example.campussaathi.SupportActivity

object DrawerManager {

    fun setupDrawer(
        activity: Activity,
        drawerLayout: DrawerLayout,
        drawerView: View
    ) {

        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser

        val profileImage =
            drawerView.findViewById<CircleImageView>(R.id.profileImage)

        val tvUserName =
            drawerView.findViewById<TextView>(R.id.tvUserName)

        val profileSection =
            drawerView.findViewById<View>(R.id.profileSection)

        // 🔹 FETCH USER DATA
        user?.uid?.let { uid ->

            db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { doc ->

                    if (doc.exists()) {

                        val name = doc.getString("fullName")
                        val image = doc.getString("profileImage")

                        tvUserName.text = name ?: "Student"

                        if (!image.isNullOrEmpty()) {

                            Glide.with(activity)
                                .load(image)
                                .placeholder(R.drawable.ic_profile)
                                .into(profileImage)

                        } else {

                            profileImage.setImageResource(R.drawable.ic_profile)
                        }
                    }
                }
        }

        // 🔹 PROFILE SECTION CLICK (image + name)
        profileSection.setOnClickListener {

            activity.startActivity(
                Intent(activity, StudentProfileActivity::class.java)
            )

            drawerLayout.closeDrawer(GravityCompat.START)
        }

        // 🔹 HOME
        drawerView.findViewById<View>(R.id.menuHome).setOnClickListener {

            drawerLayout.closeDrawer(GravityCompat.START)
        }

        // 🔹 PROFILE MENU
        drawerView.findViewById<View>(R.id.menuProfile).setOnClickListener {

            activity.startActivity(
                Intent(activity, StudentProfileActivity::class.java)
            )

            drawerLayout.closeDrawer(GravityCompat.START)
        }
        // 🔹 HELP & SUPPORT
        drawerView.findViewById<View>(R.id.menuHelp).setOnClickListener {

            activity.startActivity(
                Intent(activity, SupportActivity::class.java)
            )

            drawerLayout.closeDrawer(GravityCompat.START)
        }

        // 🔹 LOGOUT
        drawerView.findViewById<View>(R.id.menuLogout).setOnClickListener {

            MaterialAlertDialogBuilder(activity)
                .setTitle("Log out of your account?")
                .setPositiveButton("Log Out") { _, _ ->

                    FirebaseAuth.getInstance().signOut()

                    val intent = Intent(
                        activity,
                        LoginActivity::class.java
                    )

                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK

                    activity.startActivity(intent)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}