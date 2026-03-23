package com.example.campussaathi.utils

import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.graphics.BitmapFactory
import android.util.Base64

object ProfileImageLoader {

    fun loadProfile(imageView: ImageView) {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->

                val base64 = document.getString("profileImageBase64")

                if (!base64.isNullOrEmpty()) {
                    val bytes = Base64.decode(base64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                    imageView.setImageBitmap(bitmap)
                }
            }
    }
}