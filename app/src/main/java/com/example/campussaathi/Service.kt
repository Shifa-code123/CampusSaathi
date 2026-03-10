package com.example.campussaathi

data class Service(
    val ownerId: String,
    val photos: List<String>,
    val serviceName: String,
    val latitude: Double,
    val longitude: Double,
    val phone: String,
    val description: String
)