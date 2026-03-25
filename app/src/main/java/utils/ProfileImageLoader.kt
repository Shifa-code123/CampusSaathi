package com.example.campussaathi.utils

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import com.example.campussaathi.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object ProfileImageLoader {

    /**
     * Loads the profile image into the provided ImageView with real-time updates.
     * Uses Base64 decoding as requested.
     */
    fun loadProfile(imageView: ImageView) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .addSnapshotListener { document, error ->
                if (error != null) {
                    Log.e("ProfileImageLoader", "Error listening for profile changes", error)
                    return@addSnapshotListener
                }

                if (document != null && document.exists()) {
                    val base64 = document.getString("profileImageBase64")
                    
                    if (!base64.isNullOrEmpty()) {
                        try {
                            val bytes = Base64.decode(base64, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            imageView.setImageBitmap(bitmap)
                        } catch (e: Exception) {
                            Log.e("ProfileImageLoader", "Error decoding base64", e)
                            imageView.setImageResource(R.drawable.ic_default_profile)
                        }
                    } else {
                        Log.d("ProfileImageLoader", "No profile image Base64 found; using default")
                        imageView.setImageResource(R.drawable.ic_default_profile)
                    }
                } else {
                    imageView.setImageResource(R.drawable.ic_default_profile)
                }
            }
    }
}
