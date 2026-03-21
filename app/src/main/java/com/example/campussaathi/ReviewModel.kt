package com.example.campussaathi

data class ReviewModel(
    val rating: Int = 0,
    val serviceId: String = "",
    val studentId: String = "",
    val reviewText: String = "",
    val timestamp: Long? = null,
    val studentName: String = "Student" // Placeholder as requested
)
