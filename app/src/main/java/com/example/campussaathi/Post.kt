package com.example.campussaathi

data class Post(
    var postId: String = "",
    val heading: String = "",
    val caption: String = "",
    val img: String = "",        // ✅ Cloudinary URL
    val ownerId: String = "",
    val timestamp: Long = 0
)