package com.example.campussaathi

data class AdminVerificationModel(
    val uid: String = "",
    val fullName: String = "",
    val ownerType: String = "",
    val status: String = "pending"
)
