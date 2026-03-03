package com.example.campussaathi

data class NearbyPlace(
    val name: String = "",
    val about: String = "",
    val distance: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val photos: List<String> = emptyList(),
    val contacts: List<String> = emptyList()
)