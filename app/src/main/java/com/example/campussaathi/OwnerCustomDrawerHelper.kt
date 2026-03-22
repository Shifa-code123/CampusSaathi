package com.example.campussaathi

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class OwnerCustomDrawerHelper(
    private val activity: Activity,
    private val drawerLayout: DrawerLayout,
    private val drawerView: View   //  include wala layout
) {

    private val db = FirebaseFirestore.getInstance()

    fun setup() {

        setupProfile()
        setupClicks()
    }

    // 🔥 PROFILE DATA LOAD
    private fun setupProfile() {

        val profileImage = drawerView.findViewById<ImageView>(R.id.profileImage)
        val userName = drawerView.findViewById<TextView>(R.id.tvUserName)

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->

                userName.text = doc.getString("fullName") ?: "Owner"

                val base64 = doc.getString("profileImageBase64")

                if (!base64.isNullOrEmpty()) {

                    val bytes = Base64.decode(base64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                    profileImage.setImageBitmap(bitmap)
                } else {
                    profileImage.setImageResource(R.drawable.default_avatar)
                }
            }
    }

    // 🔥 MENU CLICK HANDLING
    private fun setupClicks() {

        val menuMyServices = drawerView.findViewById<TextView>(R.id.menuMyServices)
        val menuProfile = drawerView.findViewById<View>(R.id.profileSection)
        val menuSubscription = drawerView.findViewById<TextView>(R.id.menuSubscription)
        val menuHelp = drawerView.findViewById<TextView>(R.id.menuHelp)
        val menuAbout = drawerView.findViewById<TextView>(R.id.menuAbout)
        val menuDarkMode = drawerView.findViewById<TextView>(R.id.menuDarkMode)
        val menuLogout = drawerView.findViewById<TextView>(R.id.menuLogout)

        // 👉 My Services
        menuMyServices.setOnClickListener {
            activity.startActivity(Intent(activity, ActivityOwnerViewListing::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        // 👉 Verification
        menuProfile.setOnClickListener {
            activity.startActivity(Intent(activity, ActivityOwnerProfile::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        // 👉 Subscription
        menuSubscription.setOnClickListener {
            // Coming soon
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        //  Help
        menuHelp.setOnClickListener {
            activity.startActivity(Intent(activity, SupportActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }


        menuAbout.setOnClickListener {
            activity.startActivity(Intent(activity, AboutActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        //  Dark Mode (basic toggle)
        menuDarkMode.setOnClickListener {
            // Tu baad me implement karega (AppCompatDelegate)
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        //  Logout
        menuLogout.setOnClickListener {

            FirebaseAuth.getInstance().signOut()

            val intent = Intent(activity, LoginActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK

            activity.startActivity(intent)
            activity.finish()
        }
    }
}