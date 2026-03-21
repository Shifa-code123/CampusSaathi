package com.example.campussaathi

import com.google.firebase.firestore.DocumentId

data class Student_ServiceModel(
    @DocumentId val id: String = "",
    val serviceName: String = "",
    val category: String = "",
    val description: String = "",
    val contact: String = "",
    val latitude: Double? = 0.0,
    val longitude: Double? = 0.0,
    val photos: List<String> = emptyList(),
    val distance: String = "0.5 km",
    val status: String = "pending"
)