package com.example.campussaathi

data class CityHelpModel(

    val category: String = "",
    val serviceName: String = "",
    val description: String = "",
    val contact: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val photos: List<String> = listOf(),
    val ownerId: String = "",
    val distanceFromCampus: Double = 0.0

)