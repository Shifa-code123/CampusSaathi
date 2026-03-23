package com.example.campussaathi

data class ReviewModel(
    val userName: String = "",
    val comment: String = "",
    val rating: Int = 0,
    val userId: String = "",
    val timestamp: Long = 0L,
    val profileImageBase64: String = ""
)
