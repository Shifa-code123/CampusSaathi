package com.example.campussaathi

import android.content.Intent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.campussaathi.databinding.AdminHeaderLayoutBinding

fun AppCompatActivity.setupAdminHeader(
    headerBinding: AdminHeaderLayoutBinding,
    title: String,
    showBack: Boolean = false,
    onLogoClick: (() -> Unit)? = null
) {
    headerBinding.adminHeaderTitle.text = title
    
    if (showBack) {
        headerBinding.adminBackBtn.visibility = View.VISIBLE
        headerBinding.adminBackBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        headerBinding.adminLogoLayout.visibility = View.GONE
    } else {
        headerBinding.adminBackBtn.visibility = View.GONE
        headerBinding.adminLogoLayout.visibility = View.VISIBLE
        headerBinding.adminLogoLayout.setOnClickListener {
            onLogoClick?.invoke()
        }
    }

    headerBinding.adminNotificationBtn.setOnClickListener {
        if (this !is AdminNotificationActivity) {
            startActivity(Intent(this, AdminNotificationActivity::class.java))
        }
    }

    headerBinding.adminProfileBtn.setOnClickListener {
        if (this !is AdminProfileActivity) {
            startActivity(Intent(this, AdminProfileActivity::class.java))
        }
    }
}
