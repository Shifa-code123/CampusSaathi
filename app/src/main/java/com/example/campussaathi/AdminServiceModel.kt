package com.example.campussaathi

data class AdminServiceModel(
    val id: String = "",
    val serviceName: String = "",
    val category: String = "",
    val contact: String = "",
    val description: String = "",
    val photos: List<String> = emptyList(),
    val status: String = ""
)